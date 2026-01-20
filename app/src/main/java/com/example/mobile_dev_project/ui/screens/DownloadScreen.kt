package com.example.mobile_dev_project.ui.screens
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobile_dev_project.R
import com.example.mobile_dev_project.data.DownloadState
import com.example.mobile_dev_project.data.ErrorMessageKey
import com.example.mobile_dev_project.data.ProgressMessageKey
import com.example.mobile_dev_project.vm.AppViewModel
import kotlinx.coroutines.delay
import java.io.File

/**
 * Main screen for downloading textbooks.
 */
@Composable
fun DownloadScreen(
    viewModel: AppViewModel,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val downloadState by viewModel.downloadState.collectAsState()
    val statusMessage = getLocalizedDownloadMessage(downloadState)
    val urlState = viewModel.searchQuery
    // Get book URLs from resources
    val bookUrls = stringArrayResource(id = R.array.book_urls).toList()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.spacing_default))
            .verticalScroll(scrollState)
            .testTag("download_screen")
    ) {
        DownloadBookHeader()
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_default)))
        UrlTextField(viewModel, urlState)
        StatusMessage(statusMessage)
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_default)))
        DownloadButton(
            enabled = urlState.isNotBlank() && !statusMessage.startsWith(stringResource(R.string.downloading)),
            onDownloadClick = {
                val baseDir = File(context.filesDir, "textbooks")
                viewModel.downloadTextbooks(listOf(urlState.trim()), baseDir)
            }
        )
        val success = stringResource(R.string.success)
        val error = stringResource(R.string.error)
        if (statusMessage.contains(success) || statusMessage.contains(error)) {
            ClearButton(viewModel)
        }
        //https://stackoverflow.com/questions/73799916/use-of-launchedeffect-vs-sideeffect-in-jetpack-compose
        //https://developer.android.com/develop/ui/compose/side-effects
        LaunchedEffect(statusMessage) {
            if (statusMessage.contains(success) || statusMessage.contains(error)) {
                delay(2000)
                viewModel.clearDownloadState()
            }
        }
    }
}
/**
 * Text field for entering book URL.
 */
@Composable
fun UrlTextField(viewModel: AppViewModel, urlState :String){
    OutlinedTextField(
        value = urlState,
        onValueChange = { viewModel.updateSearchQuery(it) },
        label = { Text(stringResource(R.string.enter_book_url)) },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    )
}
/**
 * Clear button to reset URL input and download state.
 */
@Composable
fun ClearButton(viewModel: AppViewModel){
    OutlinedButton(
        onClick = {
            viewModel.clearDownloadState()
            viewModel.clearSearch()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.clear))
    }
}
/**
 * Header for the download screen with title and subtitle.
 */
@Composable
fun DownloadBookHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.download_title),
            style = MaterialTheme.typography.titleLarge,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_tiny)))
        Text(
            text = stringResource(R.string.download_and_extract_textbooks),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
/**
 * Button to trigger the download process.
 *
 * @param enabled Whether the button is clickable
 * @param onDownloadClick Lambda executed when button is clicked
 */
@Composable
fun DownloadButton(
    enabled: Boolean = true,
    onDownloadClick: () -> Unit,
) {
    Button(
        onClick = onDownloadClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        enabled = enabled
    ) {
        Text(
            text = stringResource(R.string.download_button),
            fontSize = 16.sp
        )
    }
}

/**
 * Displays status messages based on download state.
 *
 * @param statusMessage The message to display
 */
//https://kotlinlang.org/docs/lambdas.html#higher-order-functions
@Composable
fun StatusMessage(statusMessage: String) {
    when (statusMessage) {
        stringResource(R.string.loading) -> Text(
            text = stringResource(R.string.loading_in_progress),
            color = Color.Gray,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        else -> Text(
            text = statusMessage,
            color = when (statusMessage) {
                stringResource(R.string.success) -> Color(0xFF4CAF50)
                stringResource(R.string.error) -> Color(0xFFF44336)
                else -> Color.Gray
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Converts DownloadState to localized message string
 */
@Composable
fun getLocalizedDownloadMessage(state: DownloadState): String {
    return when (state) {
        is DownloadState.Idle -> ""
        is DownloadState.Loading -> stringResource(R.string.loading)
        is DownloadState.Success -> stringResource(R.string.success)

        is DownloadState.Progress -> when (val key = state.messageKey) {
            is ProgressMessageKey.StartingDownloads ->
                stringResource(R.string.progress_starting_downloads)
            is ProgressMessageKey.BookAlreadyInLibrary ->
                stringResource(R.string.progress_book_already_in_library, key.title)
            is ProgressMessageKey.BookAlreadyExists ->
                stringResource(R.string.progress_book_already_exists, key.title)
            is ProgressMessageKey.Downloading ->
                stringResource(R.string.progress_downloading, key.fileName)
            is ProgressMessageKey.Unzipping ->
                stringResource(R.string.progress_unzipping, key.fileName)
            is ProgressMessageKey.Parsing ->
                stringResource(R.string.progress_parsing, key.folderName)
            is ProgressMessageKey.Completed ->
                stringResource(R.string.progress_completed, key.folderName)
            }
        is DownloadState.Error -> when (val key = state.errorKey) {
            is ErrorMessageKey.DownloadFailed ->
                stringResource(R.string.error_download_failed, key.fileName)
            is ErrorMessageKey.ParseFailed ->
                stringResource(R.string.error_parse_failed, key.folderName)
            is ErrorMessageKey.NoHtmlFound ->
                stringResource(R.string.error_no_html_found, key.folderName)
            is ErrorMessageKey.UnknownError ->
                stringResource(R.string.error_unknown, key.message)
            }
        }
    }

