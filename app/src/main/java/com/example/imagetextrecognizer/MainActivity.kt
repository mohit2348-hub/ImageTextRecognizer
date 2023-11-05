package com.example.imagetextrecognizer

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.util.size
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.imagetextrecognizer.databinding.ActivityMainBinding
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val textRecognizer: TextRecognizer by lazy {
        TextRecognizer.Builder(applicationContext).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        init()

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted; you can now access the external storage.
            } else {
                // Permission is denied; handle this case.
            }
        }

    }

    private fun checkPermission(): Boolean {
        // Check if the permission is already granted.
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        // Request the permission.
        requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }


    private fun init() {
        mBinding.btnUpload.setOnClickListener {
            if (checkPermission()) {
                openSomeActivityForResult()
            } else {
                requestPermission()
            }


        }
    }


    private fun openSomeActivityForResult() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }


    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val selectedImage = result.data?.data
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                recognizeText(bitmap)
            }
        }


    private fun recognizeText(bitmap: Bitmap) {
        if (!textRecognizer.isOperational) {
            // Handle the case where text recognition is not available or not operational.
            Log.e("TextRecognition", "Text recognition not operational.")
            return
        }

        val frame = Frame.Builder().setBitmap(bitmap).build()
        val textBlocks = textRecognizer.detect(frame)

        if (textBlocks.size == 0) {
            Log.d("TextRecognition", "No text detected in the bitmap.")
            Toast.makeText(this, "Does not Contain text", Toast.LENGTH_LONG).show()
            return
        } else {
            Log.d("TextRecognition", "Text detected successfully.")
            Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()
            Glide.with(this)
                .load(bitmap)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(mBinding.ivImage)
        }
    }



}

