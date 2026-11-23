package com.en1y.alias.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp

private val Default = Typography()

val Typography = Default.copy(
    bodyLarge = Default.bodyLarge.copy(fontSize = 18.sp, lineHeight = 26.sp),
    bodyMedium = Default.bodyMedium.copy(fontSize = 16.sp, lineHeight = 22.sp),
    labelLarge = Default.labelLarge.copy(fontSize = 17.sp, lineHeight = 22.sp),
    titleMedium = Default.titleMedium.copy(fontSize = 20.sp, lineHeight = 26.sp),
    headlineSmall = Default.headlineSmall.copy(fontSize = 28.sp, lineHeight = 34.sp),
    headlineMedium = Default.headlineMedium.copy(fontSize = 34.sp, lineHeight = 40.sp),
    headlineLarge = Default.headlineLarge.copy(fontSize = 40.sp, lineHeight = 46.sp),
)
