package com.example.mobile_dev_project.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.mobile_dev_project.R

// Font families built from /res/font
val Quintessential = FontFamily(
    Font(R.font.quintessential_regular, FontWeight.Normal)
)

val PlayfairDisplay = FontFamily(
    Font(R.font.playfair_display_regular, FontWeight.Normal),
    Font(R.font.playfair_display_bold, FontWeight.Bold),
)

val Roboto = FontFamily(
    Font(R.font.roboto_regular, FontWeight.Normal)
)

//
//For display -> Playfair Display
//For headlines & titles -> Quintessential
//body & labels -> Roboto
val AppTypography = Typography(
    displayLarge  = Typography().displayLarge.copy(fontFamily = PlayfairDisplay),
    displayMedium = Typography().displayMedium.copy(fontFamily = PlayfairDisplay),
    displaySmall  = Typography().displaySmall.copy(fontFamily = PlayfairDisplay),

    headlineLarge  = Typography().headlineLarge.copy(fontFamily = Quintessential),
    headlineMedium = Typography().headlineMedium.copy(fontFamily = Quintessential),
    headlineSmall  = Typography().headlineSmall.copy(fontFamily = Quintessential),

    titleLarge  = Typography().titleLarge.copy(fontFamily = Quintessential),
    titleMedium = Typography().titleMedium.copy(fontFamily = Quintessential),
    titleSmall  = Typography().titleSmall.copy(fontFamily = Quintessential),

    bodyLarge  = Typography().bodyLarge.copy(fontFamily = Roboto),
    bodyMedium = Typography().bodyMedium.copy(fontFamily = Roboto),
    bodySmall  = Typography().bodySmall.copy(fontFamily = Roboto),

    labelLarge  = Typography().labelLarge.copy(fontFamily = Roboto),
    labelMedium = Typography().labelMedium.copy(fontFamily = Roboto),
    labelSmall  = Typography().labelSmall.copy(fontFamily = Roboto),
)