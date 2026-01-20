    package com.example.mobile_dev_project.data

    sealed class DownloadState {
        object Idle : DownloadState()
        object Loading : DownloadState()
        data class Progress(val messageKey: ProgressMessageKey) : DownloadState()
        object Success : DownloadState()
        data class Error(val errorKey: ErrorMessageKey) : DownloadState()
    }
    sealed class ProgressMessageKey {
        object StartingDownloads : ProgressMessageKey()
        data class BookAlreadyInLibrary(val title: String) : ProgressMessageKey()
        data class BookAlreadyExists(val title: String) : ProgressMessageKey()
        data class Downloading(val fileName: String) : ProgressMessageKey()
        data class Unzipping(val fileName: String) : ProgressMessageKey()
        data class Parsing(val folderName: String) : ProgressMessageKey()
        data class Completed(val folderName: String) : ProgressMessageKey()
    }

    // Message keys for Error states
    sealed class ErrorMessageKey {
        data class DownloadFailed(val fileName: String) : ErrorMessageKey()
        data class ParseFailed(val folderName: String) : ErrorMessageKey()
        data class NoHtmlFound(val folderName: String) : ErrorMessageKey()
        data class UnknownError(val message: String) : ErrorMessageKey()
    }