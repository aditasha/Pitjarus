package com.aditasha.pitjarus

import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.IOException

object Helper {
    private fun getExifInterface(uri: Uri): ExifInterface? {
        try {
            val path: String = uri.toString()
            if (path.startsWith("file://")) {
                return ExifInterface(path)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun getExifAngle(uri: Uri?): Float {
        return try {
            val exifInterface = getExifInterface(uri!!) ?: return -1f
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                ExifInterface.ORIENTATION_NORMAL -> 0f
                ExifInterface.ORIENTATION_UNDEFINED -> -1f
                else -> -1f
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            -1f
        }
    }

    fun rotateBitmap(bitmap: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }
}