package com.example.songbook.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.IOException
import java.util.UUID

object FileUtils {

    fun copyPdfToInternalStorage(context: Context, uri: Uri): String {
        val pdfDir = File(context.filesDir, "pdfs")
        if (!pdfDir.exists()) {
            pdfDir.mkdirs()
        }
        val targetFile = File(pdfDir, "${UUID.randomUUID()}.pdf")
        context.contentResolver.openInputStream(uri)?.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IOException("Unable to read selected file")
        return targetFile.absolutePath
    }

    fun fileNameFromUri(context: Context, uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) {
                return it.getString(nameIndex)
            }
        }
        return uri.lastPathSegment ?: "selected.pdf"
    }

    fun deleteInternalFile(path: String?) {
        if (path.isNullOrBlank()) return
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
    }
}
