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
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiNmZiMTgzMGFlYzFkNzUzOTdiNDBjMmE3NWJhOWRmZWU4MTU3YWMxZmZlMTg2NGYxZWMwOTYyYzA1NjE1YThjNTlkZjg3MjVjNDlmNmJmZTYiLCJpYXQiOjE3NTg2MTM2MzguMzQyMTUzLCJuYmYiOjE3NTg2MTM2MzguMzQyMTU3LCJleHAiOjE3OTAxNDk2MzguMzM2ODI2LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.QiPgcG55zwKdr_U471IaYnBQkUk437w5vRrmjxWos1zRADMFKLuovtJ8IqoTzbkQ1GMWE0fr6c22cdBq9Eq4PlY71A8ocqua6rhGKTS5h7ziYjA8y_KNRSmvWeSfrYF59FCv_sl_Yi8mci_Gl6lzLV5yzf-gQOcmxQ0m4NifHTxCZZEOXloSS0V2KtiECV1MwATuDGLGv7QYAwte7XEIZiCFjTJUjGGKdcXHGdCPXnsnlSSzyYUCuljabM4Of3dnP6QV6YVEuRkAiSn7HvyqgNpi34ux4lsVXFMWy1qnbI-VI_fo2Vcf5uZa8B4KBYVNT7YkV2_KcxEsdZ-ZhRzGbm6erYvUuwWUTLj8DHRUHfH-s1sOO4j3u8SQeVV47OfXB6Wo-CghPzuMQBtTvoQe2_zgAV9QrCS-xsk-uJxHLxp_dao9igrzB6fskUGshJ70IKLatiF3IDXuyVLZNqOJUblUYpoyvKuj4dBa-BZUnS35m9jm5Rz5XCkeIMIm26VTvwiNcuTg0cAXhpe97yZ_Ir0FV7-8YNQCPTAtCrC4IA0r6cuLTkWjZWa-gokQKbMWble8R9VoP-OIm0zFbISJUBtI-B0cif3oxTwr7m12jubPutnFl_HZyREBOticvsTf6q48cKUxNSYeyOW3aMdbZ584yRv95UYKUou0k0LbezM"

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
                    .addHeader("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiNmZiMTgzMGFlYzFkNzUzOTdiNDBjMmE3NWJhOWRmZWU4MTU3YWMxZmZlMTg2NGYxZWMwOTYyYzA1NjE1YThjNTlkZjg3MjVjNDlmNmJmZTYiLCJpYXQiOjE3NTg2MTM2MzguMzQyMTUzLCJuYmYiOjE3NTg2MTM2MzguMzQyMTU3LCJleHAiOjE3OTAxNDk2MzguMzM2ODI2LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.QiPgcG55zwKdr_U471IaYnBQkUk437w5vRrmjxWos1zRADMFKLuovtJ8IqoTzbkQ1GMWE0fr6c22cdBq9Eq4PlY71A8ocqua6rhGKTS5h7ziYjA8y_KNRSmvWeSfrYF59FCv_sl_Yi8mci_Gl6lzLV5yzf-gQOcmxQ0m4NifHTxCZZEOXloSS0V2KtiECV1MwATuDGLGv7QYAwte7XEIZiCFjTJUjGGKdcXHGdCPXnsnlSSzyYUCuljabM4Of3dnP6QV6YVEuRkAiSn7HvyqgNpi34ux4lsVXFMWy1qnbI-VI_fo2Vcf5uZa8B4KBYVNT7YkV2_KcxEsdZ-ZhRzGbm6erYvUuwWUTLj8DHRUHfH-s1sOO4j3u8SQeVV47OfXB6Wo-CghPzuMQBtTvoQe2_zgAV9QrCS-xsk-uJxHLxp_dao9igrzB6fskUGshJ70IKLatiF3IDXuyVLZNqOJUblUYpoyvKuj4dBa-BZUnS35m9jm5Rz5XCkeIMIm26VTvwiNcuTg0cAXhpe97yZ_Ir0FV7-8YNQCPTAtCrC4IA0r6cuLTkWjZWa-gokQKbMWble8R9VoP-OIm0zFbISJUBtI-B0cif3oxTwr7m12jubPutnFl_HZyREBOticvsTf6q48cKUxNSYeyOW3aMdbZ584yRv95UYKUou0k0LbezM"
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
