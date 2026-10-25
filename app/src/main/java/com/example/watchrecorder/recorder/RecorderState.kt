package com.example.watchrecorder.recorder

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 녹음 서비스와 UI가 공유하는 상태.
 * 서비스가 값을 갱신하고, 화면(ViewModel)이 구독한다.
 */
data class RecordingStatus(
    val active: Boolean = false,       // 녹음(또는 일시정지 상태) 진행 중인가
    val paused: Boolean = false,       // 일시정지 상태인가
    val elapsedMs: Long = 0L,          // 녹음 경과 시간
    val lastFilePath: String? = null,  // 방금 저장된 파일 경로(정지 직후)
)

object RecorderState {
    private val _status = MutableStateFlow(RecordingStatus())
    val status: StateFlow<RecordingStatus> = _status.asStateFlow()

    internal fun update(transform: (RecordingStatus) -> RecordingStatus) {
        _status.value = transform(_status.value)
    }
}
