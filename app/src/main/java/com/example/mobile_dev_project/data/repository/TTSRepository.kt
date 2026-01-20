package com.example.mobile_dev_project.data.repository

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.mobile_dev_project.ui.state.TtsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.cancel
import java.io.File
import org.jsoup.Jsoup


class TtsRepository(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val _state = MutableStateFlow<TtsState>(TtsState.Idle)
    val state: StateFlow<TtsState> = _state.asStateFlow()

    private fun setState(newState: TtsState) {
        _state.value = newState
    }


    private val _rate = MutableStateFlow(1.0f)
    val rate: StateFlow<Float> = _rate.asStateFlow()

    private val _pitch = MutableStateFlow(1.0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private var currentChapterText: String = ""
    private var currentOffset: Int = 0
    private var currentChapter: Int = 0
    private var currentChunkStart = 0


    // Coroutine scope for emitting StateFlow updates
    // https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-supervisor-job.html
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    //https://developer.android.com/reference/android/speech/tts/TextToSpeech
    //https://stackoverflow.com/questions/58606651/what-is-the-purpose-of-let-keyword-in-kotlin
    private fun initializeTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { engine ->
                    val result = engine.setLanguage(Locale.ENGLISH)
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported")
                        scope.launch {
                            setState(TtsState.Error("Language not supported"))
                        }
                    } else {
                        engine.setSpeechRate(_rate.value)
                        engine.setPitch(_pitch.value)

                        // Attach UtteranceProgressListener to handle playback events
                        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                Log.d("TTS", "Utterance started: $utteranceId")
                                try {
                                    val parts = utteranceId?.split("_")
                                    // index 2 is the starting offset
                                    currentChunkStart = parts?.get(2)?.toInt() ?: currentChunkStart
                                } catch (_: Exception) {}
                            }
                            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                                // Update global offset:
                                currentOffset = currentChunkStart + start
                            }

                            override fun onDone(utteranceId: String?) {
                                Log.d("TTS", "Utterance completed: $utteranceId")
                                scope.launch { advancePlayback() }
                            }

                            override fun onError(utteranceId: String?) {
                                Log.e("TTS", "Utterance error: $utteranceId")
                                scope.launch {
                                    setState(TtsState.Error("Playback error"))
                                }
                            }
                        })
                        isTtsReady = true
                        Log.d("TTS", "TextToSpeech initialized successfully")
                    }
                }
            } else {
                Log.e("TTS", "TextToSpeech initialization failed")
                scope.launch {
                    setState(TtsState.Error("TTS initialization failed"))
                }
            }
        }
    }
    init {
        initializeTts()
    }
    // Preparing the TTS Engine
    private fun ensureTtsReady() {
        if (tts == null || !isTtsReady) {
            initializeTts()
        }
    }
    // Loading Text from Chapter (refactored with jsoup)
    suspend fun loadChapterText(chapterPath: String): String = withContext(Dispatchers.IO) {
        try {
            val file = File(chapterPath)
            val html: String = if (file.exists()) {
                //Our parsed chapters on disk
                file.readText()
            } else {
                //Support bundled sample chapters in assets as a fallback
                context.assets.open(chapterPath).bufferedReader().use { it.readText() }
            }

            //Parse HTML with Jsoup
            val doc = Jsoup.parse(html)

            //Remove non-reading stuff: scripts, styles, navbars, headers, footers, etc.
            doc.select("script, style, nav, header, footer, link, meta, noscript").remove()

            //Get visible body text
            val rawText = doc.body()?.text() ?: doc.text()

            //Normalize whitespace so TTS doesn't pause weirdly
            rawText
                .replace("\\s+".toRegex(), " ")
                .trim()
        } catch (e: Exception) {
            Log.e("TTS", "Error loading chapter text from '$chapterPath'", e)
            ""
        }
    }
    // Preparing a Chapter for Playback
    suspend fun prepareChapter(chapter: Int, chapterPath: String, startOffset: Int = 0) {
        //Stop anything currently speaking (old chapter, etc.)
        tts?.stop()

        setState(TtsState.Preparing)

        currentChapterText = loadChapterText(chapterPath)
        currentChapter = chapter
        currentOffset = startOffset

        if (currentChapterText.isEmpty()) {
            setState(TtsState.Error("Failed to load chapter text"))
            return
        }

        //Ready to play, but not speaking yet
        setState(TtsState.Paused)
    }
    // Playback Control
    suspend fun play() {
        ensureTtsReady()

        if (!isTtsReady || currentChapterText.isEmpty()) {
            setState(TtsState.Error("TTS not ready or no text loaded"))
            return
        }
        
        setState(TtsState.Playing(currentChapter, currentOffset))
        speakChunk()
    }

    private fun speakChunk() {
        if (currentOffset >= currentChapterText.length) {
            // End of chapter
            setState(TtsState.Stopped)
            return
        }

        val chunkSize = 1200
        val endOffset = minOf(currentOffset + chunkSize, currentChapterText.length)
        val textChunk = currentChapterText.substring(currentOffset, endOffset)

        val utteranceId = "tts_${currentChapter}_${currentOffset}"

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        tts?.speak(textChunk, TextToSpeech.QUEUE_ADD, params, utteranceId)
    }

    private fun advancePlayback() {
        //If state is not Playing anymore, this is a stale callback → ignore
        if (_state.value !is TtsState.Playing) {
            Log.d("TTS", "advancePlayback ignored, state=${_state.value}")
            return
        }

        val chunkSize = 1200
        currentOffset += chunkSize

        if (currentOffset < currentChapterText.length) {
            setState(TtsState.Playing(currentChapter, currentOffset))
            speakChunk()
        } else {
            setState(TtsState.Stopped)
        }
    }

    // User Interaction Controls
    fun pause() {
        tts?.stop()
        setState(TtsState.Paused)
    }

    fun stop() {
        tts?.stop()
        currentOffset = 0
        setState(TtsState.Stopped)
    }

    suspend fun seekTo(offset: Int) {
        currentOffset = offset.coerceIn(0, currentChapterText.length)
        tts?.stop()

        if (_state.value is TtsState.Playing) {
            play()
        }
    }
    //https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.ranges/coerce-in.html
    // Rate and Pitch Adjustment
    fun setSpeechRate(rate: Float) {
        val clampedRate = rate.coerceIn(0.5f, 2.0f)
        tts?.setSpeechRate(clampedRate)
        scope.launch {
            _rate.emit(clampedRate)
        }
    }

    fun setPitch(pitch: Float) {
        val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
        tts?.setPitch(clampedPitch)
        scope.launch {
            _pitch.emit(clampedPitch)
        }
    }

    // Cleanup
    fun release() {
        tts?.stop()
        tts?.shutdown()
        scope.cancel()
    }
}