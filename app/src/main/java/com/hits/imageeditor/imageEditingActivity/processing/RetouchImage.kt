package com.hits.imageeditor.imageEditingActivity.processing

import android.graphics.Bitmap
import android.graphics.Color

class RetouchImage() {
   var retouchRadius = 0

     fun retouchBitmap(bitmap: Bitmap, centerX: Int, centerY: Int, radius: Int) {
        val pixels = mutableListOf<Int>()
        for (dx in -radius..radius) {
            for (dy in -radius..radius) {
                val x = (centerX + dx).coerceIn(0, bitmap.width - 1)
                val y = (centerY + dy).coerceIn(0, bitmap.height - 1)
                if (dx * dx + dy * dy <= radius * radius) {
                    pixels.add(bitmap.getPixel(x, y))
                }
            }
        }
        val medianColor = medianFilter(pixels)
        for (dx in -radius..radius) {
            for (dy in -radius..radius) {
                val x = (centerX + dx).coerceIn(0, bitmap.width - 1)
                val y = (centerY + dy).coerceIn(0, bitmap.height - 1)
                if (dx * dx + dy * dy <= radius * radius) {
                    bitmap.setPixel(x, y, medianColor)
                }
            }
        }
    }

    private fun medianFilter(pixels: List<Int>): Int {
        val reds = pixels.map { Color.red(it) }.sorted()
        val greens = pixels.map { Color.green(it) }.sorted()
        val blues = pixels.map { Color.blue(it) }.sorted()

        val medianIndex = pixels.size / 2
        val medianRed = reds[medianIndex]
        val medianGreen = greens[medianIndex]
        val medianBlue = blues[medianIndex]

        return Color.rgb(medianRed, medianGreen, medianBlue)
    }

    fun setterRetouchRadius(radius: Int) {
        retouchRadius = radius
    }
}

