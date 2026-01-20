package com.example.mobile_dev_project.ui.utils

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Enables or disables immersive mode (full screen)
 * When enabled: Hides status bar and navigation bars
 * When disabled: Shows system UI normally
 *
 * @param isEnabled Whether immersive mode should be active
 * @param content The composable content to display
 */
@Composable
fun ImmersiveMode(
    isEnabled: Boolean,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    
    DisposableEffect(isEnabled) {
        val window = (view.context as? Activity)?.window ?: return@DisposableEffect onDispose {}
        val insetsController = WindowCompat.getInsetsController(window, view)
        
        if (isEnabled) {
            // Hide system bars (status bar + navigation bar)
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = 
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            // Show system bars
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
        
        onDispose {
            // Always restore system bars when leaving the screen
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }
    
    content()
}

