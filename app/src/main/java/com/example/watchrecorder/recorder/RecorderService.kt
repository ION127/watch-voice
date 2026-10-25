package com.example.watchrecorder.recorder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaRecorder
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.watchrecorder.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 포그라운드 서비스로 동작하는 녹음기.
 * 화면이 꺼져도 녹음이 유지된다. (안드로이드 규칙상 상시 알림 + 시스템 마이크 표시는 유지됨)
 */
class RecorderService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    private var segmentStart = 0L     // 현재 구간 시작 시각(elapsedRealtime)
    private var accumulatedMs = 0L    // 일시정지 전까지 누적된 시간
    private var paused = false
    private var tickJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRecording()
            ACTION_PAUSE_RESUME -> togglePause()
            ACTION_STOP -> stopRecording()
        }
        return START_NOT_STICKY
    }

    private fun startRecording() {
        if (recorder != null) return

        val dir = File(filesDir, "recordings").apply { mkdirs() }
        val name = "REC_" +
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + ".m4a"
        val file = File(dir, name)
        outputFile = file

        // 알림 먼저 띄워 포그라운드 진입(녹음 시작 전에 startForeground 호출 보장)
        startForegroundNotification()

        val rec = MediaRecorder(this).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        recorder = rec

        segmentStart = SystemClock.elapsedRealtime()
        accumulatedMs = 0L
        paused = false

        RecorderState.update {
            it.copy(active = true, paused = false, elapsedMs = 0L, lastFilePath = null)
        }
        startTicking()
    }

    private fun togglePause() {
        val rec = recorder ?: return
        if (paused) {
            rec.resume()
            segmentStart = SystemClock.elapsedRealtime()
            paused = false
            startTicking()
        } else {
            rec.pause()
            accumulatedMs += SystemClock.elapsedRealtime() - segmentStart
            paused = true
            tickJob?.cancel()
        }
        RecorderState.update { it.copy(paused = paused, elapsedMs = currentElapsed()) }
    }

    private fun stopRecording() {
        val rec = recorder
        val file = outputFile
        var savedPath: String? = null

        if (rec != null) {
            try {
                rec.stop()
                savedPath = file?.absolutePath
            } catch (e: RuntimeException) {
                // 녹음 시간이 너무 짧아 stop()이 실패하면 파일이 손상됨 -> 삭제
                file?.delete()
                savedPath = null
            } finally {
                rec.release()
            }
        }
        recorder = null
        tickJob?.cancel()

        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        RecorderState.update {
            it.copy(
                active = false,
                paused = false,
                elapsedMs = currentElapsed(),
                lastFilePath = savedPath,
            )
        }
        stopSelf()
    }

    private fun currentElapsed(): Long =
        accumulatedMs + if (!paused) SystemClock.elapsedRealtime() - segmentStart else 0L

    private fun startTicking() {
        tickJob?.cancel()
        tickJob = scope.launch {
            while (true) {
                RecorderState.update { it.copy(elapsedMs = currentElapsed()) }
                delay(200)
            }
        }
    }

    private fun startForegroundNotification() {
        val mgr = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.rec_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        )
        mgr.createNotificationChannel(channel)

        val notif: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.rec_notif_title))
            .setSmallIcon(R.drawable.ic_mic)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        ServiceCompat.startForeground(
            this,
            NOTIF_ID,
            notif,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE,
        )
    }

    override fun onDestroy() {
        tickJob?.cancel()
        scope.cancel()
        recorder?.let { runCatching { it.release() } }
        recorder = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.example.watchrecorder.action.START"
        const val ACTION_PAUSE_RESUME = "com.example.watchrecorder.action.PAUSE_RESUME"
        const val ACTION_STOP = "com.example.watchrecorder.action.STOP"

        private const val CHANNEL_ID = "recording"
        private const val NOTIF_ID = 1001

        fun startRecording(context: Context) = send(context, ACTION_START)
        fun pauseOrResume(context: Context) = send(context, ACTION_PAUSE_RESUME)
        fun stopRecording(context: Context) = send(context, ACTION_STOP)

        private fun send(context: Context, action: String) {
            val intent = Intent(context, RecorderService::class.java).setAction(action)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
