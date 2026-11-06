package com.example.inboxzero.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MorandiBlueDark,
    secondary = MorandiGreen,
    tertiary = MorandiRed,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = TextPrimaryDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = MorandiBlue,
    secondary = MorandiGreen,
    tertiary = MorandiRed,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = SurfaceLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight
)

@Composable
fun InboxZeroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1.0f,
    boldText: Boolean = false,
    language: AppLanguage = AppLanguage.ZH, // ✨ 新增：全局语言状态
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // 根据当前选中的语言，瞬间切换对应的词典实例
    val appStrings = when (language) {
        AppLanguage.ZH -> ZhStrings
        AppLanguage.EN -> EnStrings
        AppLanguage.SV -> SvStrings
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    val adaptiveTypography = if (boldText) {
        Typography(
            titleLarge = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            bodyLarge = Typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            bodyMedium = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            labelLarge = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            labelMedium = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            labelSmall = Typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )
    } else {
        Typography
    }

    val currentDensity = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = currentDensity.density,
            fontScale = fontScale
        ),
        LocalAppStrings provides appStrings // ✨ 将多语言词典广播到全站任意角落
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = adaptiveTypography,
            content = content
        )
    }
}