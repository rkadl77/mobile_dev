package com.hits.imageeditor.imageEditingActivity.processing

import android.graphics.Bitmap
import android.graphics.Color

class FilterImageService {
    fun calculateWeights(sigma: Double): Array<DoubleArray> {
        val radius = (sigma * 3).toInt()
        val weights = Array(2 * radius + 1) { DoubleArray(2 * radius + 1) { 0.0 } }
        val sigmaSquaredTimesTwo = 2 * sigma * sigma
        var sum = 0.0

        for (j in -radius..radius) {
            for (i in -radius..radius) {
                val distanceSquared = (i * i + j * j).toDouble()
                weights[j + radius][i + radius] = Math.exp(-distanceSquared / sigmaSquaredTimesTwo) / (Math.PI * sigmaSquaredTimesTwo)
                sum += weights[j + radius][i + radius]
            }
        }

        for (j in weights.indices) {
            for (i in weights[j].indices) {
                weights[j][i] /= sum
            }
        }

        return weights
    }

    fun getAverageColor(bitmap: Bitmap, startX: Int, startY: Int, blockSize: Int): Int {
        var red = 0
        var green = 0
        var blue = 0

        val maxX = minOf(startX + blockSize, bitmap.width)
        val maxY = minOf(startY + blockSize, bitmap.height)

        for (x in startX until maxX) {
            for (y in startY until maxY) {
                val color = bitmap.getPixel(x, y)
                red += Color.red(color)
                green += Color.green(color)
                blue += Color.blue(color)
            }
        }

        val pixelCount = blockSize * blockSize
        red /= pixelCount
        green /= pixelCount
        blue /= pixelCount

        return Color.rgb(red, green, blue)
    }

    fun getNegativeColor(color: Int): Int {
        val alpha = Color.alpha(color)
        val red = 255 - Color.red(color)
        val green = 255 - Color.green(color)
        val blue = 255 - Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }
}