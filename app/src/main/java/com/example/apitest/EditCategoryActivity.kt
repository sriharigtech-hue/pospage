package com.example.apitest

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apitest.network.ApiClient
import com.example.apitest.dataModel.StatusResponse
import com.google.android.material.textfield.TextInputEditText
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.TextView
import android.view.View
import android.provider.MediaStore
import android.util.Log
import kotlin.math.log

class EditCategoryActivity : AppCompatActivity() {

    private lateinit var categoryNameEditText: TextInputEditText
//    private lateinit var categoryImageView: CircleImageView
    private lateinit var saveButton: TextView

    private var categoryId: String = ""
    private var categoryImageUri: Uri? = null

    private val IMAGE_PICK_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_category)

        categoryNameEditText = findViewById(R.id.EditCategoryName)
//        categoryImageView = findViewById(R.id.categoryImage)
        saveButton = findViewById(R.id.EditSaveButton)

        // Get data from intent
        categoryId = intent.getStringExtra("category_id") ?: ""
        val categoryName = intent.getStringExtra("category_name") ?: ""
//        val categoryImage = intent.getStringExtra("category_image") ?: ""

        categoryNameEditText.setText(categoryName)
//        if (categoryImage.isNotEmpty()) {
//             Optional: load image via Glide or Picasso
//             Glide.with(this).load(categoryImage).into(categoryImageView)
//        }
//
//         Pick new image
//        categoryImageView.setOnClickListener {
//            pickImageFromGallery()
//        }

        saveButton.setOnClickListener {
            val name = categoryNameEditText.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter category name", Toast.LENGTH_SHORT).show()
            } else {
                uploadEditCategory(categoryId, name)
            }
        }

        // Back button
        findViewById<View>(R.id.backButton).setOnClickListener { finish() }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            categoryImageUri = data?.data
//            categoryImageView.setImageURI(categoryImageUri)
        }
    }

    private fun uploadEditCategory(categoryId: String, categoryName: String) {
        val token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiYjE1YTc2MWYyMTA3NTJiYTY2MjBiMDg4NTUzYmRiMGYxYWVhZmEwOTJlMWY4MGI1YmFjYmExNzA4MDFlN2Y2NGU1NDEyODQyMzBjYTJiM2YiLCJpYXQiOjE3NTY4MTA5MzUuNjIwMDU5LCJuYmYiOjE3NTY4MTA5MzUuNjIwMDYyLCJleHAiOjE3ODgzNDY5MzUuNjE2NjA2LCJzdWIiOiI2MCIsInNjb3BlcyI6W119.UzX5UwID0Xd9Ia9ZIahOq7ugA8k8viEIOX261q2H2rhR7vMGZHTmm6ymDhdWKmmtSN0fTmU8AijKQzHN4g9HRZvr9seeEHQN3doBFT4odcCbufig4LEH2E0oMjQMmOIYEIjbj-n8o5i2lcqOfchu3vCrDt6McE7GBuPzTA87wyQpMPyO4IAUKU7h7TnwVx3VB_Y8aAUR5DpLr9-LQ7PpOG_hPUvqfUJ3jLoaluDAXA-1hPQ8EXKRz15xAfQxHLR0LLNOCf31hIj7JOxJQFDU-wzXs9g-bH6aAPOY2Q5tye-JZPoLNDCFRxNIkK7HgddkFdH7w0DnW2r4s4vjtfKe0Ubc0aOAxgE74OS_50rw-QEZjl0SceQhoNqeSgSH43_JSjAWX5-VxlwNBgGBBVMXBsKUt52S_eVyyOJpA_qXCqFjVqmWh-MPgo55-dEHw9FFc1ptvzY6FUeYnwBs4Kd65SZyFfU0Esx9wqtq5ZarZDR-gfZUlaP-toKzOzpAs7QasunG62nHDBdUX4P-EdCDFjQdroxeX983vJGe_GzYj5sBRZauXsqg0wtvZmAR2qaxzK4HB853hs7BJn0QAQRZIM7qERV0VqgynnWIrPFhV1WDyU_NMxPQh3WM6jqICK30uqiD7-201ga-522-Dnbxd1vF8CQIFGMRgiwikl3cfoI"

        val categoryIdBody = RequestBody.create("text/plain".toMediaTypeOrNull(), categoryId)
        val categoryNameBody = RequestBody.create("text/plain".toMediaTypeOrNull(), categoryName)
        val statusBody = RequestBody.create("text/plain".toMediaTypeOrNull(), "1")

        ApiClient.instance.uploadEditCategory(
            jwtToken = token,
            category_id = categoryIdBody,
            category_name = categoryNameBody,
            status = statusBody
        ).enqueue(object : Callback<StatusResponse> {
            override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(this@EditCategoryActivity, "Category updated!", Toast.LENGTH_SHORT).show()
                    // Send updated data back to fragment
                    val intent = Intent().apply {
                        putExtra("category_id", categoryId)
                        putExtra("category_name", categoryName)
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else {
                    Toast.makeText(this@EditCategoryActivity, "Update failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                Toast.makeText(this@EditCategoryActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
