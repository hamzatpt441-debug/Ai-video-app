package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PremiumGreenAccent,
    secondary = EmeraldPrimary,
    tertiary = MintLight,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = DarkBg,
    onSecondary = PureWhite,
    onBackground = PureWhite,
    onSurface = PureWhite
  )

private val LightColorScheme =
  lightColorScheme(
    primary = EditorialEmerald,
    secondary = EditorialLightAccent,
    tertiary = EditorialMutedText,
    background = EditorialBackground,
    surface = EditorialSurface,
    onPrimary = PureWhite,
    onSecondary = EditorialEmerald,
    onBackground = EditorialSlateText,
    onSurface = EditorialSlateText
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disabling dynamic colors to strictly enforce the requested premium green and white brand style
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
