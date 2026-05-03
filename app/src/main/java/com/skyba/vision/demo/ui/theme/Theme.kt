package com.skyba.vision.demo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AppWhite,
    onPrimary = AppBlack,
    background = AppDarkBackground,
    onBackground = AppWhite,
    surface = AppDarkSurface,
    onSurface = AppWhite,
    error = AppError,
    onError = AppWhite
)

private val LightColorScheme = lightColorScheme(
    primary = AppBlack,
    onPrimary = AppWhite,
    background = AppLightBackground,
    onBackground = AppBlack,
    surface = AppLightSurface,
    onSurface = AppBlack,
    error = AppError,
    onError = AppBlack
)

@Composable
fun SKYbaVisionDemoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}