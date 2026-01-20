package com.example.mobile_dev_project.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_dev_project.ui.state.TtsState
import com.example.mobile_dev_project.data.repository.TtsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TTSViewModel @Inject constructor(
    private val ttsRepository: TtsRepository
) : ViewModel() {

    val state: StateFlow<TtsState> = ttsRepository.state
    val rate: StateFlow<Float> = ttsRepository.rate
    val pitch: StateFlow<Float> = ttsRepository.pitch

    fun prepareChapter(chapterIndex: Int, chapterPath: String, startOffset: Int = 0) {
        viewModelScope.launch {
            ttsRepository.prepareChapter(chapterIndex, chapterPath, startOffset)
        }
    }

    fun startChapter(chapterIndex: Int, chapterPath: String, startOffset: Int = 0) {
        viewModelScope.launch {
            //Flush current audio + reset offset
            ttsRepository.stop()

            //Load new chapter text and reset internal state
            ttsRepository.prepareChapter(chapterIndex, chapterPath, startOffset)

            //Immediately start playback for this chapter
            ttsRepository.play()
        }
    }

    fun play() {
        viewModelScope.launch {
            ttsRepository.play()
        }
    }

    fun pause() = ttsRepository.pause()

    fun stop() = ttsRepository.stop()

    fun seekTo(offset: Int) {
        viewModelScope.launch {
            ttsRepository.seekTo(offset)
        }
    }

    fun setSpeechRate(rate: Float) = ttsRepository.setSpeechRate(rate)

    fun setPitch(pitch: Float) = ttsRepository.setPitch(pitch)

    override fun onCleared() {
        super.onCleared()
        ttsRepository.release()
    }
}