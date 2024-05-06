package com.hits.imageeditor.imageEditingActivity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hits.imageeditor.R
import com.hits.imageeditor.databinding.FragmentFirstBinding
import java.io.InputStream
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint


class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private var chosenImageUri: Uri? = null
    private var chosenImageBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chosenImageUri = activity?.intent?.getParcelableExtra("Debug")
        chosenImageBitmap = chosenImageUri?.let { uri ->
            context?.let { context ->
                getBitmapFromUri(context, uri)
            }
        }

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.imageView.setImageURI(chosenImageUri)

        binding.firstAlghoritm.setOnClickListener {
            chosenImageBitmap?.let { bitmap ->
                val rotatedImage = rotateImage(bitmap, 90)
                binding.imageView.setImageBitmap(rotatedImage)


            }
        }
        binding.secondAlghoritm.setOnClickListener {
            chosenImageBitmap?.let { bitmap ->
                val scaleFactor = 0.5f // Пример коэффициента масштабирования
                val resizedBitmap = resizeImage(bitmap, scaleFactor)
                binding.imageView.setImageBitmap(resizedBitmap)
            }
        }


        binding.gaussButton.setOnClickListener {
            chosenImageBitmap?.let { bitmap ->
                val blurBitmap = gaussianBlur(bitmap, 12)
                // Обработка измененного изображения
                binding.imageView.setImageBitmap(blurBitmap)
            }
        }

        binding.buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_MainActivity)
        }



    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            return BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return null
    }

    private fun rotateImage(bitmap: Bitmap, degrees: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val newBitmap = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val newX = if (degrees == 90 || degrees == 270) height - y - 1 else y
                val newY = if (degrees == 90 || degrees == 270) x else width - x - 1

                val pixel = bitmap.getPixel(x, y)
                newBitmap.setPixel(newX, newY, pixel)
            }
        }

        chosenImageBitmap = newBitmap
        return newBitmap
    }

    private fun resizeImage(inputBitmap: Bitmap, scaleFactor: Float): Bitmap {
        val oldWidth = inputBitmap.width
        val oldHeight = inputBitmap.height

        val newWidth = (oldWidth * scaleFactor).toInt()
        val newHeight = (oldHeight * scaleFactor).toInt()

        val newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(newBitmap)

        val matrix = Matrix()
        matrix.setScale(scaleFactor, scaleFactor)

        canvas.drawBitmap(inputBitmap, matrix, Paint())

    chosenImageBitmap = newBitmap
        return newBitmap
    }

    private fun gaussianBlur(inputBitmap: Bitmap, radius: Int): Bitmap {
        val width = inputBitmap.width
        val height = inputBitmap.height
        val pixels = IntArray(width * height)
        inputBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val blurredBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val blurredPixels = IntArray(width * height)
        val weights = calculateWeights(radius.toDouble())

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
        chosenImageBitmap = blurredBitmap
        return blurredBitmap
    }

    private fun calculateWeights(sigma: Double): Array<DoubleArray> {
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

}
