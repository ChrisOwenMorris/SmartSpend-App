package com.smartspend

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ReceiptActivity : AppCompatActivity() {

    private val db by lazy {
        (application as SmartSpendApp).database
    }

    private lateinit var previewImage: ImageView
    private lateinit var recentImage: ImageView
    private var imageUri: Uri? = null

    // FIX 1: Replace startActivityForResult(CAMERA_REQUEST) with Activity Result API
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            previewImage.setImageBitmap(it)
            previewImage.visibility = ImageView.VISIBLE
            Toast.makeText(this, getString(R.string.camera_image_captured), Toast.LENGTH_SHORT).show()
        }
    }

    // FIX 2: Replace startActivityForResult(GALLERY_REQUEST) with Activity Result API
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            previewImage.setImageURI(it)
            previewImage.visibility = ImageView.VISIBLE

            // Send to Expense screen
            val intent = android.content.Intent(this, ExpenseActivity::class.java)
            intent.putExtra("receiptPath", it.toString())
            startActivity(intent)
        }
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
                // FIX 3: Replace deprecated bundle.get("data") / Uri.parse() with toUri() KTX
                latest.receiptPath?.let {
                    recentImage.setImageURI(it.toUri())
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
        cameraLauncher.launch(null)
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }
}