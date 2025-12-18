package com.mobile.memorise.util

// File: com.mobile.memorise.util.DateUtils.kt

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun formatIsoDate(isoString: String): String {
    return try {
        // Format input dari server
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        // Format output untuk UI (e.g., Oct 24, 2023)
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

        val date = inputFormat.parse(isoString)
        outputFormat.format(date ?: return isoString)
    } catch (e: Exception) {
        isoString // Return original string jika parsing gagal
    }
}