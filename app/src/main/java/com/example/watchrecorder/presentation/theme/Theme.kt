package com.example.watchrecorder.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

@Composable
fun WatchRecorderTheme(content: @Composable () -> Unit) {
    // Wear Material 기본(다크) 테마 사용
    MaterialTheme(content = content)
}
