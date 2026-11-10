package com.edumind.ai.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

/** Maximum document size accepted for upload (20 MB). */
const val MAX_DOCUMENT_BYTES: Long = 20L * 1024L * 1024L

/** Maximum image size accepted for analysis (10 MB). */
const val MAX_IMAGE_BYTES: Long = 10L * 1024L * 1024L

val SUPPORTED_DOCUMENT_EXTENSIONS = setOf(
    "pdf", "doc", "docx", "txt", "ppt", "pptx", "jpg", "jpeg", "png", "webp",
)

data class PickedFile(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val mimeType: String?,
) {
    val extension: String get() = name.substringAfterLast('.', "").lowercase()
    val sizeText: String
        get() = when {
            sizeBytes >= 1024 * 1024 -> "%.1f MB".format(sizeBytes / (1024.0 * 1024.0))
            sizeBytes >= 1024 -> "%.0f KB".format(sizeBytes / 1024.0)
            else -> "$sizeBytes B"
        }
}

fun Context.fileInfo(uri: Uri): PickedFile {
    var name = "file"
    var size = 0L
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst()) {
            if (nameIndex >= 0) name = cursor.getString(nameIndex) ?: name
            if (sizeIndex >= 0) size = cursor.getLong(sizeIndex)
        }
    }
    return PickedFile(uri, name, size, contentResolver.getType(uri))
}

fun Context.readBytes(uri: Uri): ByteArray =
    contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
