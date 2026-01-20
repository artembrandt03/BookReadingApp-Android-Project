package com.example.mobile_dev_project.vm

import com.example.mobile_dev_project.data.repository.TtsRepository
import com.example.mobile_dev_project.ui.state.TtsState
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TTSViewModelTest {

    private lateinit var repo: TtsRepository
    private lateinit var viewModel: TTSViewModel

    @Before
    fun setUp() {
        repo = mockk(relaxed = true)

        every { repo.state } returns MutableStateFlow(TtsState.Idle)
        every { repo.rate } returns MutableStateFlow(1.0f)
        every { repo.pitch } returns MutableStateFlow(1.0f)

        viewModel = TTSViewModel(repo)
    }

    @Test
    fun `play() calls repo play`() = runTest {
        viewModel.play()

        coVerify(exactly = 1) { repo.play() }
    }

    @Test
    fun `pause() calls repo pause`() {
        viewModel.pause()

        verify(exactly = 1) { repo.pause() }
    }

    @Test
    fun `stop() calls repo stop`() {
        viewModel.stop()

        verify(exactly = 1) { repo.stop() }
    }

    @Test
    fun `prepareChapter() calls repo prepareChapter`() = runTest {
        viewModel.prepareChapter(2, "/path", 100)

        coVerify {
            repo.prepareChapter(2, "/path", 100)
        }
    }

    @Test
    fun `seekTo() calls repo seekTo`() = runTest {
        viewModel.seekTo(450)

        coVerify { repo.seekTo(450) }
    }

    @Test
    fun `setSpeechRate() updates rate`() {
        viewModel.setSpeechRate(1.3f)

        verify { repo.setSpeechRate(1.3f) }
    }

    @Test
    fun `setPitch() updates pitch`() {
        viewModel.setPitch(0.9f)

        verify { repo.setPitch(0.9f) }
    }

}
