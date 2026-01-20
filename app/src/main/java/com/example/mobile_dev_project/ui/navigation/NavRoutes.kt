package com.example.mobile_dev_project.ui.navigation

sealed class NavRoutes (val route: String) {
    object Home : NavRoutes("home")
    object Download : NavRoutes("download")
    object About : NavRoutes("about")
    object TOC : NavRoutes("toc")
    object Reading : NavRoutes("reading")
    object Search : NavRoutes("search")
}