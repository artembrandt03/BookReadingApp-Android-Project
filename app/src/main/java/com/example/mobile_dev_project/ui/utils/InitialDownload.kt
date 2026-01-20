package com.example.mobile_dev_project.ui.utils

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.mobile_dev_project.R
import com.example.mobile_dev_project.vm.AppViewModel
import java.io.File

@SuppressLint("LocalContextResourcesRead")
@Composable
fun InitialBookDownload() {
    val viewModel: AppViewModel = hiltViewModel()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val baseDir = File(context.filesDir, "textbooks")
        if (!baseDir.exists() || baseDir.listFiles().isNullOrEmpty()) {
            val urls = context.resources.getStringArray(R.array.book_urls).toList()
            viewModel.downloadTextbooks(urls, baseDir)
        }
    }
}
