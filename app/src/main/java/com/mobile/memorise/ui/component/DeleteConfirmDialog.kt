package com.mobile.memorise.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun DeleteConfirmDialog(
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        shape = RoundedCornerShape(18.dp),
        containerColor = Color.White,

        // HEADER — diperkecil jarak + teks dipertegas
        title = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Warning",
                    fontSize = 18.sp,
                    color = Color(0xFF222222)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "This action is irreversible.",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }
        },

        // Supaya card

        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, top = 4.dp),   // tombol lebih dekat ke teks
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // CANCEL
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE6E6E6),
                        contentColor = Color(0xFF1A1A1A)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .width(100.dp)
                        .height(36.dp)
                ) {
                    Text("Cancel", fontSize = 13.sp)
                }

                // DELETE
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFE2E4),
                        contentColor = Color(0xFFD32F2F)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .width(100.dp)
                        .height(36.dp)
                ) {
                    Text("Delete", fontSize = 13.sp)
                }
            }
        }
    )
}


