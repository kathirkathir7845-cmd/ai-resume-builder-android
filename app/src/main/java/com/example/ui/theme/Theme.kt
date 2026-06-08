package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ProfessionalDarkPrimary,
    secondary = ProfessionalDarkSecondary,
    tertiary = ProfessionalDarkTertiary,
    background = ProfessionalDarkBackground,
    surface = ProfessionalDarkSurface,
    onPrimary = ProfessionalDarkOnPrimary,
    onSecondary = ProfessionalDarkOnSecondary,
    onTertiary = Color.White,
    onBackground = ProfessionalDarkOnBackground,
    onSurface = ProfessionalDarkOnSurface,
    outline = ProfessionalDarkOutline
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ProfessionalPrimary,
    secondary = ProfessionalSecondary,
    tertiary = ProfessionalTertiary,
    background = ProfessionalBackground,
    surface = ProfessionalSurface,
    onPrimary = ProfessionalOnPrimary,
    onSecondary = ProfessionalOnSecondary,
    onTertiary = Color.White,
    onBackground = ProfessionalOnBackground,
    onSurface = ProfessionalOnSurface,
    outline = ProfessionalOutline
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disabling dynamic colors by default so our custom design theme acts as the primary aesthetic.
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
