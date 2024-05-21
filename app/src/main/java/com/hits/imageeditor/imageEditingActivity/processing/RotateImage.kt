package com.hits.imageeditor.imageEditingActivity.processing

import android.graphics.Bitmap
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class RotateImage {
    fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap {
        val radians = Math.toRadians(degrees.toDouble())
        val cos = cos(radians)
        val sin = sin(radians)

        val width = bitmap.width
        val height = bitmap.height
        val newWidth = (abs(width * cos) + abs(height * sin)).toInt()
        val newHeight = (abs(width * sin) + abs(height * cos)).toInt()

        val newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

        val px = width / 2
        val py = height / 2
        val newPx = newWidth / 2
        val newPy = newHeight / 2

        val originalPixels = IntArray(width * height)
        bitmap.getPixels(originalPixels, 0, width, 0, 0, width, height)

        val newPixels = IntArray(newWidth * newHeight)

        for (x in 0 until newWidth) {
            for (y in 0 until newHeight) {
                val ax = x - newPx
                val ay = y - newPy
                val srcX = (ax * cos - ay * sin + px).toInt()
                val srcY = (ax * sin + ay * cos + py).toInt()

                if (srcX in 0 until width && srcY in 0 until height) {
                    newPixels[y * newWidth + x] = originalPixels[srcY * width + srcX]
                } else {
                    newPixels[y * newWidth + x] = android.graphics.Color.TRANSPARENT
                }
            }
        }

        newBitmap.setPixels(newPixels, 0, newWidth, 0, 0, newWidth, newHeight)

        val rotatedAndScaledBitmap = Bitmap.createScaledBitmap(
            newBitmap, width, height, true
        )

        return rotatedAndScaledBitmap
    }
}