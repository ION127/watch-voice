package com.example.watchrecorder.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.watchrecorder.presentation.theme.WatchRecorderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WatchRecorderTheme {
                RecorderApp()
            }
        }
    }
}
