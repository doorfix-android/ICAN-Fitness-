package com.ican.fitness.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val CharcoalBg = Color(0xFF141514)
val GraphiteSurface = Color(0xFF1E201E)
val GraphiteElevated = Color(0xFF282B28)
val WarmCream = Color(0xFFF5F2EB)
val WarmCreamMuted = Color(0xFFC8C3B8)
val OliveAccent = Color(0xFF7B946B)
val OliveContainer = Color(0xFF2D3827)
val TerracottaAlert = Color(0xFFD46A5E)
val AmberHighlight = Color(0xFFD6A85A)

private val IcanDarkColorScheme = darkColorScheme(
    primary = OliveAccent,
    onPrimary = CharcoalBg,
    primaryContainer = OliveContainer,
    onPrimaryContainer = WarmCream,
    secondary = WarmCreamMuted,
    onSecondary = CharcoalBg,
    background = CharcoalBg,
    onBackground = WarmCream,
    surface = GraphiteSurface,
    onSurface = WarmCream,
    surfaceVariant = GraphiteElevated,
    onSurfaceVariant = WarmCreamMuted,
    error = TerracottaAlert,
    onError = CharcoalBg
)

val IcanTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

@Composable
fun IcanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IcanDarkColorScheme,
        typography = IcanTypography,
        content = content
    )
}
