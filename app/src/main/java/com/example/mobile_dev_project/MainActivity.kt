package com.example.mobile_dev_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.ui.res.dimensionResource
import com.example.mobile_dev_project.ui.BookApp
import com.example.mobile_dev_project.ui.theme.MobileDevProjectTheme
import com.example.mobile_dev_project.ui.utils.InitialBookDownload
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileDevProjectTheme(
                darkTheme = true,
                dynamicColor = false
            )
            {
                Surface(tonalElevation = dimensionResource(id = R.dimen.spacing_small)) {
                    InitialBookDownload()
                    BookApp()
                }

            }
        }
    }
}
