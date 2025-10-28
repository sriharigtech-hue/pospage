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
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZTRjY2JlNDBmYWFiZmE5YTg1YmVkMDdjOTcyMzAzNWY3MTljNzQ4M2Q3YzIzMjg3M2E5ZjZmN2QyMjQ1Y2E3MjVhNjVlMTQ5ZGRiZjBkYjgiLCJpYXQiOjE3NjE2MzM3OTIuNDI5OTk5LCJuYmYiOjE3NjE2MzM3OTIuNDMwMDAzLCJleHAiOjE3OTMxNjk3OTIuNDIyNjY5LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.SutcB4SMDdEzgfDZ1lnzGDjY4gf1aSwNJMGFnKF3RXWGbB4MhP6R8wRLCa2wtCMZ1egUQc1LxPMq2S9P-hfCeh4stj4W-zlWf0xOfvswvWVos14aP5RBTjbZ8Rc3PrmAwiFvnzu7PpRtqy8hGF_h1nt2dJrZpT4k0CIuvpUK8afLBKYfpr0i1NOx1awb2Z6Uo5YfJRQrB7y4wrs8TEsnbuAIdC4JG6-9Oi_TPwZ42SGatw4UEUvm09I3SjKGjZRDCOMJZLbSc1O4C6B53aK9hNQKCjnwsSXWc37h-cA6lKg-DSfm6K1usg0yHeAsE-2uya2_b8_TNq3LN4Mb04S820FmpnP0RqtDoPeoqBUSTacd0bSINimYpNv8NyiaO0D6k0J3HzaNd5MmwORyHpTNnVEaM8l0O5iyI-UIa3bPaOBnltamiAydE2EmUA9pfRQy3HWd6yZnxfuITM0THdj73ju1Qh3D0WVP7aUFR-3XUbp5qVflBZkiBe0klClG94ubWNFMX6vebixLQ21KsDvEDLj2Xy0hoLY-g6sm33l14NwbSiUgZ0VQI_3WbOzSpUvU0sprU7ozX7y7-nwjIXjshOQ5ymktZUMCN7LyCbvgX-qcGyxbS8C9JN9BhmvhagIBatDpPZw75arXlksHzxKnytbLG3BVxFHXxkp9jSBqW2s"
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
