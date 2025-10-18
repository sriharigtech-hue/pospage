package com.example.apitest


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apitest.adapter.PhotoAdapter
import com.example.apitest.dataModel.StatusResponse
import com.example.apitest.databinding.ActivityAddCategoryBinding
import com.example.apitest.network.ApiClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class AddCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCategoryBinding
    private var selectedImageUri: Uri? = null
    private var selectedImageFile: File? = null   // ✅ store converted file
    private val PICK_IMAGE_REQUEST = 101
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZGE0YjJmNjlmZGJkNjMwMDMyNGE3MWNkZWRhMDI2ZWI2YTIwMGM4NWIyNTI2MTNjOTZhZGIyMDA2MTE3YjMxMGI0MTFjYjczNzNmZmNlZDAiLCJpYXQiOjE3NjAzMjkzNTkuNDc3MTA1LCJuYmYiOjE3NjAzMjkzNTkuNDc3MTA4LCJleHAiOjE3OTE4NjUzNTkuNDcyNjI1LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.C3ySWdDX7BRHm4qzwWFZZofL_DEx3C2Qjy7iEUWxy9GdrL8OJS7m7Kk_Oe4HtFaT7DvPMEWE_c9kIC8RalMXflXTvPGKkfsw7yxdVxZOKSE20UNZiSbScAdvx3RxAz-XoHK4wJr7wepspLad5y5KCv4RyPXAJl8sIjFELfiCMoxt1CiYGp5_GhsOjbMeSLWSBoDwd3H4MLNvUyU2KN2zhvQaRRUh4T-L11mZgmd_8A8kWZbp_bO6AK-3hGHFGd7VaT2Xqoi4asmn0ABlxusVYWG6hw9UhnU-_uxOVFQLAHog-WKfbahCwfkssXtK07wMpk-ZGHfRn7ujbkrMAX5gNgNkcNQZPRMkUSrokHylEJXKC7UOAgUiK8fy32bIlmFuMQE9hTuuQjHWJ8hdEqtPaXVIcc1oXURtZhCWTp2APH9RE4_L41NYStog_bVMdXwRO_a6QEg_ex0moqxwtRZKivnIF4DKm6WLj45X0FLj-F7HTlZ-eoc9j3w_dVaVyhhxEKUiTyQSJ_AwVKMTbAUmxvWY3OnoIAmu4WYrbC4T4tA2cWoB9yXKna8Yfbil_vC46tLZweGF7RRZR2MPT16q-iCzKG73JqAMphV4NO7b-bMk6mhvgz8TR0_YUewsPg2CVvgdvEmnV4DE4znhnwiLMniN0kPGzF5pindkKTVNDb8"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        // Pick image
        binding.AddImageLayout.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Save category
        binding.saveButton.setOnClickListener {
            val categoryName = binding.categoryName.text.toString().trim()
            if (categoryName.isEmpty()) {
                Toast.makeText(this, "Please enter category name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedImageFile == null) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            uploadCategory(categoryName)
        }


    }


    // Handle image picker result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data ?: return
            validateAndSetImage(uri)
        }
    }


    // Validate image type & size
    private fun validateAndSetImage(uri: Uri) {
        val type = contentResolver.getType(uri) ?: ""
        if (!type.startsWith("image/")) {
            Toast.makeText(this, "Only image files are allowed", Toast.LENGTH_SHORT).show()
            return
        }

        val fileSize = getFileSize(uri)
        if (fileSize > 100 * 1024) { // 100 KB
            Toast.makeText(this, "Image too large! Must be under 100KB", Toast.LENGTH_SHORT).show()
            return
        }


        // Convert URI → File
        selectedImageFile = uriToFile(uri)

        // Show preview
        selectedImageUri = uri
        binding.photoLayout.visibility = View.VISIBLE
        binding.photosRecyclerView.adapter = PhotoAdapter(listOf(uri))
    }



    // Get file size from URI
    private fun getFileSize(uri: Uri): Long {
        var size: Long = 0
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            if (it.moveToFirst()) {
                size = it.getLong(sizeIndex)
            }
        }
        return size
    }


    // Convert URI to File (copy to cache)
    private fun uriToFile(uri: Uri): File {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        outputStream.close()
        inputStream?.close()
        return file
    }

    // Upload category with image
    private fun uploadCategory(name: String) {


        if (selectedImageFile == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            return
        }

        // Image part
        val requestFile = selectedImageFile!!.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("category_image", selectedImageFile!!.name, requestFile)


        // Text parts
        val nameBody = name.toRequestBody("text/plain".toMediaType())
        val statusBody = "1".toRequestBody("text/plain".toMediaType())

        // API call
        ApiClient.instance.uploadCategory(jwtToken, imagePart, nameBody, statusBody)
            .enqueue(object : Callback<StatusResponse> {
                override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        Toast.makeText(this@AddCategoryActivity, "Category saved", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this@AddCategoryActivity, "Failed: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                    Toast.makeText(this@AddCategoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
