package com.hits.imageeditor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.dhaval2404.imagepicker.ImagePicker
import com.hits.imageeditor.databinding.ActivityMainBinding
import com.hits.imageeditor.imageEditingActivity.ImageEditingActivity



class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var chosenImageURI:Uri? = null


    override fun onCreate(savedInstanceState: Bundle?, ) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.imageAddButton.setOnClickListener{
            ImagePicker.with(this).start()
        }
        binding.rotateToImageEditingActivity.setOnClickListener {
             Intent(this@MainActivity, ImageEditingActivity::class.java).also {
                 it.putExtra("Debug", chosenImageURI )
                 startActivity(it)
             }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
            chosenImageURI = data?.data
            binding.imgGallery.setImageURI(data?.data)
    }

}