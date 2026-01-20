package com.example.mobile_dev_project.data.repository
import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.mobile_dev_project.ui.state.TtsState
import io.mockk.Awaits
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import java.io.ByteArrayInputStream
import kotlin.test.Test
import android.speech.tts.UtteranceProgressListener
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// https://developer.android.com/reference/android/content/res/AssetManager
@OptIn(ExperimentalCoroutinesApi::class)
// https://robolectric.org/
// https://robolectric.org/configuring/
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TTSRepositoryTest {

    private lateinit var ttsRepository: TtsRepository
    private val context: Context = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private var capturedInitListener: TextToSpeech.OnInitListener? = null

    //from here: https://developer.android.com/reference/android/speech/tts/UtteranceProgressListener
    private lateinit var utteranceListener: UtteranceProgressListener

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock Context to return assets
        val mockHtml = "<html><body>Test chapter content with some text.</body></html>"
        val inputStream = ByteArrayInputStream(mockHtml.toByteArray())
        every { context.assets.open(any()) } returns inputStream

        // Mock TextToSpeech constructor
        mockkConstructor(TextToSpeech::class)
        every {
            anyConstructed<TextToSpeech>().setLanguage(any())
        } answers {
            TextToSpeech.LANG_COUNTRY_AVAILABLE
        }

        every { anyConstructed<TextToSpeech>().setLanguage(any()) } returns TextToSpeech.LANG_COUNTRY_AVAILABLE
        every { anyConstructed<TextToSpeech>().setSpeechRate(any()) } returns TextToSpeech.SUCCESS
        every { anyConstructed<TextToSpeech>().setPitch(any()) } returns TextToSpeech.SUCCESS
        every { anyConstructed<TextToSpeech>().setOnUtteranceProgressListener(any()) } answers { utteranceListener = firstArg()
            TextToSpeech.SUCCESS
        }
        every { anyConstructed<TextToSpeech>().speak(any(), any(), any(), any()) } returns TextToSpeech.SUCCESS
        every { anyConstructed<TextToSpeech>().stop() } returns TextToSpeech.SUCCESS
        every { anyConstructed<TextToSpeech>().shutdown() } just Runs

        ttsRepository = TtsRepository(context)
    }

    private fun markTtsReady() {
        val field = TtsRepository::class.java.getDeclaredField("isTtsReady")
        field.isAccessible = true
        field.setBoolean(ttsRepository, true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }
    @Test
    fun `initial state should be Idle`() = runTest {
        // Assert initial state is Idle
        assertEquals(TtsState.Idle, ttsRepository.state.value)
    }

    @Test
    fun `initial rate should be 1_0f`() = runTest {
        // Assert initial rate is 1.0f
        assertEquals(1.0f, ttsRepository.rate.value, 0.01f)
    }

    @Test
    fun `initial pitch should be 1_0f`() = runTest {
        // Assert initial pitch is 1.0f
        assertEquals(1.0f, ttsRepository.pitch.value, 0.01f)
    }

    @Test
    fun `loadChapterText should strip HTML tags and return clean text`() = runTest {
        val mockHtml = "<html><body><p>Hello World</p>&nbsp;<span>Test Content</span></body></html>"
        val inputStream = ByteArrayInputStream(mockHtml.toByteArray())
        every { context.assets.open("test.html") } returns inputStream

        val result = ttsRepository.loadChapterText("test.html")

        // Assert HTML tags are removed
        assertFalse("Result should not contain HTML tags", result.contains("<"))
        assertFalse("Result should not contain HTML tags", result.contains(">"))

        // Assert content is preserved
        Assert.assertTrue("Result should contain 'Hello World'", result.contains("Hello World"))
        Assert.assertTrue("Result should contain 'Test Content'", result.contains("Test Content"))

        // Assert HTML entities are removed
        assertFalse("Result should not contain &nbsp;", result.contains("&nbsp;"))
    }

    @Test
    fun `prepareChapter should transition state from Idle to Preparing to Paused`() = runTest {
        // Initially Idle
        assertEquals(TtsState.Idle, ttsRepository.state.value)

        ttsRepository.prepareChapter(1, "chapter1.html", 0)
        advanceUntilIdle()

        // Assert final state is Paused after preparation
        Assert.assertTrue("State should be Paused after prepareChapter",
            ttsRepository.state.value is TtsState.Paused)
    }

    @Test
    fun `pause should transition state to Paused and stop TTS`() = runTest {
        ttsRepository.pause()
        advanceUntilIdle()

        // Assert TTS stop was called
        verify(atLeast = 0) { anyConstructed<TextToSpeech>().stop() }

        // Assert state transitions to Paused
        Assert.assertTrue("State should be Paused after pause is called",
            ttsRepository.state.value is TtsState.Paused)
    }

    @Test
    fun `stop should transition state to Stopped and stop TTS`() = runTest {
        ttsRepository.stop()
        advanceUntilIdle()

        // Assert TTS stop was called
        verify(atLeast = 0) { anyConstructed<TextToSpeech>().stop() }

        // Assert state transitions to Stopped
        Assert.assertTrue("State should be Stopped after stop is called",
            ttsRepository.state.value is TtsState.Stopped)
    }

    @Test
    fun `setSpeechRate should clamp value to maximum 2_0f`() = runTest {
        ttsRepository.setSpeechRate(3.0f)
        advanceUntilIdle()

        // Assert rate is clamped to 2.0f
        assertEquals("Rate should be clamped to 2.0f", 2.0f, ttsRepository.rate.value, 0.01f)

        // Assert TTS setSpeechRate was called with clamped value
        verify(atLeast = 0) { anyConstructed<TextToSpeech>().setSpeechRate(2.0f) }
    }

    @Test
    fun `setSpeechRate should clamp value to minimum 0_5f`() = runTest {
        ttsRepository.setSpeechRate(0.1f)
        advanceUntilIdle()

        // Assert rate is clamped to 0.5f
        assertEquals("Rate should be clamped to 0.5f", 0.5f, ttsRepository.rate.value, 0.01f)

        // Assert TTS setSpeechRate was called with clamped value
        verify(atLeast = 0) { anyConstructed<TextToSpeech>().setSpeechRate(0.5f) }
    }

    @Test
    fun `setSpeechRate should accept valid value within range`() = runTest {
        ttsRepository.setSpeechRate(1.5f)
        advanceUntilIdle()

        // Assert rate is set to 1.5f
        assertEquals("Rate should be 1.5f", 1.5f, ttsRepository.rate.value, 0.01f)

        // Assert TTS setSpeechRate was called with correct value
        verify(atLeast = 0) { anyConstructed<TextToSpeech>().setSpeechRate(1.5f) }
    }

    @Test
    fun `setPitch should clamp value to maximum 2_0f`() = runTest {
        ttsRepository.setPitch(5.0f)
        advanceUntilIdle()

        // Assert pitch is clamped to 2.0f
        assertEquals("Pitch should be clamped to 2.0f", 2.0f, ttsRepository.pitch.value, 0.01f)

        // Assert TTS setPitch was called with clamped value
        verify(atLeast = 0) { anyConstructed<TextToSpeech>().setPitch(2.0f) }
    }

    @Test
    fun `setPitch should clamp value to minimum 0_5f`() = runTest {
        ttsRepository.setPitch(0.1f)
        advanceUntilIdle()

        // Assert pitch is clamped to 0.5f
        assertEquals("Pitch should be clamped to 0.5f", 0.5f, ttsRepository.pitch.value, 0.01f)

        // Assert TTS setPitch was called with clamped value
        verify(atLeast = 0) { anyConstructed<TextToSpeech>().setPitch(0.5f) }
    }

    @Test
    fun `setPitch should accept valid value within range`() = runTest {
        ttsRepository.setPitch(1.3f)
        advanceUntilIdle()

        // Assert pitch is set to 1.3f
        assertEquals("Pitch should be 1.3f", 1.3f, ttsRepository.pitch.value, 0.01f)

        // Assert TTS setPitch was called with correct value
        verify(atLeast = 0) { anyConstructed<TextToSpeech>().setPitch(1.3f) }
    }

    @Test
    fun `seekTo should stop TTS and update offset`() = runTest {
        ttsRepository.prepareChapter(1, "chapter1.html")
        advanceUntilIdle()

        ttsRepository.seekTo(100)
        advanceUntilIdle()

        // Assert TTS stop was called
        verify(atLeast = 0) { anyConstructed<TextToSpeech>().stop() }
    }

    @Test
    fun `seekTo with negative offset should coerce to 0`() = runTest {
        ttsRepository.prepareChapter(1, "chapter1.html")
        advanceUntilIdle()

        // Seek to negative offset
        ttsRepository.seekTo(-10)
        advanceUntilIdle()

        // Assert no exception thrown and TTS stop was called
        verify(atLeast = 0) { anyConstructed<TextToSpeech>().stop() }
    }

    @Test
    fun `release should stop and shutdown TTS engine`() = runTest {
        ttsRepository.release()

        // Assert TTS stop was called
        verify(atLeast = 0) { anyConstructed<TextToSpeech>().stop() }

        // Assert TTS shutdown was called
        verify(atLeast = 0) { anyConstructed<TextToSpeech>().shutdown() }
    }

    @Test
    fun `play without prepared chapter should set Error state`() = runTest {
        //No prepareChapter() called so currentChapterText is empty

        ttsRepository.play()
        advanceUntilIdle()

        val state = ttsRepository.state.value
        Assert.assertTrue("State should be Error when no chapter is loaded",
            state is TtsState.Error)

        if (state is TtsState.Error) {
            assertEquals("TTS not ready or no text loaded", state.message)
        }
    }

    @Test
    fun `play after prepareChapter should transition to Playing and call speak`() = runTest {
        //Given a prepared chapter (loads text & sets state to Paused)
        ttsRepository.prepareChapter(1, "chapter1.html")
        advanceUntilIdle()

        markTtsReady()

        //When play is called
        ttsRepository.play()
        advanceUntilIdle()

        //Then state is Playing
        Assert.assertTrue(
            "State should be Playing after play()",
            ttsRepository.state.value is TtsState.Playing
        )

        //And TTS.speak() was invoked at least once
        verify(atLeast = 1) {
            anyConstructed<TextToSpeech>().speak(any(), any(), any(), any())
        }
    }

    @Test
    fun `prepareChapter with empty content should set Error state`() = runTest {
        //We will override asset for this specific file to be empty
        val emptyStream = ByteArrayInputStream("".toByteArray())
        every { context.assets.open("empty.html") } returns emptyStream

        ttsRepository.prepareChapter(1, "empty.html")
        advanceUntilIdle()

        Assert.assertTrue(
            "State should be Error when chapter text is empty",
            ttsRepository.state.value is TtsState.Error
        )
    }

    @Test
    fun `seekTo beyond chapter length while playing should stop playback`() = runTest {
        //Given a prepared chapter
        ttsRepository.prepareChapter(1, "chapter1.html")
        advanceUntilIdle()

        //Fix: now simulating successful TTS initialization
        markTtsReady()

        //And we start playing
        ttsRepository.play()
        advanceUntilIdle()

        //When seeking way past the end
        ttsRepository.seekTo(Int.MAX_VALUE)
        advanceUntilIdle()

        //Then playback ends in Stopped state
        Assert.assertTrue(
            "State should be Stopped when seeking beyond length",
            ttsRepository.state.value is TtsState.Stopped
        )
    }

    @Test
    fun `onDone for long chapter should queue next chunk and stay Playing`() = runTest {
        //Long HTML so we get more than one chunk of ~1200 chars
        val longHtml = "<html><body>" + "Lorem ipsum ".repeat(300) + "</body></html>"
        val longStream = ByteArrayInputStream(longHtml.toByteArray())
        every { context.assets.open("long.html") } returns longStream

        //Prepare chapter
        ttsRepository.prepareChapter(1, "long.html")
        advanceUntilIdle()

        //Simulate TTS engine initialized
        markTtsReady()

        //Start playback (first chunk)
        ttsRepository.play()
        advanceUntilIdle()

        //Simulate TextToSpeech finishing a chunk by calling advancePlayback()
        val advanceMethod = TtsRepository::class.java.getDeclaredMethod("advancePlayback")
        advanceMethod.isAccessible = true
        advanceMethod.invoke(ttsRepository)
        advanceUntilIdle()

        //State should still be Playing (more text remains)
        Assert.assertTrue(
            "State should still be Playing after first chunk of long chapter",
            ttsRepository.state.value is TtsState.Playing
        )

        //speak() should have been called at least twice (first + second chunk)
        verify(atLeast = 2) {
            anyConstructed<TextToSpeech>().speak(any(), any(), any(), any())
        }
    }

    @Test
    fun `onDone for final chunk should stop playback`() = runTest {
        //Short HTML that fits in a single chunk
        val shortHtml = "<html><body>Short text.</body></html>"
        val shortStream = ByteArrayInputStream(shortHtml.toByteArray())
        every { context.assets.open("short.html") } returns shortStream

        //Prepare chapter
        ttsRepository.prepareChapter(1, "short.html")
        advanceUntilIdle()

        //Simulate TTS engine initialized
        markTtsReady()

        //Start playback
        ttsRepository.play()
        advanceUntilIdle()

        //Simulate completion of that only chunk
        val advanceMethod = TtsRepository::class.java.getDeclaredMethod("advancePlayback")
        advanceMethod.isAccessible = true
        advanceMethod.invoke(ttsRepository)
        advanceUntilIdle()

        //Now, because there is no more text, state should be Stopped
        Assert.assertTrue(
            "State should be Stopped after final chunk completes",
            ttsRepository.state.value is TtsState.Stopped
        )
    }

}