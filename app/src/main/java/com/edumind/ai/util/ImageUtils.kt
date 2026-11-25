package com.edumind.ai.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import java.io.ByteArrayOutputStream

/** Bitmap helpers for the camera scanner: rotate, center-crop, JPEG encode. */
object ImageUtils {

    fun decode(bytes: ByteArray): Bitmap? =
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /** Center-crop to the given aspect ratio (width / height). */
    fun centerCrop(bitmap: Bitmap, aspectRatio: Float): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val current = w.toFloat() / h.toFloat()
        if (current > aspectRatio) {
            val newWidth = (h * aspectRatio).toInt()
            val x = (w - newWidth) / 2
            return Bitmap.createBitmap(bitmap, x, 0, newWidth, h)
        }
        val newHeight = (w / aspectRatio).toInt()
        val y = (h - newHeight) / 2
        return Bitmap.createBitmap(bitmap, 0, y, w, newHeight)
    }

    /** Scale down so the longest side is at most [maxSide] px (keeps uploads small). */
    fun scaleDown(bitmap: Bitmap, maxSide: Int = 1600): Bitmap {
        val longest = maxOf(bitmap.width, bitmap.height)
        if (longest <= maxSide) return bitmap
        val scale = maxSide.toFloat() / longest.toFloat()
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt(),
            (bitmap.height * scale).toInt(),
            true,
        )
    }

    fun toJpegBytes(bitmap: Bitmap, quality: Int = 85): ByteArray {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        return out.toByteArray()
    }

    fun toBase64(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.NO_WRAP)
}
