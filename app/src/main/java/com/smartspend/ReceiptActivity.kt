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

        // 🔹 Bind Views
        val btnGallery = findViewById<Button>(R.id.btnGallery)
        val cameraCard = findViewById<LinearLayout>(R.id.topCardContainer)

        previewImage = findViewById(R.id.ivReceiptPreview)
        recentImage = findViewById(R.id.imgReceipt1)

        val container = findViewById<LinearLayout>(R.id.recentReceiptsContainer)

        // 📸 Camera click
        cameraCard.setOnClickListener {
            openCamera()
        }

        // 🖼️ Gallery click
        btnGallery.setOnClickListener {
            openGallery()
        }

        // 🔥 LOAD RECEIPTS FROM DB
        lifecycleScope.launch {
            val expenses = db.expenseDao().getAllExpenses()

            if (expenses.isNotEmpty()) {

                // ✅ First static item gets latest image
                val latest = expenses.last()
                latest.receiptPath?.let {
                    recentImage.setImageURI(Uri.parse(it))
                }

                // 🔥 Dynamic receipts
                val reversed = expenses.dropLast(1).reversed()

                for (expense in reversed) {

                    val itemLayout = LinearLayout(this@ReceiptActivity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(24, 24, 24, 24)
                        setBackgroundResource(R.drawable.card_background)
                    }

                    val image = ImageView(this@ReceiptActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(120, 120)
                        setPadding(8, 8, 8, 8)
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }

                    expense.receiptPath?.let {
                        image.setImageURI(Uri.parse(it))
                    }

                    val textContainer = LinearLayout(this@ReceiptActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    }

                    val title = android.widget.TextView(this@ReceiptActivity).apply {
                        text = expense.description
                        textSize = 16f
                    }

                    val date = android.widget.TextView(this@ReceiptActivity).apply {
                        text = expense.date
                        textSize = 12f
                    }

                    val amount = android.widget.TextView(this@ReceiptActivity).apply {
                        text = "R %.2f".format(expense.amount)
                    }

                    textContainer.addView(title)
                    textContainer.addView(date)

                    itemLayout.addView(image)
                    itemLayout.addView(textContainer)
                    itemLayout.addView(amount)

                    container.addView(itemLayout)
                }
            }
        }

        // 🔙 Back navigation
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                NavigationHelper.goToDashboard(this@ReceiptActivity)
            }
        })
    }

    // 📸 OPEN CAMERA
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST)
    }

    // 🖼️ OPEN GALLERY
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST)
    }

    // 🎯 HANDLE RESULT
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

                    // 🔥 SEND TO EXPENSE SCREEN
                    val intent = Intent(this, ExpenseActivity::class.java)
                    intent.putExtra("receiptPath", imageUri.toString())
                    startActivity(intent)
                }
            }
        }
    }
}