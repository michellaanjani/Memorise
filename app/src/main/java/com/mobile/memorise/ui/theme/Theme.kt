package com.mobile.memorise.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Konfigurasi Warna untuk Mode Gelap (Menggunakan warna default Purple yang kamu berikan)
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

// Konfigurasi Warna untuk Mode Terang (Sesuai Desain Kamu)
private val LightColorScheme = lightColorScheme(
    // Warna Utama (Header Biru)
    primary = DeepBlue,
    onPrimary = White, // Warna teks di atas header (Putih)

    // Warna Kedua (Aksen teks "3 decks", Icon active)
    secondary = BrightBlue,
    onSecondary = White,

    // Warna Latar Belakang
    background = White,
    onBackground = TextBlack, // Warna teks hitam di background putih

    // Warna Kartu/Surface
    surface = White,
    onSurface = TextBlack,

    // Menggunakan BottomBarBackground untuk varian surface (opsional)
    surfaceVariant = BottomBarBackground
)

@Composable
fun MemoriseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // PENTING: Set ke FALSE agar warna tetap Biru sesuai desain,
    // tidak mengikuti warna wallpaper HP user (Android 12+)
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Mengubah warna Status Bar (Sinyal/Baterai) agar menyatu dengan Header Biru
            // Jika Light Mode -> Status Bar Biru Tua (DeepBlue)
            // Jika Dark Mode -> Biarkan default
            window.statusBarColor = if (darkTheme) Color.Black.toArgb() else DeepBlue.toArgb()

            // Mengatur warna ikon status bar (Jam/Baterai)
            // false = ikon putih (cocok untuk background biru tua)
            // true = ikon hitam
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Pastikan file Type.kt ada, atau hapus baris ini jika error
        content = content
    )
}