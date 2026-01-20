package com.example.mobile_dev_project.ui

import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass as calculateWindowSizeClass1
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.mobile_dev_project.ui.navigation.AdaptiveNavigationApp
import com.example.mobile_dev_project.ui.navigation.NavigationHost
import com.example.mobile_dev_project.vm.AppViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun BookApp(modifier: Modifier = Modifier) {
    val viewModel: AppViewModel = hiltViewModel()
    val navController = rememberNavController()
    val context = LocalContext.current

    val windowSizeClass = calculateWindowSizeClass1(context as Activity)
    val windowWidthSizeClass = windowSizeClass.widthSizeClass
    Scaffold(
        modifier = modifier,
        bottomBar = {
            // Hide bottom navigation bar when in immersive mode
            if (!viewModel.immersive) {
                AdaptiveNavigationApp(
                    navController = navController,
                    hasBookSelected = viewModel.currentBookId != null,
                    windowSizeClass = windowWidthSizeClass,
                    modifier = Modifier.padding(),
                    appViewModel = viewModel
                )
            }
        }
    ) { innerPadding ->
        NavigationHost(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            appViewModel = viewModel
        )
    }
}
