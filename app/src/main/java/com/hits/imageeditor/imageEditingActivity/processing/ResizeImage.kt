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

        val interpolateMethod =
            if (scaleFactor > 1.0f) ::bilinearInterpolate else ::trilinearInterpolate

        for (x in 0 until newWidth) {
            for (y in 0 until newHeight) {
                val originX = (x * xRatio).toInt()
                val originY = (y * yRatio).toInt()
                val xDiff = (x * xRatio) - originX
                val yDiff = (y * yRatio) - originY

                val topLeftPixel = inputBitmap.getPixel(originX, originY)
                val topRightPixel = inputBitmap.getPixel(originX + 1, originY)
                val bottomLeftPixel = inputBitmap.getPixel(originX, originY + 1)
                val bottomRightPixel = inputBitmap.getPixel(originX + 1, originY + 1)

                val interpolatedPixel = interpolateMethod(
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
        val top = topLeft + (xDiff * (topRight - topLeft)).toInt()
        val bottom = bottomLeft + (xDiff * (bottomRight - bottomLeft)).toInt()
        return top + (yDiff * (bottom - top)).toInt()
    }

    private fun trilinearInterpolate(
        topLeft: Int,
        topRight: Int,
        bottomLeft: Int,
        bottomRight: Int,
        xDiff: Float,
        yDiff: Float
    ): Int {
        val top = bilinearInterpolate(
            topLeft and 0xFF0000 shr 16,
            topRight and 0xFF0000 shr 16,
            bottomLeft and 0xFF0000 shr 16,
            bottomRight and 0xFF0000 shr 16,
            xDiff,
            yDiff
        )
        val bottom = bilinearInterpolate(
            topLeft and 0x00FF00 shr 8,
            topRight and 0x00FF00 shr 8,
            bottomLeft and 0x00FF00 shr 8,
            bottomRight and 0x00FF00 shr 8,
            xDiff,
            yDiff
        )
        val blue = bilinearInterpolate(
            topLeft and 0x0000FF,
            topRight and 0x0000FF,
            bottomLeft and 0x0000FF,
            bottomRight and 0x0000FF,
            xDiff,
            yDiff
        )
        return 0xFF000000.toInt() or (top shl 16) or (bottom shl 8) or blue
    }
}