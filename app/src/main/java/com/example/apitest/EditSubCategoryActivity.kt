package com.example.apitest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.apitest.dataModel.*
import com.example.apitest.network.ApiClient
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditSubCategoryActivity : AppCompatActivity() {
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiNmZiMTgzMGFlYzFkNzUzOTdiNDBjMmE3NWJhOWRmZWU4MTU3YWMxZmZlMTg2NGYxZWMwOTYyYzA1NjE1YThjNTlkZjg3MjVjNDlmNmJmZTYiLCJpYXQiOjE3NTg2MTM2MzguMzQyMTUzLCJuYmYiOjE3NTg2MTM2MzguMzQyMTU3LCJleHAiOjE3OTAxNDk2MzguMzM2ODI2LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.QiPgcG55zwKdr_U471IaYnBQkUk437w5vRrmjxWos1zRADMFKLuovtJ8IqoTzbkQ1GMWE0fr6c22cdBq9Eq4PlY71A8ocqua6rhGKTS5h7ziYjA8y_KNRSmvWeSfrYF59FCv_sl_Yi8mci_Gl6lzLV5yzf-gQOcmxQ0m4NifHTxCZZEOXloSS0V2KtiECV1MwATuDGLGv7QYAwte7XEIZiCFjTJUjGGKdcXHGdCPXnsnlSSzyYUCuljabM4Of3dnP6QV6YVEuRkAiSn7HvyqgNpi34ux4lsVXFMWy1qnbI-VI_fo2Vcf5uZa8B4KBYVNT7YkV2_KcxEsdZ-ZhRzGbm6erYvUuwWUTLj8DHRUHfH-s1sOO4j3u8SQeVV47OfXB6Wo-CghPzuMQBtTvoQe2_zgAV9QrCS-xsk-uJxHLxp_dao9igrzB6fskUGshJ70IKLatiF3IDXuyVLZNqOJUblUYpoyvKuj4dBa-BZUnS35m9jm5Rz5XCkeIMIm26VTvwiNcuTg0cAXhpe97yZ_Ir0FV7-8YNQCPTAtCrC4IA0r6cuLTkWjZWa-gokQKbMWble8R9VoP-OIm0zFbISJUBtI-B0cif3oxTwr7m12jubPutnFl_HZyREBOticvsTf6q48cKUxNSYeyOW3aMdbZ584yRv95UYKUou0k0LbezM"

    private lateinit var subCategoryNameEdit: TextInputEditText
    private lateinit var categorySpinner: Spinner
    private lateinit var saveButton: TextView

    private var subCategoryId: Int? = null
    private var selectedCategoryId: Int? = null
    private var categories: List<Category> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_sub_category)

        subCategoryNameEdit = findViewById(R.id.EditSubCategoryName)
        categorySpinner = findViewById(R.id.categorySpinner)
        saveButton = findViewById(R.id.EditSaveButton)

        // Get intent data
        subCategoryId = intent.getIntExtra("subcategory_id", 0)
        selectedCategoryId = intent.getIntExtra("category_id", 0)
        val subCategoryName = intent.getStringExtra("subcategory_name")

        subCategoryNameEdit.setText(subCategoryName)

        // Load categories
        fetchCategories()

        saveButton.setOnClickListener {
            val updatedName = subCategoryNameEdit.text.toString().trim()
            if (updatedName.isEmpty() || selectedCategoryId == null) {
                Toast.makeText(this, "Please enter name and select category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            editSubCategory(updatedName, selectedCategoryId!!, subCategoryId!!)
        }

        findViewById<View>(R.id.backButton)?.setOnClickListener { finish() }
    }

    private fun fetchCategories() {

        val input = CategoryInput(status = "1")


        ApiClient.instance.stockCategoryApi(jwtToken, input)?.enqueue(object : Callback<CategoryListOutput?> {
            override fun onResponse(call: Call<CategoryListOutput?>, response: Response<CategoryListOutput?>) {
                if (response.isSuccessful && response.body()?.data != null) {
                    categories = response.body()!!.data!!
                    setupSpinner()
                } else {
                    Toast.makeText(this@EditSubCategoryActivity, "No categories found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CategoryListOutput?>, t: Throwable) {
                Toast.makeText(this@EditSubCategoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSpinner() {
        val categoryNames = categories.map { it.category_name ?: "N/A" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        val index = categories.indexOfFirst { it.category_id == selectedCategoryId }
        if (index != -1) categorySpinner.setSelection(index)

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategoryId = categories[position].category_id
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun editSubCategory(updatedName: String, categoryId: Int,subcategoryId: Int) {

        val input = InputField(
            sub_category_id = subCategoryId,
            categoryId = categoryId,
            subCategoryName = updatedName,
            status = "1"
        )

        ApiClient.instance.editSubCategory(jwtToken, input)?.enqueue(object : Callback<StatusResponse?> {
            override fun onResponse(call: Call<StatusResponse?>, response: Response<StatusResponse?>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(this@EditSubCategoryActivity, "Updated Successfully", Toast.LENGTH_SHORT).show()

                    // Send result back
                    val resultIntent = Intent().apply {
                        putExtra("updated_subcategory_id", subCategoryId)
                        putExtra("updated_subcategory_name", updatedName)
                        putExtra("updated_category_id", categoryId)
                        putExtra("updated_category_name", selectedCategoryId)

                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this@EditSubCategoryActivity, response.body()?.message ?: "Failed to update", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StatusResponse?>, t: Throwable) {
                Toast.makeText(this@EditSubCategoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
