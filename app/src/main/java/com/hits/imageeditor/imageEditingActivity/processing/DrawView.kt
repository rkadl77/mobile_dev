package com.hits.imageeditor.imageEditingActivity.processing

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View

class DrawView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs), SurfaceHolder.Callback, View.OnTouchListener {
    private val path = Path()
    private val points = mutableListOf<PointF>()
    private val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private var bitmap: Bitmap? = null
    private var canvasBitmap: Canvas? = null
    private var isDrawingEnabled = false

    init {
        holder.addCallback(this)
        setOnTouchListener(this)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

    override fun surfaceCreated(holder: SurfaceHolder) {
        drawCanvas()
    }

    private fun drawCanvas() {
        val canvas = holder.lockCanvas()
        if (canvas != null) {
            canvas.drawColor(Color.WHITE)
            bitmap?.let {
                canvas.drawBitmap(it, 0f, 0f, null)
            }
            if (points.isNotEmpty()) {
                path.reset()
                path.moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    path.lineTo(points[i].x, points[i].y)
                }
                canvas.drawPath(path, paint)
            }
            holder.unlockCanvasAndPost(canvas)
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (!isDrawingEnabled) return false

        val point = PointF(event.x, event.y)
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                points.add(point)
                drawCanvas()
            }
        }
        return true
    }

    fun enableDrawing(enabled: Boolean) {
        isDrawingEnabled = enabled
    }

    fun transformToSpline() {
        if (points.size > 2) {
            val splinePath = calculateSpline(points)
            path.set(splinePath)
            drawCanvas()
        }
    }

    private fun calculateSpline(points: List<PointF>): Path {
        val splinePath = Path()
        splinePath.moveTo(points[0].x, points[0].y)

        for (i in 0 until points.size - 1) {
            val p0 = if (i > 0) points[i - 1] else points[i]
            val p1 = points[i]
            val p2 = points[i + 1]
            val p3 = if (i + 2 < points.size) points[i + 2] else points[i + 1]

            for (t in 0..100) {
                val tNormalized = t / 100.0f
                val x = 0.5f * ((2 * p1.x) + (-p0.x + p2.x) * tNormalized +
                        (2 * p0.x - 5 * p1.x + 4 * p2.x - p3.x) * tNormalized * tNormalized +
                        (-p0.x + 3 * p1.x - 3 * p2.x + p3.x) * tNormalized * tNormalized * tNormalized)
                val y = 0.5f * ((2 * p1.y) + (-p0.y + p2.y) * tNormalized +
                        (2 * p0.y - 5 * p1.y + 4 * p2.y - p3.y) * tNormalized * tNormalized +
                        (-p0.y + 3 * p1.y - 3 * p2.y + p3.y) * tNormalized * tNormalized * tNormalized)
                splinePath.lineTo(x, y)
            }
        }
        return splinePath
    }

    fun setBitmap(newBitmap: Bitmap) {
        bitmap = newBitmap.copy(Bitmap.Config.ARGB_8888, true)
        canvasBitmap = Canvas(bitmap!!)
        drawCanvas()
    }

    fun getBitmap(): Bitmap? {
        val resultBitmap = bitmap?.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(resultBitmap!!)
        canvas.drawPath(path, paint)
        return resultBitmap
    }
}
