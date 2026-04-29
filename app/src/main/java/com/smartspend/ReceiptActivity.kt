package com.smartspend

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ReceiptActivity : AppCompatActivity() {

    private val db by lazy {
        (application as SmartSpendApp).database
    }

    private lateinit var previewImage: ImageView
    private lateinit var recentImage: ImageView
    private var imageUri: Uri? = null

    companion object {
        private const val CAMERA_REQUEST = 100
        private const val GALLERY_REQUEST = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        NavigationHelper.setupMenu(this)


        val btnGallery = findViewById<Button>(R.id.btnGallery)
        val cameraCard = findViewById<LinearLayout>(R.id.topCardContainer)

        previewImage = findViewById(R.id.ivReceiptPreview)


        recentImage = findViewById(R.id.imgReceipt1)


        cameraCard?.setOnClickListener {
            openCamera()
        }


        btnGallery.setOnClickListener {
            openGallery()
        }


        lifecycleScope.launch {
            val expenses = db.expenseDao().getAllExpenses()

            if (expenses.isNotEmpty()) {
                val latest = expenses.last()

                latest.receiptPath?.let {
                    recentImage.setImageURI(Uri.parse(it))
                }
            }
        }


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                NavigationHelper.goToDashboard(this@ReceiptActivity)
            }
        })
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {

                CAMERA_REQUEST -> {
                    val photo = data?.extras?.get("data") as Bitmap
                    previewImage.setImageBitmap(photo)
                    previewImage.visibility = ImageView.VISIBLE

                    Toast.makeText(this, "Camera image captured", Toast.LENGTH_SHORT).show()
                }

                GALLERY_REQUEST -> {
                    imageUri = data?.data
                    previewImage.setImageURI(imageUri)
                    previewImage.visibility = ImageView.VISIBLE

                    //  SEND TO EXPENSE SCREEN
                    val intent = Intent(this, ExpenseActivity::class.java)
                    intent.putExtra("receiptPath", imageUri.toString())
                    startActivity(intent)
                }
            }
        }
    }
}