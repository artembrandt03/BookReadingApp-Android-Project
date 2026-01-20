package com.example.mobile_dev_project.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Home
import com.example.mobile_dev_project.data.BarItem
import androidx.compose.material.icons.filled.List

object NavBarItems {
    val  BarItems = listOf(
        BarItem(
            title = "Home",
            image = Icons.Filled.Home,
            route = NavRoutes.Home.route
        ),
        BarItem(
            title = "Download",
            image = Icons.Filled.Download,
            route = NavRoutes.Download.route
        ),
        BarItem(
              title = "TOC",
              image = Icons.Filled.List,
              route = NavRoutes.TOC.route
        ),
        BarItem(
              title = "Search",
              image = Icons.Filled.Search,
              route = NavRoutes.Search.route
        )
    )
}
