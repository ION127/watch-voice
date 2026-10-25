package com.example.watchrecorder.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.watchrecorder.presentation.IndicatorConfig

/**
 * 녹음 중 표시 점. 하단 중앙(6시)에 아주 작게.
 * 부모가 fillMaxSize 된 Box 여야 한다(align 사용).
 */
@Composable
fun BoxScope.RecordingIndicator(visible: Boolean) {
    if (!visible) return
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = IndicatorConfig.bottomPaddingDp.dp)
            .size(IndicatorConfig.sizeDp.dp)
            .clip(CircleShape)
            .background(IndicatorConfig.color),
    )
}
