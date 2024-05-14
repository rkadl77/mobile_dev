package com.hits.imageeditor.imageEditingActivity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
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


        binding.buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.imageView.setImageURI(chosenImageUri)

        //Диапазонный ввод для алгоритма поворота изображения
        binding.rotateAlghoritm.setOnClickListener{
            binding.userInputSettings.displayedChild = 1

        }


        binding.rotateSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.rotateSeekBarValue.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.rotateApplyButton.setOnClickListener {
            chosenImageBitmap?.let { bitmap ->
                val rotatedImage = rotateImage(bitmap, binding.rotateSeekBar.progress * 1f)
                binding.imageView.setImageBitmap(rotatedImage)
            }
        }

        //Диапазонный ввод для алгоритма изменения масштаба изображения
        val resizeSeekBar = binding.resizeSeekBar
        resizeSeekBar.min = -20
        resizeSeekBar.max = 20
        resizeSeekBar.progress = 0

        binding.resizeAlghoritm.setOnClickListener {
            binding.userInputSettings.displayedChild = 2
        }

        binding.resizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.resizeSeekBarValue.text = (progress / 10.0).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.resizeApplyButton.setOnClickListener{
            chosenImageBitmap?.let { bitmap ->
                val scaleFactor = binding.resizeSeekBar.progress/10.0f
                val resizedBitmap = scaleImage(bitmap, scaleFactor)
                binding.imageView.setImageBitmap(resizedBitmap)
            }
        }

        binding.filtersButton.setOnClickListener{
            binding.userInputSettings.displayedChild = 3
        }

        binding.mosaicButton.setOnClickListener {
            chosenImageBitmap?.let { bitmap ->
                val mosaicBitmap = mosaicFilter(bitmap, 20)
                binding.imageView.setImageBitmap(mosaicBitmap)
            }
        }

        binding.negativeButton.setOnClickListener {
            chosenImageBitmap?.let { bitmap ->
                val negativeBitmap = negativeFilter(bitmap)
                binding.imageView.setImageBitmap(negativeBitmap)
            }
        }

        binding.gausinButton.setOnClickListener {
            chosenImageBitmap?.let { bitmap ->
                val blurBitmap = gaussianBlur(bitmap, 12)
                binding.imageView.setImageBitmap(blurBitmap)
            }
        }

        binding.unsharpMaskingButton.setOnClickListener {
            chosenImageBitmap?.let { bitmap ->
                val sharpedBitmap = unsharpMasking(bitmap)
                binding.imageView.setImageBitmap(sharpedBitmap)
            }
        }


        binding.saveButton.setOnClickListener {
            chosenImageBitmap?.let { bitmap ->
                saveImageToStorage(bitmap)
            }
        }


        binding.buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_MainActivity)
        }

        binding.undoButton.setOnClickListener {
            chosenImageUri = activity?.intent?.getParcelableExtra("Debug")
            chosenImageBitmap = chosenImageUri?.let { uri ->
                context?.let { context ->
                    getBitmapFromUri(context, uri)
                }
            }
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

    private fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap {
        val radians = Math.toRadians(degrees.toDouble())
        val cos = Math.cos(radians)
        val sin = Math.sin(radians)

        val width = bitmap.width
        val height = bitmap.height
        val newWidth = (Math.abs(width * cos) + Math.abs(height * sin)).toInt()
        val newHeight = (Math.abs(width * sin) + Math.abs(height * cos)).toInt()

        val newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

        val px = width / 2
        val py = height / 2

        for (x in 0 until newWidth) {
            for (y in 0 until newHeight) {
                val ax = x - newWidth / 2
                val ay = y - newHeight / 2
                val srcX = (ax * cos + ay * sin + px).toInt()
                val srcY = (-ax * sin + ay * cos + py).toInt()

                if (srcX >= 0 && srcX < width && srcY >= 0 && srcY < height) {
                    newBitmap.setPixel(x, y, bitmap.getPixel(srcX, srcY))
                } else {
                    newBitmap.setPixel(x, y, Color.BLACK)
                }
            }
        }

        chosenImageBitmap = newBitmap
        return newBitmap
    }

    private fun unsharpMasking(bitmap: Bitmap, radius: Int = 2, amount: Float = 0.5f): Bitmap {
        val blurredBitmap = boxBlur(bitmap, radius)

        val maskBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                val blurredPixel = blurredBitmap.getPixel(x, y)
                val redDiff = Color.red(pixel) - Color.red(blurredPixel)
                val greenDiff = Color.green(pixel) - Color.green(blurredPixel)
                val blueDiff = Color.blue(pixel) - Color.blue(blurredPixel)
                val alpha = Color.alpha(pixel)
                val newPixel = Color.argb(
                    alpha,
                    clamp((Color.red(pixel) + redDiff * amount).toInt()),
                    clamp((Color.green(pixel) + greenDiff * amount).toInt()),
                    clamp((Color.blue(pixel) + blueDiff * amount).toInt())
                )
                maskBitmap.setPixel(x, y, newPixel)
            }
        }

        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                val maskPixel = maskBitmap.getPixel(x, y)
                val alpha = Color.alpha(pixel)
                val newPixel = Color.argb(
                    alpha,
                    clamp(Color.red(pixel) + Color.red(maskPixel)),
                    clamp(Color.green(pixel) + Color.green(maskPixel)),
                    clamp(Color.blue(pixel) + Color.blue(maskPixel))
                )
                resultBitmap.setPixel(x, y, newPixel)
            }
        }

        chosenImageBitmap = resultBitmap
        return resultBitmap
    }

    private fun clamp(value: Int): Int {
        return value.coerceIn(0, 255)
    }


    private fun boxBlur(bitmap: Bitmap, radius: Int): Bitmap {
        val blurredBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        val blurredPixels = IntArray(bitmap.width * bitmap.height)

        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                var red = 0
                var green = 0
                var blue = 0
                var alpha = 0

                var count = 0

                for (dy in -radius..radius) {
                    for (dx in -radius..radius) {
                        val nx = x + dx
                        val ny = y + dy

                        if (nx in 0 until bitmap.width && ny in 0 until bitmap.height) {
                            val pixel = pixels[ny * bitmap.width + nx]
                            red += Color.red(pixel)
                            green += Color.green(pixel)
                            blue += Color.blue(pixel)
                            alpha += Color.alpha(pixel)
                            count++
                        }
                    }
                }

                val avgRed = red / count
                val avgGreen = green / count
                val avgBlue = blue / count
                val avgAlpha = alpha / count

                val blurredPixel = Color.argb(avgAlpha, avgRed, avgGreen, avgBlue)

                blurredPixels[y * bitmap.width + x] = blurredPixel
            }
        }

        blurredBitmap.setPixels(blurredPixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        return blurredBitmap
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
