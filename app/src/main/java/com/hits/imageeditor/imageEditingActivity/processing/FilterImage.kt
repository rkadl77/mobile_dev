package com.hits.imageeditor.imageEditingActivity.processing

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.math.min

class FilterImage {
    private val filterImageService = FilterImageService()
     fun unsharpMasking(inputBitmap: Bitmap, radius: Int = 2, amount: Float = 0.8f, threshold: Int = 2): Bitmap {
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

        runBlocking(Dispatchers.Default) {
            val chunkSize = height / 4
            val jobs = List(4) { index ->
                launch {
                    val startY = index * chunkSize
                    val endY = if (index == 3) height else startY + chunkSize
                    gaussProcessChunk(pixels, blurredPixels, width, height, startY, endY, radius, weights)
                }
            }
            jobs.joinAll()
        }

        blurredBitmap.setPixels(blurredPixels, 0, width, 0, 0, width, height)
        return blurredBitmap
    }

    suspend fun gaussProcessChunk(
        pixels: IntArray, blurredPixels: IntArray, width: Int, height: Int,
        startY: Int, endY: Int, radius: Int, weights: Array<DoubleArray>
    ) {
        withContext(Dispatchers.Default) {
            for (y in startY until endY) {
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
        }
    }



    fun mosaicFilter(bitmap: Bitmap, blockSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        runBlocking {
            val halfWidth = width / 2
            val halfHeight = height / 2

            val jobs = listOf(
                launch(Dispatchers.Default) { mosaicProcessChunk(bitmap, newBitmap, 0, 0, halfWidth, halfHeight, blockSize) },
                launch(Dispatchers.Default) { mosaicProcessChunk(bitmap, newBitmap, halfWidth, 0, width, halfHeight, blockSize) },
                launch(Dispatchers.Default) { mosaicProcessChunk(bitmap, newBitmap, 0, halfHeight, halfWidth, height, blockSize) },
                launch(Dispatchers.Default) { mosaicProcessChunk(bitmap, newBitmap, halfWidth, halfHeight, width, height, blockSize) }
            )

            jobs.joinAll()
        }

        return newBitmap
    }

    suspend fun mosaicProcessChunk(
        bitmap: Bitmap, newBitmap: Bitmap,
        startX: Int, startY: Int, endX: Int, endY: Int,
        blockSize: Int
    ) {
        withContext(Dispatchers.Default) {
            for (x in startX until endX step blockSize) {
                for (y in startY until endY step blockSize) {
                    val avgColor = filterImageService.getAverageColor(bitmap, x, y, blockSize, endX, endY)
                    for (i in x until min(x + blockSize, endX)) {
                        for (j in y until min(y + blockSize, endY)) {
                            newBitmap.setPixel(i, j, avgColor)
                        }
                    }
                }
            }
        }
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