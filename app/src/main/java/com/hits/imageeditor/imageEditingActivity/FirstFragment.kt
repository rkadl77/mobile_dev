package com.hits.imageeditor.imageEditingActivity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hits.imageeditor.R
import com.hits.imageeditor.databinding.FragmentFirstBinding
import com.hits.imageeditor.imageEditingActivity.processing.FilterImage
import com.hits.imageeditor.imageEditingActivity.processing.ResizeImage
import com.hits.imageeditor.imageEditingActivity.processing.RetouchImage
import com.hits.imageeditor.imageEditingActivity.processing.RotateImage
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

    private var isRetouchMode = false

    private val rotateImage = RotateImage()
    private val resizeImage = ResizeImage()
    private val filterImage = FilterImage()
    private val retouchImage = RetouchImage()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chosenImageUri = activity?.intent?.getParcelableExtra("Debug")
        chosenImageBitmap = chosenImageUri?.let { uri ->
            context?.let { context ->
                getBitmapFromUri(context, uri)
            }?.copy(Bitmap.Config.ARGB_8888, true)

        }


        binding.buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.imageView.setImageBitmap(chosenImageBitmap)

        //Диапазонный ввод для алгоритма поворота изображения
        binding.rotateAlghoritm.setOnClickListener {
            isRetouchMode = false
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
                val rotatedImage = rotateImage.rotateImage(bitmap, binding.rotateSeekBar.progress * 1f)
                chosenImageBitmap = rotatedImage
                binding.imageView.setImageBitmap(rotatedImage)
            }
        }

        //Диапазонный ввод для алгоритма изменения масштаба изображения
        val resizeSeekBar = binding.resizeSeekBar
        resizeSeekBar.min = -20
        resizeSeekBar.max = 20
        resizeSeekBar.progress = 0

        binding.resizeAlghoritm.setOnClickListener {
            isRetouchMode = false
            binding.userInputSettings.displayedChild = 2
        }

        binding.resizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.resizeSeekBarValue.text = (progress / 10.0).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.resizeApplyButton.setOnClickListener {
            chosenImageBitmap?.let { bitmap ->
                val scaleFactor = binding.resizeSeekBar.progress / 10.0f
                val resizedBitmap = resizeImage.scaleImage(bitmap, scaleFactor)
                chosenImageBitmap = resizedBitmap
                binding.imageView.setImageBitmap(resizedBitmap)
            }
        }


        //Фильтры
        binding.filtersButton.setOnClickListener {
            isRetouchMode = false
            binding.userInputSettings.displayedChild = 3
        }

        binding.mosaicButton.setOnClickListener {
            chosenImageBitmap?.let { bitmap ->
                val mosaicBitmap = filterImage.mosaicFilter(bitmap, 20)
                chosenImageBitmap = mosaicBitmap
                binding.imageView.setImageBitmap(mosaicBitmap)
            }
        }

        binding.negativeButton.setOnClickListener {
            chosenImageBitmap?.let { bitmap ->
                val negativeBitmap = filterImage.negativeFilter(bitmap)
                chosenImageBitmap = negativeBitmap
                binding.imageView.setImageBitmap(negativeBitmap)
            }
        }

        binding.gausinButton.setOnClickListener {
            chosenImageBitmap?.let { bitmap ->
                val blurBitmap = filterImage.gaussianBlur(bitmap, 12)
                chosenImageBitmap = blurBitmap
                binding.imageView.setImageBitmap(blurBitmap)
            }
        }

        // диапозонный ввод для нерезкого маскирования
        binding.unsharpMaskingButton.setOnClickListener {
            isRetouchMode = false
            binding.userInputSettings.displayedChild = 4
        }

        binding.maskingAmountSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.maskingAmountSeekBarValue.text = "Amount: ${(progress / 100.0)}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.maskingRadiusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.maskingRadiusSeekBarValue.text = "Radius: ${(progress / 2.0)}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.maskingThresholdSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.maskingThresholdSeekBarValue.text = "Threshold: ${(progress / 2.0)}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.maskingApplyButton.setOnClickListener {
            chosenImageBitmap?.let { bitmap ->
                val radius = binding.maskingRadiusSeekBar.progress / 2
                val amount = binding.maskingAmountSeekBar.progress / 100.0f
                val threshold = binding.maskingThresholdSeekBar.progress / 2
                val maskedBitmap = filterImage.unsharpMasking(bitmap, radius, amount, threshold )
                chosenImageBitmap = maskedBitmap
                binding.imageView.setImageBitmap(maskedBitmap)
            }
        }

        // диапазонный ввод для ретуширования
        binding.retouchButton.setOnClickListener{
            isRetouchMode = true
            binding.userInputSettings.displayedChild = 5
        }

        binding.retouchSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.retouchSeekBarValue.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.retouchApplyButton.setOnClickListener{
            retouchImage.setterRetouchRadius(binding.retouchSeekBar.progress)
        }

        binding.imageView.setOnTouchListener { _, event ->
            if ( isRetouchMode && (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE)) {
                val matrix = binding.imageView.imageMatrix
                val values = FloatArray(9)
                matrix.getValues(values)

                val scaleX = values[Matrix.MSCALE_X]
                val scaleY = values[Matrix.MSCALE_Y]
                val transX = values[Matrix.MTRANS_X]
                val transY = values[Matrix.MTRANS_Y]

                val imageViewX = (event.x - transX) / scaleX
                val imageViewY = (event.y - transY) / scaleY

                val imageWidth = chosenImageBitmap?.width ?: 1
                val imageHeight = chosenImageBitmap?.height ?: 1

                val bitmapX = imageViewX.coerceIn(0f, (imageWidth - 1).toFloat()).toInt()
                val bitmapY = imageViewY.coerceIn(0f, (imageHeight - 1).toFloat()).toInt()

                chosenImageBitmap?.let {
                    try {
                        retouchImage.retouchBitmap(it, bitmapX, bitmapY, retouchImage.retouchRadius)
                        binding.imageView.setImageBitmap(it)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error during retouching", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            true
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
            }?.copy(Bitmap.Config.ARGB_8888, true)
            binding.imageView.setImageBitmap(chosenImageBitmap)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun saveImageToStorage(bitmap: Bitmap) {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "ImageEditor_$timeStamp.jpg"

        val storageDir: File? =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val imageFile = File(storageDir, fileName)

        try {
            val fileOutputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            showToast("Image successfully saved")
        } catch (e: IOException) {
            e.printStackTrace()
            showToast("Image doesn't saved")
        }
    }

    private fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            return originalBitmap?.copy(Bitmap.Config.ARGB_8888, true)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return null
    }

}
