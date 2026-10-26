package com.digitalbeavertz.ruqya.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digitalbeavertz.ruqya.R
import com.digitalbeavertz.ruqya.data.ArabicFont
import com.digitalbeavertz.ruqya.data.ThemeMode

// Calm and flat: a soft paper canvas, white cards and one deep green. No gradients, no emoji.

@Immutable
data class RColors(
    val canvas: Color,
    val card: Color,
    val soft: Color,     // chips, tracks, quiet fills
    val ink: Color,      // headings and main text
    val muted: Color,    // secondary text
    val line: Color,
    val green: Color,    // the brand colour: buttons, accents
    val onGreen: Color,
    val greenSoft: Color, // tinted fills (done states, source chips)
    val onGreenSoft: Color,
    val gold: Color,     // verse numbers and small ornaments
    val warn: Color,
    val dark: Boolean,
)

private val Light = RColors(
    canvas = Color(0xFFF6F5F0), card = Color(0xFFFFFFFF), soft = Color(0xFFECEBE4),
    ink = Color(0xFF14201A), muted = Color(0xFF65706A), line = Color(0xFFE3E2DA),
    green = Color(0xFF14532D), onGreen = Color.White,
    greenSoft = Color(0xFFDCEBE1), onGreenSoft = Color(0xFF14532D),
    gold = Color(0xFF9A7B2F), warn = Color(0xFFB42318), dark = false,
)

private val Dark = RColors(
    canvas = Color(0xFF0C1310), card = Color(0xFF151E19), soft = Color(0xFF1F2A24),
    ink = Color(0xFFEDF2EE), muted = Color(0xFF98A69E), line = Color(0xFF26322B),
    green = Color(0xFF3E9A68), onGreen = Color.White,
    greenSoft = Color(0xFF1C3427), onGreenSoft = Color(0xFFB9DCC6),
    gold = Color(0xFFD2B46A), warn = Color(0xFFFF7A6E), dark = true,
)

val LocalR = staticCompositionLocalOf { Light }

object RT {
    val colors: RColors @Composable get() = LocalR.current
}

/** Plus Jakarta Sans (OFL) for the interface. */
val Jakarta = FontFamily(
    Font(R.font.jakarta_regular, FontWeight.Normal),
    Font(R.font.jakarta_medium, FontWeight.Medium),
    Font(R.font.jakarta_semibold, FontWeight.SemiBold),
    Font(R.font.jakarta_bold, FontWeight.Bold),
    Font(R.font.jakarta_extrabold, FontWeight.ExtraBold),
)

/** The Arabic fonts the person can choose from. */
fun arabicFamily(f: ArabicFont): FontFamily = FontFamily(
    Font(
        when (f) {
            ArabicFont.MADINAH -> R.font.arabic_madinah
            ArabicFont.AMIRI -> R.font.arabic_amiri
            ArabicFont.SCHEHERAZADE -> R.font.arabic_scheherazade
            ArabicFont.NASKH -> R.font.arabic_naskh
        },
    ),
)

private val base = Typography()

private val RTypography = Typography(
    displaySmall = base.displaySmall.copy(fontFamily = Jakarta, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.8).sp),
    headlineLarge = base.headlineLarge.copy(fontFamily = Jakarta, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.6).sp),
    headlineSmall = base.headlineSmall.copy(fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.3).sp),
    titleLarge = base.titleLarge.copy(fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 19.sp, letterSpacing = (-0.2).sp),
    titleMedium = base.titleMedium.copy(fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 16.sp),
    titleSmall = base.titleSmall.copy(fontFamily = Jakarta, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    bodyLarge = base.bodyLarge.copy(fontFamily = Jakarta, fontSize = 16.sp, lineHeight = 25.sp),
    bodyMedium = base.bodyMedium.copy(fontFamily = Jakarta, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = base.bodySmall.copy(fontFamily = Jakarta, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = base.labelLarge.copy(fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 15.sp),
    labelMedium = base.labelMedium.copy(fontFamily = Jakarta, fontWeight = FontWeight.SemiBold),
    labelSmall = base.labelSmall.copy(fontFamily = Jakarta, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp),
)

@Composable
fun isDark(mode: ThemeMode) = when (mode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Composable
fun RuqyaTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val c = if (isDark(mode)) Dark else Light
    val scheme = if (c.dark) darkColorScheme(
        primary = c.green, onPrimary = c.onGreen, secondary = c.ink, onSecondary = c.canvas,
        background = c.canvas, onBackground = c.ink, surface = c.card, onSurface = c.ink,
        surfaceVariant = c.soft, onSurfaceVariant = c.muted, outline = c.line, outlineVariant = c.line,
        surfaceContainerHigh = c.card, surfaceContainer = c.card, surfaceContainerLow = c.card, surfaceContainerHighest = c.soft,
        primaryContainer = c.greenSoft, onPrimaryContainer = c.onGreenSoft, error = c.warn,
    ) else lightColorScheme(
        primary = c.green, onPrimary = c.onGreen, secondary = c.ink, onSecondary = c.canvas,
        background = c.canvas, onBackground = c.ink, surface = c.card, onSurface = c.ink,
        surfaceVariant = c.soft, onSurfaceVariant = c.muted, outline = c.line, outlineVariant = c.line,
        surfaceContainerHigh = c.card, surfaceContainer = c.card, surfaceContainerLow = c.card, surfaceContainerHighest = c.soft,
        primaryContainer = c.greenSoft, onPrimaryContainer = c.onGreenSoft, error = c.warn,
    )
    CompositionLocalProvider(LocalR provides c) {
        MaterialTheme(
            colorScheme = scheme,
            typography = RTypography,
            shapes = Shapes(
                small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(18.dp),
                large = RoundedCornerShape(24.dp), extraLarge = RoundedCornerShape(28.dp),
            ),
            content = content,
        )
    }
}
