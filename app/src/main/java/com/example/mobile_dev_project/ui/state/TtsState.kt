package com.example.mobile_dev_project.ui.state

sealed class TtsState {
    data object Idle : TtsState()
    data object Preparing : TtsState()
    data class Playing(val chapterIndex: Int, val offset: Int) : TtsState()
    data object Paused : TtsState()
    data object Stopped : TtsState()
    data class Error(val message: String) : TtsState()
}