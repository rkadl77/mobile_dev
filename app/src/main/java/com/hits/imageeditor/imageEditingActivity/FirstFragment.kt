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
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hits.imageeditor.R
import com.hits.imageeditor.databinding.FragmentFirstBinding
import com.hits.imageeditor.imageEditingActivity.processing.FilterImage
import com.hits.imageeditor.imageEditingActivity.processing.ResizeImage
import com.hits.imageeditor.imageEditingActivity.processing.RotateImage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking



class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private var chosenImageUri: Uri? = null
    private var chosenImageBitmap: Bitmap? = null

    private val rotateImage = RotateImage()
    private val resizeImage = ResizeImage()
    private val filterImage = FilterImage()


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
        binding.rotateAlghoritm.setOnClickListener {
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

        binding.filtersButton.setOnClickListener {
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

        binding.unsharpMaskingButton.setOnClickListener {
            chosenImageBitmap?.let { bitmap ->
                val sharpedBitmap = filterImage.unsharpMasking(bitmap)
                chosenImageBitmap = sharpedBitmap
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

            // Уведомление об успешном сохранении
            showToast("Image successfully saved")
        } catch (e: IOException) {
            e.printStackTrace()
            // Обработка ошибки сохранения
            showToast("Image doesn't saved")
        }
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
    
}
