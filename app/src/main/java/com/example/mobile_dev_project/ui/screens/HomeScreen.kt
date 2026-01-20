package com.example.mobile_dev_project.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mobile_dev_project.R
import com.example.mobile_dev_project.data.Book
import com.example.mobile_dev_project.ui.theme.Deemphasize
import com.example.mobile_dev_project.vm.AppViewModel
import java.io.File
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/**
 * Main home screen of the app.
 */
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onAddBookClick: () -> Unit,
    onBookClick: (Book) -> Unit,
    context: Context = LocalContext.current

) {
    val books by viewModel.books.collectAsState()
    val baseDir = File(context.filesDir, "textbooks")
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(dimensionResource(id = R.dimen.spacing_default))
            .testTag("home_screen")

    ) {
        AppLogo()
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_medium)))
        AboutApp()
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_medium)))
        DeleteLibraryButton(
            onDeleteClick = {viewModel.clearTextbooksDirectory(baseDir) }
        )
        BookShelf(
            books = books,
            onAddBookClick = onAddBookClick,
            onBookClick = onBookClick
        )
    }
}
/**
 * Displays the app logo centered.
 */
@Composable
fun AppLogo() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = stringResource(R.string.cd_app_logo),
            modifier = Modifier.size(dimensionResource(id = R.dimen.home_logo_size))
        )
    }
}

/**
 * Displays a short description of the app.
 */
@Composable
fun AboutApp(){
    Text(
        text = stringResource(R.string.home_about),
        textAlign = TextAlign.Center,
        fontSize = 16.sp,
        modifier = Modifier.fillMaxWidth()
    )
}
/**
 * Displays a bookshelf section with a grid of books.
 *
 * @param books List of books in the library
 * @param onAddBookClick Lambda executed when add button is clicked
 * @param onBookClick Lambda executed when a book is clicked
 */
@Composable
fun BookShelf(
    books: List<Book>,
    onAddBookClick: () -> Unit,
    onBookClick: (Book) -> Unit) {
    Text(
        text = stringResource(R.string.home_section_library),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.spacing_small))
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.grid_max_height)), // Set max height
        contentPadding = PaddingValues(dimensionResource(id = R.dimen.spacing_small)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_small)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_small))
    ) {
        items(books) { book ->
            BookItem(book = book, onBookClick = onBookClick)
        }
    }

    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_medium)))
    AddBookButton(onAddBookClick)
}
/**
 * Button to add a new book to the library.
 */
@Composable
fun AddBookButton(onAddBookClick:() -> Unit){
    Button(
        onClick = onAddBookClick,
        modifier = Modifier.fillMaxWidth(),
        shape= MaterialTheme.shapes.large
    ) {
        Text(stringResource(R.string.home_add_new_book))
    }
}
/**
 * Button to delete the entire library.
 */
@Composable
fun DeleteLibraryButton(onDeleteClick: () -> Unit){
    Button(
        onClick = onDeleteClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Text(stringResource(R.string.delete_library))
    }
}
/**
 * Individual book item in the grid.
 */
@Composable
fun BookItem(book: Book, onBookClick: (Book) -> Unit) {
    Column(
        modifier = Modifier
            .padding(dimensionResource(id = R.dimen.spacing_tiny))
            .clip(MaterialTheme.shapes.medium)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = dimensionResource(id = R.dimen.border_width),
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_small))
            )
            .clickable { onBookClick(book) }
            .padding(dimensionResource(id = R.dimen.spacing_small)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BookCover(book)
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_medium)))
        BookTitle(book)
        LastAccessedText(book)
    }
}
/**
 * Displays the book cover using Coil.
 */
@Composable
fun BookCover(book: Book){
    val cover = book.coverImagePath
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(if (cover != null && File(cover).exists()) File(cover) else R.drawable.book_app_img)
            .crossfade(true)
            .build(),
        contentDescription = book.title,
        placeholder = painterResource(R.drawable.book_app_img),
        error = painterResource(R.drawable.book_app_img),
        modifier = Modifier
            .size(
                width = dimensionResource(id = R.dimen.home_book_cover_width),
                height = dimensionResource(id = R.dimen.home_book_cover_height)
            )
            .clip(MaterialTheme.shapes.medium)
    )
}
/**
 * Displays the book title.
 */
@Composable
fun BookTitle(book: Book){
    Text(
        text = book.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * Displays the last accessed date for the book or a "never opened" message.
 */
@Composable
fun LastAccessedText(book: Book){
    //https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/let.html
    //https://docs.oracle.com/javase/8/docs/api/java/text/DateFormat.html?utm_source=chatgpt.com
    val lastAccessedText = book.lastAccessedDate?.let {
        val formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
        stringResource(R.string.home_last_accessed, formatter.format(Date(it)))
    } ?: stringResource(R.string.home_never_opened)

    val variant = MaterialTheme.colorScheme.onSurfaceVariant
    Deemphasize {
        Text(
            text = lastAccessedText,
            style = MaterialTheme.typography.bodySmall,
            color = if (book.lastAccessedDate == null) variant.copy(alpha = 0.8f) else variant
        )
    }
}
