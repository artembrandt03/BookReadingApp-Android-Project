package com.example.mobile_dev_project.ui.screens

//New Imports for TTS
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mobile_dev_project.R
import com.example.mobile_dev_project.data.Chapter
import com.example.mobile_dev_project.ui.state.TtsState
import com.example.mobile_dev_project.ui.utils.ImmersiveMode
import com.example.mobile_dev_project.vm.AppViewModel
import com.example.mobile_dev_project.vm.TTSViewModel
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource

/**
 * Reading Screen that displays book content
 * - Vertical scrolling within a chapter
 * - Horizontal swiping between chapters
 * - Supports immersive mode (tap to toggle full screen)
 *
 * @param viewModel The shared AppViewModel
 * @param modifier Modifier for this composable
 *
 */
@Composable
fun ReadingScreen(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel,
    ttsViewModel: TTSViewModel
) {
    val books by viewModel.books.collectAsState()
    val currentBookIdNullable = viewModel.currentBookId
    val chapters = viewModel.chapters
    val currentChapterIndex = viewModel.currentChapterIndex
    val isImmersiveMode = viewModel.immersive
    val searchPosition = viewModel.selectedSearchPosition
    val searchLength = viewModel.selectedSearchLength
    val searchQuery = viewModel.selectedSearchQuery
    val searchOccurrence = viewModel.selectedSearchOccurrence

    //we get TTS state from the viewmodel
    val ttsState by ttsViewModel.state.collectAsState()

    //Stop TTS when this screen leaves composition (navigate away)
//    DisposableEffect(Unit) {
//        onDispose {
//            ttsViewModel.stop()
//        }
//    }

    //Clear search position after using it
    LaunchedEffect(searchPosition) {
        if (searchPosition != null) {
            kotlinx.coroutines.delay(1000)
            viewModel.clearSearchPosition()
        }
    }

    if (currentBookIdNullable == null || chapters.isEmpty()) {
        Box(modifier = modifier.fillMaxSize())
        return
    }

    //we make it non-null for the rest of the composable
    val bookId = currentBookIdNullable

    val pagerState = rememberPagerState(
        initialPage = currentChapterIndex,
        pageCount = { chapters.size }
    )

    //stop TTS when user switches chapters
    var lastPage by remember { mutableStateOf(pagerState.currentPage) }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != lastPage) {
            // user swiped to a different chapter → stop reading old one
            ttsViewModel.stop()
            lastPage = pagerState.currentPage
        }
    }

    ImmersiveMode(isEnabled = isImmersiveMode) {
        //Artem: changed to box so we can overlay TTS controls on top of the reader
        Box(
            modifier = modifier
                .fillMaxSize()
                .testTag("reading_screen")
        ) {
            ReadingScreenContent(
                chapters = chapters,
                pagerState = pagerState,
                bookId = bookId,
                onToggleImmersiveMode = { viewModel.toggleImmersive() },
                onScroll = { chapterIndex, y ->
                    viewModel.rememberScrollY(chapterIndex, y)
                    viewModel.saveReadingProgress(bookId, chapterIndex, y)
                },
                initialScrollFor = { index ->
                    viewModel.scrollYByChapter[index] ?: 0
                },
                searchQuery = searchQuery,
                searchOccurrence = searchOccurrence,
                modifier = modifier
            )

            //TTS CONTROL BAR AT THE BOTTOM
            //Hide TTS bar when in immersive mode (new)
            if (!isImmersiveMode) {
                TtsControlBar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(dimensionResource(id = R.dimen.spacing_small)),
                    ttsState = ttsState,
                    onPlayPause = {
                        val pageIndex = pagerState.currentPage
                        val chapter = chapters[pageIndex]

                        when (ttsState) {
                            is TtsState.Playing -> ttsViewModel.pause()
                            is TtsState.Paused -> ttsViewModel.play()
                            is TtsState.Stopped, TtsState.Idle, is TtsState.Error -> {
                                ttsViewModel.startChapter(
                                    chapterIndex = pageIndex,
                                    chapterPath = chapter.htmlFilePath,
                                    startOffset = 0
                                )
                            }
                            is TtsState.Preparing -> { /* ignore */ }
                        }
                    },
                    onStop = { ttsViewModel.stop() }
                )
            }
        }
    }
}

