package com.hits.imageeditor.imageEditingActivity.processing

import android.graphics.Bitmap

class ResizeImage {
    fun scaleImage(inputBitmap: Bitmap, scaleFactor: Float): Bitmap {
        val width = inputBitmap.width
        val height = inputBitmap.height
        val newWidth = (width * scaleFactor).toInt()
        val newHeight = (height * scaleFactor).toInt()

        val outputBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

        val xRatio = width.toFloat() / newWidth.toFloat()
        val yRatio = height.toFloat() / newHeight.toFloat()

        val interpolationFunction = if (scaleFactor > 1.0f) ::bilinearInterpolate else ::trilinearInterpolate

        for (y in 0 until newHeight) {
            val originY = (y * yRatio).toInt().coerceIn(0, height - 1)
            val yDiff = (y * yRatio) - originY
            for (x in 0 until newWidth) {
                val originX = (x * xRatio).toInt().coerceIn(0, width - 1)
                val xDiff = (x * xRatio) - originX

                val topLeftPixel = inputBitmap.getPixel(originX, originY)
                val topRightPixel = inputBitmap.getPixel((originX + 1).coerceAtMost(width - 1), originY)
                val bottomLeftPixel = inputBitmap.getPixel(originX, (originY + 1).coerceAtMost(height - 1))
                val bottomRightPixel = inputBitmap.getPixel((originX + 1).coerceAtMost(width - 1), (originY + 1).coerceAtMost(height - 1))

                val interpolatedPixel = interpolationFunction(
                    topLeftPixel,
                    topRightPixel,
                    bottomLeftPixel,
                    bottomRightPixel,
                    xDiff,
                    yDiff
                )

                outputBitmap.setPixel(x, y, interpolatedPixel)
            }
        }

        return outputBitmap
    }

    private fun bilinearInterpolate(
        topLeft: Int,
        topRight: Int,
        bottomLeft: Int,
        bottomRight: Int,
        xDiff: Float,
        yDiff: Float
    ): Int {
        val inverseXDiff = 1.0f - xDiff
        val inverseYDiff = 1.0f - yDiff

        val a = ((topLeft shr 24 and 0xff) * inverseXDiff + (topRight shr 24 and 0xff) * xDiff) * inverseYDiff +
                ((bottomLeft shr 24 and 0xff) * inverseXDiff + (bottomRight shr 24 and 0xff) * xDiff) * yDiff
        val r = ((topLeft shr 16 and 0xff) * inverseXDiff + (topRight shr 16 and 0xff) * xDiff) * inverseYDiff +
                ((bottomLeft shr 16 and 0xff) * inverseXDiff + (bottomRight shr 16 and 0xff) * xDiff) * yDiff
        val g = ((topLeft shr 8 and 0xff) * inverseXDiff + (topRight shr 8 and 0xff) * xDiff) * inverseYDiff +
                ((bottomLeft shr 8 and 0xff) * inverseXDiff + (bottomRight shr 8 and 0xff) * xDiff) * yDiff
        val b = ((topLeft and 0xff) * inverseXDiff + (topRight and 0xff) * xDiff) * inverseYDiff +
                ((bottomLeft and 0xff) * inverseXDiff + (bottomRight and 0xff) * xDiff) * yDiff

        return (a.toInt() shl 24) or (r.toInt() shl 16) or (g.toInt() shl 8) or b.toInt()
    }

    private fun trilinearInterpolate(
        topLeft: Int,
        topRight: Int,
        bottomLeft: Int,
        bottomRight: Int,
        xDiff: Float,
        yDiff: Float
    ): Int {
        val inverseXDiff = 1.0f - xDiff
        val inverseYDiff = 1.0f - yDiff


        val a = ((topLeft shr 24 and 0xff) * inverseXDiff + (topRight shr 24 and 0xff) * xDiff) * inverseYDiff +
                ((bottomLeft shr 24 and 0xff) * inverseXDiff + (bottomRight shr 24 and 0xff) * xDiff) * yDiff
        val r = ((topLeft shr 16 and 0xff) * inverseXDiff + (topRight shr 16 and 0xff) * xDiff) * inverseYDiff +
                ((bottomLeft shr 16 and 0xff) * inverseXDiff + (bottomRight shr 16 and 0xff) * xDiff) * yDiff
        val g = ((topLeft shr 8 and 0xff) * inverseXDiff + (topRight shr 8 and 0xff) * xDiff) * inverseYDiff +
                ((bottomLeft shr 8 and 0xff) * inverseXDiff + (bottomRight shr 8 and 0xff) * xDiff) * yDiff
        val b = ((topLeft and 0xff) * inverseXDiff + (topRight and 0xff) * xDiff) * inverseYDiff +
                ((bottomLeft and 0xff) * inverseXDiff + (bottomRight and 0xff) * xDiff) * yDiff

        return (a.toInt() shl 24) or (r.toInt() shl 16) or (g.toInt() shl 8) or b.toInt()
    }
}

