package com.hits.imageeditor.imageEditingActivity.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class RetouchImage(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var bitmap: Bitmap? = null
    private val paint = Paint()

    private val touchRadius = 50

    fun setImageBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                bitmap?.let {
                    val x = event.x.toInt()
                    val y = event.y.toInt()
                    retouchBitmap(it, x, y, touchRadius)
                    invalidate()
                }
            }
        }
        return true
    }

    private fun retouchBitmap(bitmap: Bitmap, centerX: Int, centerY: Int, radius: Int) {
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
}
