package com.hits.imageeditor.imageEditingActivity.processing

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.min

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

    fun getAverageColor(bitmap: Bitmap, x: Int, y: Int, blockSize: Int, width: Int, height: Int): Int {
        var redSum = 0
        var greenSum = 0
        var blueSum = 0
        var pixelCount = 0

        for (i in x until min(x + blockSize, width)) {
            for (j in y until min(y + blockSize, height)) {
                val pixel = bitmap.getPixel(i, j)
                redSum += Color.red(pixel)
                greenSum += Color.green(pixel)
                blueSum += Color.blue(pixel)
                pixelCount++
            }
        }

        return Color.rgb(redSum / pixelCount, greenSum / pixelCount, blueSum / pixelCount)
    }

    fun getNegativeColor(color: Int): Int {
        val alpha = Color.alpha(color)
        val red = 255 - Color.red(color)
        val green = 255 - Color.green(color)
        val blue = 255 - Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }
}