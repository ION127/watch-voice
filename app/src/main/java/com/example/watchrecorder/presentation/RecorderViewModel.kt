package com.example.watchrecorder.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.watchrecorder.recorder.RecorderService
import com.example.watchrecorder.recorder.RecorderState
import com.example.watchrecorder.recorder.RecordingStatus
import kotlinx.coroutines.flow.StateFlow

/**
 * 녹음 전용. 재생 기능 없음.
 * - 시작 / 일시정지·재개(같은 파일로 이어서) / 저장(완료)
 */
class RecorderViewModel(app: Application) : AndroidViewModel(app) {

    /** 녹음 상태(서비스가 갱신) */
    val recording: StateFlow<RecordingStatus> = RecorderState.status

    fun startRecording() = RecorderService.startRecording(getApplication())

    /** 일시정지 ↔ 다시 이어서 녹음 (같은 파일) */
    fun pauseResumeRecording() = RecorderService.pauseOrResume(getApplication())

    /** 녹음 끝내고 파일로 저장 */
    fun saveRecording() = RecorderService.stopRecording(getApplication())
}
