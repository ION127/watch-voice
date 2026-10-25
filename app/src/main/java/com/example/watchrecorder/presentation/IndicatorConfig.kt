package com.example.watchrecorder.presentation

import androidx.compose.ui.graphics.Color

/**
 * 녹음 중 표시 점(인디케이터) 설정.
 * 설정 화면 없이 여기 값만 바꾸면 색/크기가 바뀐다.
 * 위치는 "하단 중앙(6시)" 로 고정 — RecordingIndicator 에서 처리.
 *
 * - color  : 어두운 회색(검은 배경에서 거의 안 띄게). 원하면 아래 값만 바꾸면 됨.
 * - sizeDp : 점 지름(dp). 아주 작게 6.
 */
object IndicatorConfig {
    val color: Color = Color(0xFF3A3A3A)
    const val sizeDp: Int = 6
    const val bottomPaddingDp: Int = 8
}
