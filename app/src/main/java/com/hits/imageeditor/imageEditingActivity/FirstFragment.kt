package com.hits.imageeditor.imageEditingActivity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
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

                val newWidth = 400 // ваша новая ширина
                val newHeight = 400// ваша новая высота
                val resizedBitmap = resizeImage(bitmap, newWidth, newHeight)
                // Обработка измененного изображения
                binding.imageView.setImageBitmap(resizedBitmap)
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

    private fun resizeImage(inputBitmap: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val oldWidth = inputBitmap.width
        val oldHeight = inputBitmap.height

        val newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

        val scaleX = oldWidth.toFloat() / newWidth
        val scaleY = oldHeight.toFloat() / newHeight

        for (x in 0 until newWidth) {
            for (y in 0 until newHeight) {
                val oldX = (x * scaleX).toInt()
                val oldY = (y * scaleY).toInt()
                val pixel = inputBitmap.getPixel(oldX, oldY)
                newBitmap.setPixel(x, y, pixel)
            }
        }

        chosenImageBitmap = newBitmap
        return newBitmap
    }


}
