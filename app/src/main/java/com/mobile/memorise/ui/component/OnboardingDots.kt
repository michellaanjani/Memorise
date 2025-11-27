package com.mobile.memorise.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DotSmall() {
    Box(
        modifier = Modifier
            .size(5.dp)
            .background(Color(0xFFD9D9D9), RoundedCornerShape(10.dp))
    )
}

@Composable
fun DotActive() {
    Box(
        modifier = Modifier
            .width(22.dp)
            .height(5.dp)
            .background(Color(0xFF0C3DF4), RoundedCornerShape(10.dp))
    )
}
