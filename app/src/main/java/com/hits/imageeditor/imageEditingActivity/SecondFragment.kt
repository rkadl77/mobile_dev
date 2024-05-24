package com.hits.imageeditor.imageEditingActivity

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.AttributeSet
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.hits.imageeditor.R
import com.hits.imageeditor.databinding.FragmentSecondBinding
import kotlin.random.Random

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        binding.buttonCube.setOnClickListener {
            val cubeView = binding.customCubeContainer.getChildAt(0) as? CustomCubeView
            cubeView?.startAnimation() // Проверка на null добавлена для безопасности
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class CustomCubeView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val vertexCoords = arrayOf(
        floatArrayOf(-1f, 1f, -1f), floatArrayOf(-1f, -1f, -1f),
        floatArrayOf(1f, -1f, -1f), floatArrayOf(1f, 1f, -1f),
        floatArrayOf(-1f, 1f, 1f), floatArrayOf(-1f, -1f, 1f),
        floatArrayOf(1f, -1f, 1f), floatArrayOf(1f, 1f, 1f)
    )

    private val edgeIndices = arrayOf(
        intArrayOf(0, 1), intArrayOf(1, 2), intArrayOf(2, 3), intArrayOf(3, 0),
        intArrayOf(4, 5), intArrayOf(5, 6), intArrayOf(6, 7), intArrayOf(7, 4),
        intArrayOf(0, 4), intArrayOf(1, 5), intArrayOf(2, 6), intArrayOf(3, 7)
    )

    private val faceIndices = arrayOf(
        intArrayOf(0, 1, 2, 3), intArrayOf(4, 5, 6, 7),
        intArrayOf(0, 4, 7, 3), intArrayOf(1, 2, 6, 5),
        intArrayOf(0, 1, 5, 4), intArrayOf(2, 3, 7, 6)
    )

    private val edgePaint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val facePaints = arrayOf(
        Paint().apply { color = Color.RED },
        Paint().apply { color = Color.GREEN },
        Paint().apply { color = Color.BLUE },
        Paint().apply { color = Color.YELLOW },
        Paint().apply { color = Color.CYAN },
        Paint().apply { color = Color.MAGENTA }
    )

    private var angleX = 0f
    private var angleY = 0f

    init {
        pivotX = 0.5f * resources.displayMetrics.widthPixels
        pivotY = 0.5f * resources.displayMetrics.heightPixels
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.apply {
            save()
            rotate(angleX, pivotX, pivotY)
            rotate(angleY, pivotX, pivotY)

            drawCube(canvas)

            restore()
        }
    }

    private fun drawCube(canvas: Canvas) {
        for (faceIndex in faceIndices.indices) {
            val face = faceIndices[faceIndex]
            val paint = facePaints[faceIndex]

            val path = android.graphics.Path()
            path.moveTo(vertexCoords[face[0]][0] * 100 + pivotX, vertexCoords[face[0]][1] * 100 + pivotY)
            for (i in 1 until face.size) {
                val vertex = vertexCoords[face[i]]
                path.lineTo(vertex[0] * 100 + pivotX, vertex[1] * 100 + pivotY)
            }
            path.close()
            canvas.drawPath(path, paint)
            canvas.drawPath(path, edgePaint)
        }
    }

    fun startAnimation() {
        animate().rotationXBy(360f).rotationYBy(360f).setDuration(1000).start()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                angleX += event.y - pivotY
                angleY += event.x - pivotX
                invalidate()
            }
        }
        return true
    }
}