@Composable
private fun ReadingScreenContent(
    chapters: List<Chapter>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    bookId: String,
    onToggleImmersiveMode: () -> Unit,
    onScroll: (chapterIndex: Int, y: Int) -> Unit,
    initialScrollFor: (Int) -> Int,
    searchQuery: String? = null,
    searchOccurrence: Int? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        onClick = onToggleImmersiveMode,
        tonalElevation = dimensionResource(id = R.dimen.spacing_small)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            ChapterContent(
                chapter = chapters[pageIndex],
                initialScrollY = initialScrollFor(pageIndex),
                onScrollY = { y -> onScroll(pageIndex, y) },
                searchQuery = searchQuery,
                searchOccurrence = searchOccurrence
            )
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
@Composable
private fun ChapterContent(
    chapter: Chapter,
    initialScrollY: Int,
    onScrollY: (Int) -> Unit,
    searchQuery: String? = null,
    searchOccurrence: Int? = null
) {
    //Prepare HTML + baseUrl once per chapter file path
    val htmlContent = remember(chapter.htmlFilePath) {
        try {
            if (chapter.htmlFilePath.isNotEmpty()) {
                val file = java.io.File(chapter.htmlFilePath)
                if (file.exists()) file.readText()
                else "<html><body><p>File not found: ${chapter.htmlFilePath}</p></body></html>"
            } else {
                "<html><body><p>${chapter.content}</p></body></html>"
            }
        } catch (e: Exception) {
            "<html><body><p>Error loading chapter: ${e.message}</p></body></html>"
        }
    }

    val baseUrl = remember(chapter.htmlFilePath) {
        if (chapter.htmlFilePath.isNotEmpty()) {
            val f = java.io.File(chapter.htmlFilePath)
            "file://${f.parent}/"
        } else null
    }

    //Apply initial scroll only once per chapter
    //Reset when searchQuery changes to allow search scroll
    val appliedInitial = remember(chapter.id, searchQuery) { mutableStateOf(false) }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.reading_content_padding)),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.allowFileAccess = true
                settings.allowContentAccess = true

                //Fix here: prevent pager from stealing vertical scrolls, but allow real horizontal swipes
                //Before scrolling down would be really hard and screen would 'wiggle' left and right
                //Now you can properly scroll not only from the 'middle column' on the screen
                //but anywhere, and swiping between chapters still works
                var downX = 0f
                var downY = 0f
                val slop = ViewConfiguration.get(context).scaledTouchSlop * 1.5f // tweak sensitivity

                setOnTouchListener { v, ev ->
                    when (ev.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            downX = ev.x
                            downY = ev.y
                            v.parent?.requestDisallowInterceptTouchEvent(true)
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val dx = kotlin.math.abs(ev.x - downX)
                            val dy = kotlin.math.abs(ev.y - downY)
                            val clearlyHorizontal = dx > dy && dx > slop
                            v.parent?.requestDisallowInterceptTouchEvent(!clearlyHorizontal)
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            //release after gesture ends
                            v.parent?.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                    false //let WebView handle the scroll
                }

                //Load content ONCE (factory runs once per composition)
                loadDataWithBaseURL(
                    baseUrl,
                    htmlContent,
                    "text/html",
                    "UTF-8",
                    null
                )

                //Inject JavaScript helper to scroll to text position
                webViewClient = object : android.webkit.WebViewClient() {
                    override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        //Inject helper function
                        view?.evaluateJavascript("""
                            function highlightAndScrollToText(searchText, occurrence) {
                                // Remove previous highlights
                                var oldHighlights = document.querySelectorAll('mark.search-highlight');
                                oldHighlights.forEach(function(mark) {
                                    var parent = mark.parentNode;
                                    parent.replaceChild(document.createTextNode(mark.textContent), mark);
                                    parent.normalize();
                                });

                                if (!searchText || searchText.length === 0) return;

                                // Use TreeWalker to search through text nodes
                                var walker = document.createTreeWalker(
                                    document.body,
                                    NodeFilter.SHOW_TEXT,
                                    null,
                                    false
                                );

                                var searchLower = searchText.toLowerCase();
                                var foundCount = 0;
                                var node;

                                while (node = walker.nextNode()) {
                                    var text = node.textContent;
                                    var textLower = text.toLowerCase();

                                    // Find ALL occurrences in this text node
                                    var searchIndex = 0;
                                    while (true) {
                                        var index = textLower.indexOf(searchLower, searchIndex);
                                        if (index === -1) break;

                                        foundCount++;
                                        if (foundCount === occurrence) {
                                            // Found the right occurrence
                                            var range = document.createRange();
                                            range.setStart(node, index);
                                            range.setEnd(node, index + searchText.length);

                                            // Highlight the text
                                            var mark = document.createElement('mark');
                                            mark.className = 'search-highlight';
                                            mark.style.backgroundColor = '#ffeb3b';
                                            mark.style.padding = '2px';
                                            range.surroundContents(mark);

                                            // Scroll to it
                                            var rect = mark.getBoundingClientRect();
                                            window.scrollTo(0, rect.top + window.scrollY - 100);
                                            return;
                                        }

                                        searchIndex = index + searchText.length;
                                    }
                                }
                            }
                        """.trimIndent(), null)

                        //Execute scroll and highlight if search query is set
                        if (!searchQuery.isNullOrEmpty() && searchOccurrence != null) {
                            val escapedQuery = searchQuery.replace("'", "\\'")
                            view?.evaluateJavascript("highlightAndScrollToText('$escapedQuery', $searchOccurrence);", null)
                        }
                    }
                }

                //Apply initial scroll ONCE for this chapter (we don't want recomposition)
                //Skip initial scroll if coming from search - search scroll will handle it
                post {
                    if (!appliedInitial.value) {
                        if (searchQuery.isNullOrEmpty()) {
                            scrollTo(0, initialScrollY)
                        }
                        appliedInitial.value = true
                    }
                }

                //Report user scrolls
                setOnScrollChangeListener { _, _, y, _, _ ->
                    onScrollY(y)
                }
            }
        },
        update = { webView ->
            //IMPORTANT: do NOT reload or re-scroll here.
            //We only react to content changes via remember keys above.
            //Otherwise gonna run into an issue where we recompose on every scroll fame
        }
    )
}

@Composable
private fun TtsControlBar(
    modifier: Modifier = Modifier,
    ttsState: TtsState,
    onPlayPause: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = dimensionResource(id = R.dimen.spacing_small),
        shadowElevation = dimensionResource(id = R.dimen.spacing_small)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.spacing_small)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isPlaying = ttsState is TtsState.Playing

            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
            }

            IconButton(onClick = onStop) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop"
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            //Tiny label for demo/debugging
            Text(
                text = when (ttsState) {
                    is TtsState.Idle -> stringResource(R.string.tts_idle)
                    is TtsState.Preparing -> stringResource(R.string.preparing)
                    is TtsState.Playing -> stringResource(R.string.playing)
                    is TtsState.Paused -> stringResource(R.string.paused)
                    is TtsState.Stopped -> stringResource(R.string.stopped)
                    is TtsState.Error -> stringResource(R.string.tts_error)
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}