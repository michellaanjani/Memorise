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
    // Diset false agar warna konsisten sesuai brand (Biru/Memorise), tidak ikut wallpaper user
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

            // 1. STATUS BAR & NAVIGATION BAR TRANSPARAN (Standard Production Edge-to-Edge)
            // Kita set transparent agar background aplikasi bisa naik sampai ke belakang jam/baterai
            window.statusBarColor = Color(0x60424242).toArgb()
            window.navigationBarColor = Color(0x70424242).toArgb() // Nav bar bawah juga transparan

            // 2. MENGATUR KONTRAS ICON (Jam, Baterai, Sinyal)
            // Logic:
            // !darkTheme (Jika Light Mode) -> true  -> Icon jadi HITAM (karena background biasanya putih)
            // !darkTheme (Jika Dark Mode)  -> false -> Icon jadi PUTIH (karena background gelap)
            val insetsController = WindowCompat.getInsetsController(window, view)

            // CATATAN PENTING:
            // Jika di Light Mode header kamu warnanya BIRU TUA (DeepBlue),
            // maka kamu harus memaksa ini menjadi 'false' (Icon Putih).
            // Tapi jika background aplikasimu Putih, gunakan logic standar di bawah ini:
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}