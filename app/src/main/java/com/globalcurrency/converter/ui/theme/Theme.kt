package com.globalcurrency.converter.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Forced dark-only color scheme ────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary          = GoldPrimary,
    onPrimary        = OnDarkPrimary,
    primaryContainer = GoldDark,
    onPrimaryContainer = GoldLight,

    secondary        = SkyAccent,
    onSecondary      = DarkBackground,
    secondaryContainer = Color(0xFF002233),
    onSecondaryContainer = SkyAccent,

    tertiary         = EmeraldAccent,
    onTertiary       = DarkBackground,

    background       = DarkBackground,
    onBackground     = OnDarkBackground,

    surface          = DarkSurface,
    onSurface        = OnDarkBackground,
    surfaceVariant   = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSurface,

    outline          = DarkOutline,
    error            = CrimsonAccent,
    onError          = OnDarkBackground
)

@Composable
fun GlobalCurrencyTheme(content: @Composable () -> Unit) {
    // App is ALWAYS dark — no light mode support
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBackground.toArgb()
            window.navigationBarColor = DarkBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = AppTypography,
        content     = content
    )
}
