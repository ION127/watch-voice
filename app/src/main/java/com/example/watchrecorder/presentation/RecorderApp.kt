package com.example.watchrecorder.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.watchrecorder.R
import com.example.watchrecorder.presentation.components.RecordingIndicator
import com.example.watchrecorder.util.formatDuration

@Composable
fun RecorderApp(vm: RecorderViewModel = viewModel()) {
    val context = LocalContext.current
    val status by vm.recording.collectAsStateWithLifecycle()

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        hasAudioPermission = result[Manifest.permission.RECORD_AUDIO] == true
        if (hasAudioPermission) vm.startRecording()
    }

    // 앱을 열 때마다: 권한 있고 현재 녹음 중이 아니면 바로 새 녹음 시작
    LaunchedEffect(Unit) {
        if (!hasAudioPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS),
            )
        } else if (!status.active) {
            vm.startRecording()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        when {
            !hasAudioPermission -> PermissionContent(
                onRequest = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ),
                    )
                },
            )

            status.active -> RecordingContent(
                elapsedMs = status.elapsedMs,
                paused = status.paused,
                onPauseResume = vm::pauseResumeRecording,
                onSave = vm::saveRecording,
            )

            else -> IdleContent(
                saved = status.lastFilePath != null,
                onStart = vm::startRecording,
            )
        }

        // 녹음 중 표시 점 (하단 중앙, 아주 작게)
        RecordingIndicator(visible = status.active)
    }
}

@Composable
private fun RecordingContent(
    elapsedMs: Long,
    paused: Boolean,
    onPauseResume: () -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = formatDuration(elapsedMs),
            style = MaterialTheme.typography.title1,
            color = Color.White,
        )
        Text(
            text = if (paused) "일시정지됨" else "녹음 중",
            style = MaterialTheme.typography.caption1,
            color = Color(0xFFBBBBBB),
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // 가운데 버튼: 일시정지 ↔ 다시 이어서 녹음
            Button(onClick = onPauseResume) {
                Icon(
                    painter = painterResource(
                        if (paused) R.drawable.ic_play else R.drawable.ic_pause,
                    ),
                    contentDescription = if (paused) "다시 이어서 녹음" else "일시정지",
                    tint = Color.White,
                )
            }
            // 저장(완료)
            Button(
                onClick = onSave,
                colors = ButtonDefaults.secondaryButtonColors(),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_stop),
                    contentDescription = "저장",
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun IdleContent(saved: Boolean, onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (saved) {
            Text(
                text = "저장됨",
                style = MaterialTheme.typography.caption1,
                color = Color(0xFFBBBBBB),
            )
            Spacer(Modifier.height(10.dp))
        }
        Button(onClick = onStart) {
            Icon(
                painter = painterResource(R.drawable.ic_mic),
                contentDescription = "녹음 시작",
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun PermissionContent(onRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "마이크 권한이 필요합니다",
            style = MaterialTheme.typography.caption1,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRequest) {
            Icon(
                painter = painterResource(R.drawable.ic_mic),
                contentDescription = "권한 허용",
                tint = Color.White,
            )
        }
    }
}
