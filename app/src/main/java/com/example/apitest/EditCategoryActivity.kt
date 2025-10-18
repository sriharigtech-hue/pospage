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
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZGE0YjJmNjlmZGJkNjMwMDMyNGE3MWNkZWRhMDI2ZWI2YTIwMGM4NWIyNTI2MTNjOTZhZGIyMDA2MTE3YjMxMGI0MTFjYjczNzNmZmNlZDAiLCJpYXQiOjE3NjAzMjkzNTkuNDc3MTA1LCJuYmYiOjE3NjAzMjkzNTkuNDc3MTA4LCJleHAiOjE3OTE4NjUzNTkuNDcyNjI1LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.C3ySWdDX7BRHm4qzwWFZZofL_DEx3C2Qjy7iEUWxy9GdrL8OJS7m7Kk_Oe4HtFaT7DvPMEWE_c9kIC8RalMXflXTvPGKkfsw7yxdVxZOKSE20UNZiSbScAdvx3RxAz-XoHK4wJr7wepspLad5y5KCv4RyPXAJl8sIjFELfiCMoxt1CiYGp5_GhsOjbMeSLWSBoDwd3H4MLNvUyU2KN2zhvQaRRUh4T-L11mZgmd_8A8kWZbp_bO6AK-3hGHFGd7VaT2Xqoi4asmn0ABlxusVYWG6hw9UhnU-_uxOVFQLAHog-WKfbahCwfkssXtK07wMpk-ZGHfRn7ujbkrMAX5gNgNkcNQZPRMkUSrokHylEJXKC7UOAgUiK8fy32bIlmFuMQE9hTuuQjHWJ8hdEqtPaXVIcc1oXURtZhCWTp2APH9RE4_L41NYStog_bVMdXwRO_a6QEg_ex0moqxwtRZKivnIF4DKm6WLj45X0FLj-F7HTlZ-eoc9j3w_dVaVyhhxEKUiTyQSJ_AwVKMTbAUmxvWY3OnoIAmu4WYrbC4T4tA2cWoB9yXKna8Yfbil_vC46tLZweGF7RRZR2MPT16q-iCzKG73JqAMphV4NO7b-bMk6mhvgz8TR0_YUewsPg2CVvgdvEmnV4DE4znhnwiLMniN0kPGzF5pindkKTVNDb8"

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
                    .addHeader("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiOTMyZGFhM2IyNGZmM2Q0ZTY5NmQwYTdlODdhNmY1Y2ViOTA1Y2VhMzc0NWU5NmFlMTBhZWEwZjY2MDkxZDZiMWI1MjIwYjIwYmU4N2EyNDEiLCJpYXQiOjE3NTg3OTI2NDEuMjExMDEsIm5iZiI6MTc1ODc5MjY0MS4yMTEwMTMsImV4cCI6MTc5MDMyODY0MS4yMDQ5NjYsInN1YiI6IjYiLCJzY29wZXMiOltdfQ.lcZz0mc3u-to55t0p23p5g_Z1NQHM23K6xT8vaRAr7X9qHsMbyN9OEq-KuHhZGaqikcY-7ak26uM3_mtoLpLfq6jivhUPFC06JL7ID3HRHOUsWY05CqjIwPpOfjop127eWyz1CYmBmZx3mKsSuE2rvNK91OsROryf1Dz-Px0SnVJ1uDJGK9y1zsJ_Mi8wY7WIH_E3cBusI7uSgNHTQq7dh6GurCYymPxnvvR8cdMQ4Om9SnmfqX9f3GCUncHXBVfxYCuH6ElLsjq74Z9ZXRGBLdwdM4BJWiyv4jzKfU80LmErPo7XT90DPzqa40T0pRblACQsaSGLn64TBoKvxlsO4HjJV8nBg3az5PrCkDsj8QTwgPLzJtP7WcT3pvcCI6O3O8OKlL2lR2-csFHNzCHetHaT1fmOLnVWuc5YIjqhYFEOjp8IKVhzxmcocxsd3R8bcvrjR8NcutOX5H7zmfD-GX17f64RT2c0zqSRdVpRxFZYlNycxd3rI591w9ImgZSkeGQN4Eg8us8oqmlRqfF5mO7QZXi_OsjJgnMdovqP1NB1IxHuTHQIyfESkQ3DoA_KYBF4_8DXhyjvE2D5_SQfZitUpjSwfWqZ_ghgVoOdLJokz4TBJQ9j_ec4jK3uf3nCwS_6Evx9zwbZxmin2CpnIrg4lFRmMpO6YgLfYKPqoI"
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
