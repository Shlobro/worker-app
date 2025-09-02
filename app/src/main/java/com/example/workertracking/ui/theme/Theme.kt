package com.example.workertracking.ui.theme

import android.app.Activity
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
    primary = DeepBlue60,
    onPrimary = Color.White,
    secondary = BlueGray60,
    onSecondary = Color.White,
    tertiary = Amber60,
    onTertiary = Gray10,
    background = Gray10,
    onBackground = Gray80,
    surface = Gray20,
    onSurface = Gray80,
    surfaceVariant = Gray20,
    onSurfaceVariant = Gray60,
    surfaceContainer = Gray20,
    surfaceContainerHigh = Gray40,
    outline = Gray60,
    error = Error,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = DeepBlue40,
    onPrimary = Color.White,
    secondary = BlueGray40,
    onSecondary = Color.White,
    tertiary = Amber40,
    onTertiary = Color.White,
    background = Color.White,
    onBackground = Gray10,
    surface = Color.White,
    onSurface = Gray10,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = Gray40,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    outline = Gray60,
    error = Error,
    onError = Color.White,
    // Custom token colors for business logic
    primaryContainer = SurfaceContainer,
    onPrimaryContainer = DeepBlue20,
    secondaryContainer = Gray90,
    onSecondaryContainer = Gray20
)

@Composable
fun WorkerTrackingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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