package com.hits.imageeditor.imageEditingActivity.processing

import android.graphics.Bitmap
import android.graphics.Color

class FilterImage {
    private val filterImageService = FilterImageService()
     fun unsharpMasking(inputBitmap: Bitmap, radius: Int, amount: Float , threshold: Int): Bitmap {
        val blurredBitmap = gaussianBlur(inputBitmap, radius)

        val maskBitmap = Bitmap.createBitmap(inputBitmap.width, inputBitmap.height, Bitmap.Config.ARGB_8888)
        for (x in 0 until inputBitmap.width) {
            for (y in 0 until inputBitmap.height) {
                val originalPixel = inputBitmap.getPixel(x, y)
                val blurredPixel = blurredBitmap.getPixel(x, y)

                val r = Color.red(originalPixel) - Color.red(blurredPixel)
                val g = Color.green(originalPixel) - Color.green(blurredPixel)
                val b = Color.blue(originalPixel) - Color.blue(blurredPixel)

                val newR = Color.red(originalPixel) + (amount * r).toInt()
                val newG = Color.green(originalPixel) + (amount * g).toInt()
                val newB = Color.blue(originalPixel) + (amount * b).toInt()

                val finalR = if (Math.abs(r) > threshold) newR.coerceIn(0, 255) else Color.red(originalPixel)
                val finalG = if (Math.abs(g) > threshold) newG.coerceIn(0, 255) else Color.green(originalPixel)
                val finalB = if (Math.abs(b) > threshold) newB.coerceIn(0, 255) else Color.blue(originalPixel)

                maskBitmap.setPixel(x, y, Color.argb(Color.alpha(originalPixel), finalR, finalG, finalB))
            }
        }

        return maskBitmap
    }

    fun gaussianBlur(inputBitmap: Bitmap, radius: Int): Bitmap {
        val width = inputBitmap.width
        val height = inputBitmap.height
        val pixels = IntArray(width * height)
        inputBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val blurredBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val blurredPixels = IntArray(width * height)
        val weights = filterImageService.calculateWeights(radius.toDouble())

        for (y in 0 until height) {
            for (x in 0 until width) {
                var red = 0.0
                var green = 0.0
                var blue = 0.0
                var alpha = 0.0
                var weightSum = 0.0

                for (j in -radius..radius) {
                    for (i in -radius..radius) {
                        val currentX = x + i
                        val currentY = y + j

                        if (currentX in 0 until width && currentY in 0 until height) {
                            val currentPixel = pixels[currentY * width + currentX]
                            val weight = weights[j + radius][i + radius]

                            alpha += weight * (currentPixel ushr 24 and 0xFF)
                            red += weight * (currentPixel ushr 16 and 0xFF)
                            green += weight * (currentPixel ushr 8 and 0xFF)
                            blue += weight * (currentPixel and 0xFF)

                            weightSum += weight
                        }
                    }
                }

                alpha /= weightSum
                red /= weightSum
                green /= weightSum
                blue /= weightSum

                blurredPixels[y * width + x] = ((alpha.toInt() shl 24) or (red.toInt() shl 16) or (green.toInt() shl 8) or blue.toInt())
            }
        }

        blurredBitmap.setPixels(blurredPixels, 0, width, 0, 0, width, height)
        return blurredBitmap
    }




    fun mosaicFilter(bitmap: Bitmap, blockSize: Int): Bitmap {
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)

        for (x in 0 until bitmap.width step blockSize) {
            for (y in 0 until bitmap.height step blockSize) {
                val avgColor = filterImageService.getAverageColor(bitmap, x, y, blockSize)

                for (i in x until x + blockSize) {
                    for (j in y until y + blockSize) {
                        if (i < bitmap.width && j < bitmap.height) {
                            newBitmap.setPixel(i, j, avgColor)
                        }
                    }
                }
            }
        }
        return newBitmap
    }



    fun negativeFilter(bitmap: Bitmap): Bitmap {
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val color = bitmap.getPixel(x, y)

                val negativeColor = filterImageService.getNegativeColor(color)

                newBitmap.setPixel(x, y, negativeColor)
            }
        }
        return newBitmap
    }


}