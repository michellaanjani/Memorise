package com.mobile.memorise

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.memorise.ui.theme.* // Pastikan import tema warna kamu

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FB)) // Background Abu sangat muda sesuai desain
            .padding(24.dp)
    ) {
        // --- 1. JUDUL HALAMAN ---
        Text(
            text = "Account",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1C24), // Hitam pekat / DeepBlue
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // --- 2. BAGIAN AVATAR & NAMA ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Box untuk Avatar + Icon Kamera
            Box(
                contentAlignment = Alignment.BottomEnd // Agar kamera di pojok kanan bawah
            ) {
                // Avatar Frame Lingkaran
                Box(
                    modifier = Modifier
                        .size(100.dp) // Ukuran Avatar
                        .clip(CircleShape) // Crop otomatis jadi lingkaran
                        .background(Color(0xFFBBDEFB)), // Warna placeholder biru muda
                    contentAlignment = Alignment.Center
                ) {
                    // Icon Placeholder (Ganti dengan Image() jika sudah ada foto asli)
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(60.dp),
                        tint = Color(0xFF1976D2) // Warna icon biru
                    )
                }

                // Tombol Kamera Kecil
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = 4.dp) // Sedikit geser keluar agar estetik
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF448AFF)) // Warna tombol kamera (Biru terang)
                        .clickable { /* TODO: Aksi ganti foto */ }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = "Change Photo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nama User
            Text(
                text = "Reynard Wijaya",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C24)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- 3. MENU EDIT ACCOUNT ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { /* TODO: Aksi Edit Account */ }
                .padding(vertical = 12.dp), // Area sentuh lebih luas
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Edit Account",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1C24)
            )

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF1A1C24)
            )
        }

        // --- 4. SPACER UNTUK DORONG TOMBOL LOGOUT KE BAWAH ---
        Spacer(modifier = Modifier.weight(1f))

        // --- 5. TOMBOL LOG OUT ---
        Button(
            onClick = { /* TODO: Aksi Log Out */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp)), // Efek bayangan
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White // Background Putih
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Log Out",
                color = Color.Red, // Teks Merah
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        // Jarak sedikit dari bawah layar
        Spacer(modifier = Modifier.height(24.dp))
    }
}