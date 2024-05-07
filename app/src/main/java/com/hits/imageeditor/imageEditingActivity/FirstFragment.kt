package com.hits.imageeditor.imageEditingActivity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hits.imageeditor.R
import com.hits.imageeditor.databinding.FragmentFirstBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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
                val resizedBitmap = scaleImage(bitmap, scaleFactor)
                binding.imageView.setImageBitmap(resizedBitmap)
            }
        }

        binding.gausinButton.setOnClickListener {
            chosenImageBitmap?.let { bitmap ->
                val blurBitmap = gaussianBlur(bitmap, 12)
                // Обработка измененного изображения
                binding.imageView.setImageBitmap(blurBitmap)
            }
        }

        binding.buttonFirst.setOnClickListener {
            chosenImageBitmap?.let { bitmap ->
                // Сохранение изображения
                saveImageToStorage(bitmap)
            }
        }


        binding.mosaicButton.setOnClickListener {
            chosenImageBitmap?.let { bitmap ->
                val mosaicBitmap = mosaicFilter(bitmap, 20)
                // Обработка измененного изображения
                binding.imageView.setImageBitmap(mosaicBitmap)
            }
        }

        binding.negativeButton.setOnClickListener {
            chosenImageBitmap?.let { bitmap ->
                val negativeBitmap = negativeFilter(bitmap)
                // Обработка измененного изображения
                binding.imageView.setImageBitmap(negativeBitmap)
            }
        }

        binding.buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_MainActivity)
        }

        binding.undoButton.setOnClickListener {
            chosenImageUri = activity?.intent?.getParcelableExtra("Debug")
            binding.imageView.setImageURI(chosenImageUri)
        }

    }
    private fun saveImageToStorage(bitmap: Bitmap) {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "ImageEditor_$timeStamp.jpg"

        val storageDir: File? = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val imageFile = File(storageDir, fileName)

        try {
            val fileOutputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            // Уведомление об успешном сохранении
            showToast("Image successfully saved")
        } catch (e: IOException) {
            e.printStackTrace()
            // Обработка ошибки сохранения
            showToast("Image doesn't saved")
        }
    }


    // Функция для отображения всплывающего сообщения
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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

    private fun scaleImage(inputBitmap: Bitmap, scaleFactor: Float): Bitmap {
        val width = inputBitmap.width
        val height = inputBitmap.height
        val newWidth = (width * scaleFactor).toInt()
        val newHeight = (height * scaleFactor).toInt()

        val outputBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

        val xRatio = width.toFloat() / newWidth.toFloat()
        val yRatio = height.toFloat() / newHeight.toFloat()

        val interpolateMethod = if (scaleFactor > 1.0f) ::bilinearInterpolate else ::trilinearInterpolate

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

                val interpolatedPixel = interpolateMethod(topLeftPixel, topRightPixel, bottomLeftPixel, bottomRightPixel, xDiff, yDiff)

                outputBitmap.setPixel(x, y, interpolatedPixel)
            }
        }

        chosenImageBitmap = outputBitmap
        return  outputBitmap
    }

    private fun bilinearInterpolate(topLeft: Int, topRight: Int, bottomLeft: Int, bottomRight: Int, xDiff: Float, yDiff: Float): Int {
        val top = topLeft + (xDiff * (topRight - topLeft)).toInt()
        val bottom = bottomLeft + (xDiff * (bottomRight - bottomLeft)).toInt()
        return top + (yDiff * (bottom - top)).toInt()
    }

    private fun trilinearInterpolate(topLeft: Int, topRight: Int, bottomLeft: Int, bottomRight: Int, xDiff: Float, yDiff: Float): Int {
        val top = bilinearInterpolate(topLeft and 0xFF0000 shr 16, topRight and 0xFF0000 shr 16, bottomLeft and 0xFF0000 shr 16, bottomRight and 0xFF0000 shr 16, xDiff, yDiff)
        val bottom = bilinearInterpolate(topLeft and 0x00FF00 shr 8, topRight and 0x00FF00 shr 8, bottomLeft and 0x00FF00 shr 8, bottomRight and 0x00FF00 shr 8, xDiff, yDiff)
        val blue = bilinearInterpolate(topLeft and 0x0000FF, topRight and 0x0000FF, bottomLeft and 0x0000FF, bottomRight and 0x0000FF, xDiff, yDiff)
        return 0xFF000000.toInt() or (top shl 16) or (bottom shl 8) or blue
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


    fun mosaicFilter(bitmap: Bitmap, blockSize: Int): Bitmap {
        // Создаем новый битмап для результата
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)

        // Проходим по каждому блоку в изображении
        for (x in 0 until bitmap.width step blockSize) {
            for (y in 0 until bitmap.height step blockSize) {
                // Вычисляем средний цвет для текущего блока
                val avgColor = getAverageColor(bitmap, x, y, blockSize)

                // Закрашиваем весь блок средним цветом
                for (i in x until x + blockSize) {
                    for (j in y until y + blockSize) {
                        if (i < bitmap.width && j < bitmap.height) {
                            newBitmap.setPixel(i, j, avgColor)
                        }
                    }
                }
            }
        }
        chosenImageBitmap = newBitmap
        return newBitmap
    }

    // Функция для вычисления среднего цвета блока
    private fun getAverageColor(bitmap: Bitmap, startX: Int, startY: Int, blockSize: Int): Int {
        var red = 0
        var green = 0
        var blue = 0

        val maxX = minOf(startX + blockSize, bitmap.width)
        val maxY = minOf(startY + blockSize, bitmap.height)

        // Суммируем значения цветов всех пикселей в блоке
        for (x in startX until maxX) {
            for (y in startY until maxY) {
                val color = bitmap.getPixel(x, y)
                red += Color.red(color)
                green += Color.green(color)
                blue += Color.blue(color)
            }
        }

        // Вычисляем средний цвет для блока
        val pixelCount = blockSize * blockSize
        red /= pixelCount
        green /= pixelCount
        blue /= pixelCount

        return Color.rgb(red, green, blue)
    }

    // Функция для применения фильтра негатива к изображению
    fun negativeFilter(bitmap: Bitmap): Bitmap {
        // Создаем новый битмап для результата
        val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)

        // Проходим по каждому пикселю в изображении
        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                // Получаем цвет текущего пикселя
                val color = bitmap.getPixel(x, y)

                // Вычисляем негативный цвет для текущего пикселя
                val negativeColor = getNegativeColor(color)

                // Устанавливаем негативный цвет для пикселя в результирующем изображении
                newBitmap.setPixel(x, y, negativeColor)
            }
        }
        chosenImageBitmap = newBitmap
        return newBitmap
    }

    // Функция для вычисления негативного цвета
    private fun getNegativeColor(color: Int): Int {
        val alpha = Color.alpha(color)
        val red = 255 - Color.red(color)
        val green = 255 - Color.green(color)
        val blue = 255 - Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

}
