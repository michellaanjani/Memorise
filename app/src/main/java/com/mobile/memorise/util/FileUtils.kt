package com.mobile.memorise.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

/**
 * Mengubah URI (dari galeri/file picker) menjadi File fisik di cache
 * agar bisa di-upload menggunakan Retrofit.
 */
fun getFileFromUri(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null

        // Panggil fungsi getFileName yang didefinisikan di bawah
        val fileName = getFileName(context, uri) ?: "temp_file_${System.currentTimeMillis()}"

        // Buat file temporary di folder cache aplikasi
        val tempFile = File(context.cacheDir, fileName)
        val outputStream = FileOutputStream(tempFile)

        inputStream.copyTo(outputStream)

        inputStream.close()
        outputStream.close()
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Mendapatkan nama file asli dari URI.
 */
fun getFileName(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    return result
}