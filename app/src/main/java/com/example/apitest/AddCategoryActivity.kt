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
        val token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiYjE1YTc2MWYyMTA3NTJiYTY2MjBiMDg4NTUzYmRiMGYxYWVhZmEwOTJlMWY4MGI1YmFjYmExNzA4MDFlN2Y2NGU1NDEyODQyMzBjYTJiM2YiLCJpYXQiOjE3NTY4MTA5MzUuNjIwMDU5LCJuYmYiOjE3NTY4MTA5MzUuNjIwMDYyLCJleHAiOjE3ODgzNDY5MzUuNjE2NjA2LCJzdWIiOiI2MCIsInNjb3BlcyI6W119.UzX5UwID0Xd9Ia9ZIahOq7ugA8k8viEIOX261q2H2rhR7vMGZHTmm6ymDhdWKmmtSN0fTmU8AijKQzHN4g9HRZvr9seeEHQN3doBFT4odcCbufig4LEH2E0oMjQMmOIYEIjbj-n8o5i2lcqOfchu3vCrDt6McE7GBuPzTA87wyQpMPyO4IAUKU7h7TnwVx3VB_Y8aAUR5DpLr9-LQ7PpOG_hPUvqfUJ3jLoaluDAXA-1hPQ8EXKRz15xAfQxHLR0LLNOCf31hIj7JOxJQFDU-wzXs9g-bH6aAPOY2Q5tye-JZPoLNDCFRxNIkK7HgddkFdH7w0DnW2r4s4vjtfKe0Ubc0aOAxgE74OS_50rw-QEZjl0SceQhoNqeSgSH43_JSjAWX5-VxlwNBgGBBVMXBsKUt52S_eVyyOJpA_qXCqFjVqmWh-MPgo55-dEHw9FFc1ptvzY6FUeYnwBs4Kd65SZyFfU0Esx9wqtq5ZarZDR-gfZUlaP-toKzOzpAs7QasunG62nHDBdUX4P-EdCDFjQdroxeX983vJGe_GzYj5sBRZauXsqg0wtvZmAR2qaxzK4HB853hs7BJn0QAQRZIM7qERV0VqgynnWIrPFhV1WDyU_NMxPQh3WM6jqICK30uqiD7-201ga-522-Dnbxd1vF8CQIFGMRgiwikl3cfoI"


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
        ApiClient.instance.uploadCategory(token, imagePart, nameBody, statusBody)
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
