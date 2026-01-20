package com.example.mobile_dev_project.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.adaptivenavigationegcode.ui.utils.AdaptiveNavigationType
import com.example.mobile_dev_project.ui.screens.DownloadScreen
import com.example.mobile_dev_project.ui.screens.HomeScreen
import com.example.mobile_dev_project.ui.screens.ReadingScreen
import com.example.mobile_dev_project.ui.screens.SearchScreen
import com.example.mobile_dev_project.ui.screens.TableOfContentsScreen
import com.example.mobile_dev_project.vm.AppViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile_dev_project.vm.TTSViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * The NavigationHost is responsible for defining all the routes for app.
 *
 */
@Composable
fun NavigationHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    appViewModel: AppViewModel,
    ttsViewModel: TTSViewModel = hiltViewModel()
) {

    //Observe current route
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val lastRouteState = remember { mutableStateOf(currentRoute) }

    //When we leave the Reading route, stop TTS
    LaunchedEffect(currentRoute) {
        val lastRoute = lastRouteState.value
        if (lastRoute == NavRoutes.Reading.route && currentRoute != NavRoutes.Reading.route) {
            ttsViewModel.stop()
        }
        lastRouteState.value = currentRoute
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Home.route,
        modifier = modifier
    ) {

        composable(NavRoutes.Home.route) {
            HomeScreen(
                viewModel = appViewModel,
                onAddBookClick = { navController.navigate(NavRoutes.Download.route) },
                onBookClick = { book ->
                    appViewModel.selectBook(book.id)
                    navController.navigate(NavRoutes.TOC.route)
                }
            )
        }

        composable(NavRoutes.Download.route) {
            DownloadScreen(viewModel = appViewModel)
        }

        composable(NavRoutes.Search.route) {
            SearchScreen(
                vm = appViewModel,
                onOpenReading = { navController.navigate(NavRoutes.Reading.route) }
            )
        }

        composable(NavRoutes.TOC.route) {
            TableOfContentsScreen(
                viewModel = appViewModel,
                onChapterClick = { chapterIndex ->
                    appViewModel.openChapter(chapterIndex)
                    navController.navigate(NavRoutes.Reading.route)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        composable(NavRoutes.Reading.route) {
            ReadingScreen(
                viewModel = appViewModel,
                ttsViewModel = ttsViewModel,   // use the shared VM
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * AdaptiveNavigationApp decides which navigation type to show
 * based on the window size (Compact -> Bottom Nav, Medium -> Navigation Rail, Expanded -> Drawer)
 * and wraps the NavigationHost inside the appropriate layout.
 */
@Composable
@ExperimentalMaterial3Api
fun AdaptiveNavigationApp(
    windowSizeClass: WindowWidthSizeClass,
    modifier: Modifier,
    hasBookSelected: Boolean,
    appViewModel: AppViewModel,
    navController: NavHostController,
    ttsViewModel: TTSViewModel = hiltViewModel()
) {
    val adaptiveNavigationType = when (windowSizeClass) {
        WindowWidthSizeClass.Compact -> AdaptiveNavigationType.BOTTOM_NAVIGATION
        WindowWidthSizeClass.Medium -> AdaptiveNavigationType.NAVIGATION_RAIL
        else -> AdaptiveNavigationType.PERMANENT_NAVIGATION_DRAWER
    }

    Scaffold(
        bottomBar = {
            if (adaptiveNavigationType == AdaptiveNavigationType.BOTTOM_NAVIGATION) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        Row(modifier = modifier.padding(paddingValues)) {
            if (adaptiveNavigationType == AdaptiveNavigationType.PERMANENT_NAVIGATION_DRAWER) {
                PermanentNavigationDrawerComponent(appViewModel, navController, ttsViewModel)
            }
            if (adaptiveNavigationType == AdaptiveNavigationType.NAVIGATION_RAIL) {
                NavigationRailComponent(navController = navController)
            }
                NavigationHost(
                    navController = navController,
                    modifier = Modifier,
                    appViewModel = appViewModel,
                    ttsViewModel = ttsViewModel
                )
            }
        }
    }

/**
 * Bottom navigation bar for compact screens
 */
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavBarItems.BarItems.forEach { navItem ->
            NavigationBarItem(
                modifier = Modifier.testTag("nav_${navItem.route}"),
                selected = currentRoute == navItem.route,
                onClick = {
                    navController.navigate(navItem.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(imageVector = navItem.image, contentDescription = navItem.title)
                },
                label = {
                    Text(text = navItem.title)
                },
            )
        }
    }
}
/**
 * Navigation rail for medium screens
 */
@Composable
fun NavigationRailComponent(navController: NavController) {
    NavigationRail {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoutes = backStackEntry?.destination?.route
        NavBarItems.BarItems.forEach { navItem ->
            NavigationRailItem(
                selected = currentRoutes == navItem.route,
                onClick = { navController.navigate(navItem.route) },
                icon = {
                    Icon(navItem.image, contentDescription = navItem.title)
                },
                label = {
                    Text(text = navItem.title)
                }
            )
        }
    }
}

/**
 * Permanent drawer for large/expanded screens
 */
@Composable
fun PermanentNavigationDrawerComponent(
    appViewModel: AppViewModel,
    navController: NavHostController,
    ttsViewModel: TTSViewModel
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoutes = backStackEntry?.destination?.route
    PermanentNavigationDrawer(
        drawerContent = {
            PermanentDrawerSheet {
                Column {
                    NavBarItems.BarItems.forEach { navItem ->
                        NavigationDrawerItem(
                            selected = currentRoutes == navItem.route,
                            onClick = {
                                navController.navigate(navItem.route)
                            },
                            icon = {
                                Icon(navItem.image, contentDescription = navItem.title)
                            },
                            label = { Text(text = navItem.title) }
                        )
                    }
                }
            } },
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                //The call to NavigationHost is necessary to display the screen based on the route
                NavigationHost(
                    navController = navController,
                    modifier = Modifier,
                    appViewModel = appViewModel,
                    ttsViewModel = ttsViewModel
                )
            }
        }
    )
}



