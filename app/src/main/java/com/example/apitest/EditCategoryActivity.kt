package com.example.apitest

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.apitest.dataModel.StatusResponse
import com.example.apitest.network.ApiClient
import com.google.android.material.textfield.TextInputEditText
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class EditCategoryActivity : AppCompatActivity() {
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZTRjY2JlNDBmYWFiZmE5YTg1YmVkMDdjOTcyMzAzNWY3MTljNzQ4M2Q3YzIzMjg3M2E5ZjZmN2QyMjQ1Y2E3MjVhNjVlMTQ5ZGRiZjBkYjgiLCJpYXQiOjE3NjE2MzM3OTIuNDI5OTk5LCJuYmYiOjE3NjE2MzM3OTIuNDMwMDAzLCJleHAiOjE3OTMxNjk3OTIuNDIyNjY5LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.SutcB4SMDdEzgfDZ1lnzGDjY4gf1aSwNJMGFnKF3RXWGbB4MhP6R8wRLCa2wtCMZ1egUQc1LxPMq2S9P-hfCeh4stj4W-zlWf0xOfvswvWVos14aP5RBTjbZ8Rc3PrmAwiFvnzu7PpRtqy8hGF_h1nt2dJrZpT4k0CIuvpUK8afLBKYfpr0i1NOx1awb2Z6Uo5YfJRQrB7y4wrs8TEsnbuAIdC4JG6-9Oi_TPwZ42SGatw4UEUvm09I3SjKGjZRDCOMJZLbSc1O4C6B53aK9hNQKCjnwsSXWc37h-cA6lKg-DSfm6K1usg0yHeAsE-2uya2_b8_TNq3LN4Mb04S820FmpnP0RqtDoPeoqBUSTacd0bSINimYpNv8NyiaO0D6k0J3HzaNd5MmwORyHpTNnVEaM8l0O5iyI-UIa3bPaOBnltamiAydE2EmUA9pfRQy3HWd6yZnxfuITM0THdj73ju1Qh3D0WVP7aUFR-3XUbp5qVflBZkiBe0klClG94ubWNFMX6vebixLQ21KsDvEDLj2Xy0hoLY-g6sm33l14NwbSiUgZ0VQI_3WbOzSpUvU0sprU7ozX7y7-nwjIXjshOQ5ymktZUMCN7LyCbvgX-qcGyxbS8C9JN9BhmvhagIBatDpPZw75arXlksHzxKnytbLG3BVxFHXxkp9jSBqW2s"
    private lateinit var categoryNameEditText: TextInputEditText
    private lateinit var categoryImageView: CircleImageView
    private lateinit var saveButton: TextView

    private var categoryId: String = ""
    private var categoryImageUri: Uri? = null
    private var categoryImageFile: File? = null

    private val IMAGE_PICK_CODE = 1001
    private val baseUrl = "https://dev.ginexpos.com/" // Replace with your server's base URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_category)

        categoryNameEditText = findViewById(R.id.EditCategoryName)
        categoryImageView = findViewById(R.id.EditCategoryImage)
        saveButton = findViewById(R.id.EditSaveButton)

        // Get data from intent
        categoryId = intent.getStringExtra("category_id") ?: ""
        val categoryName = intent.getStringExtra("category_name") ?: ""
        val categoryImage = intent.getStringExtra("category_image") ?: ""

        // Set category name
        categoryNameEditText.setText(categoryName)

        // Load existing image if exists
        // inside onCreate()
        if (categoryImage.isNotEmpty()) {
            // Use GlideUrl with Authorization header
            val glideUrl = GlideUrl(
                categoryImage,
                LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZGE0YjJmNjlmZGJkNjMwMDMyNGE3MWNkZWRhMDI2ZWI2YTIwMGM4NWIyNTI2MTNjOTZhZGIyMDA2MTE3YjMxMGI0MTFjYjczNzNmZmNlZDAiLCJpYXQiOjE3NjAzMjkzNTkuNDc3MTA1LCJuYmYiOjE3NjAzMjkzNTkuNDc3MTA4LCJleHAiOjE3OTE4NjUzNTkuNDcyNjI1LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.C3ySWdDX7BRHm4qzwWFZZofL_DEx3C2Qjy7iEUWxy9GdrL8OJS7m7Kk_Oe4HtFaT7DvPMEWE_c9kIC8RalMXflXTvPGKkfsw7yxdVxZOKSE20UNZiSbScAdvx3RxAz-XoHK4wJr7wepspLad5y5KCv4RyPXAJl8sIjFELfiCMoxt1CiYGp5_GhsOjbMeSLWSBoDwd3H4MLNvUyU2KN2zhvQaRRUh4T-L11mZgmd_8A8kWZbp_bO6AK-3hGHFGd7VaT2Xqoi4asmn0ABlxusVYWG6hw9UhnU-_uxOVFQLAHog-WKfbahCwfkssXtK07wMpk-ZGHfRn7ujbkrMAX5gNgNkcNQZPRMkUSrokHylEJXKC7UOAgUiK8fy32bIlmFuMQE9hTuuQjHWJ8hdEqtPaXVIcc1oXURtZhCWTp2APH9RE4_L41NYStog_bVMdXwRO_a6QEg_ex0moqxwtRZKivnIF4DKm6WLj45X0FLj-F7HTlZ-eoc9j3w_dVaVyhhxEKUiTyQSJ_AwVKMTbAUmxvWY3OnoIAmu4WYrbC4T4tA2cWoB9yXKna8Yfbil_vC46tLZweGF7RRZR2MPT16q-iCzKG73JqAMphV4NO7b-bMk6mhvgz8TR0_YUewsPg2CVvgdvEmnV4DE4znhnwiLMniN0kPGzF5pindkKTVNDb8"
                            )
                    .build()
            )

            Glide.with(this)
                .load(glideUrl)
                .placeholder(R.drawable.ic_placeholder) // local placeholder
                .error(R.drawable.ic_placeholder)
                .circleCrop() // fits CircleImageView
                .into(categoryImageView)
        }


        // Pick new image
        categoryImageView.setOnClickListener {
            pickImageFromGallery()
        }

        // Save category
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
            categoryImageFile = categoryImageUri?.let { uriToFile(it) }
            categoryImageView.setImageURI(categoryImageUri)
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "edit_upload_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        outputStream.close()
        inputStream?.close()
        return file
    }

    private fun uploadEditCategory(categoryId: String, categoryName: String) {

        val categoryIdBody = RequestBody.create("text/plain".toMediaTypeOrNull(), categoryId)
        val categoryNameBody = RequestBody.create("text/plain".toMediaTypeOrNull(), categoryName)
        val statusBody = RequestBody.create("text/plain".toMediaTypeOrNull(), "1")

        val imagePart: MultipartBody.Part? = categoryImageFile?.let {
            val reqFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("category_image", it.name, reqFile)
        }

        ApiClient.instance.uploadEditCategory(
            jwtToken = jwtToken,
            category_image = imagePart,
            category_id = categoryIdBody,
            category_name = categoryNameBody,
            status = statusBody
        ).enqueue(object : Callback<StatusResponse> {
            override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(this@EditCategoryActivity, "Category updated!", Toast.LENGTH_SHORT).show()
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
