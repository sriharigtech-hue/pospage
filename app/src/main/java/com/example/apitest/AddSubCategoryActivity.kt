package com.example.apitest

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apitest.dataModel.Category
import com.example.apitest.dataModel.CategoryInput
import com.example.apitest.dataModel.CategoryListOutput
import com.example.apitest.dataModel.InputField
import com.example.apitest.dataModel.StatusResponse
import com.example.apitest.network.ApiClient
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddSubCategoryActivity : AppCompatActivity() {
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZTRjY2JlNDBmYWFiZmE5YTg1YmVkMDdjOTcyMzAzNWY3MTljNzQ4M2Q3YzIzMjg3M2E5ZjZmN2QyMjQ1Y2E3MjVhNjVlMTQ5ZGRiZjBkYjgiLCJpYXQiOjE3NjE2MzM3OTIuNDI5OTk5LCJuYmYiOjE3NjE2MzM3OTIuNDMwMDAzLCJleHAiOjE3OTMxNjk3OTIuNDIyNjY5LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.SutcB4SMDdEzgfDZ1lnzGDjY4gf1aSwNJMGFnKF3RXWGbB4MhP6R8wRLCa2wtCMZ1egUQc1LxPMq2S9P-hfCeh4stj4W-zlWf0xOfvswvWVos14aP5RBTjbZ8Rc3PrmAwiFvnzu7PpRtqy8hGF_h1nt2dJrZpT4k0CIuvpUK8afLBKYfpr0i1NOx1awb2Z6Uo5YfJRQrB7y4wrs8TEsnbuAIdC4JG6-9Oi_TPwZ42SGatw4UEUvm09I3SjKGjZRDCOMJZLbSc1O4C6B53aK9hNQKCjnwsSXWc37h-cA6lKg-DSfm6K1usg0yHeAsE-2uya2_b8_TNq3LN4Mb04S820FmpnP0RqtDoPeoqBUSTacd0bSINimYpNv8NyiaO0D6k0J3HzaNd5MmwORyHpTNnVEaM8l0O5iyI-UIa3bPaOBnltamiAydE2EmUA9pfRQy3HWd6yZnxfuITM0THdj73ju1Qh3D0WVP7aUFR-3XUbp5qVflBZkiBe0klClG94ubWNFMX6vebixLQ21KsDvEDLj2Xy0hoLY-g6sm33l14NwbSiUgZ0VQI_3WbOzSpUvU0sprU7ozX7y7-nwjIXjshOQ5ymktZUMCN7LyCbvgX-qcGyxbS8C9JN9BhmvhagIBatDpPZw75arXlksHzxKnytbLG3BVxFHXxkp9jSBqW2s"
    private lateinit var categorySpinner: Spinner




    private lateinit var subCategoryNameEt: TextInputEditText

    private var categoryList = mutableListOf<Category>()
    private var selectedCategoryId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_sub_category)
        // Back button click
        findViewById<RelativeLayout>(R.id.backButton).setOnClickListener {
            finish() // Closes this activity and returns to previous screen
        }



        categorySpinner = findViewById(R.id.categorySpinner)
        subCategoryNameEt = findViewById(R.id.AddSubCategoryName)

        fetchCategories()

        findViewById<View>(R.id.saveButton).setOnClickListener {
            val subCategoryName = subCategoryNameEt.text.toString().trim()
            if (subCategoryName.isEmpty()) {
                Toast.makeText(this, "Enter Sub Category Name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedCategoryId == null) {
                Toast.makeText(this, "Select a Category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            addSubCategory(subCategoryName, selectedCategoryId!!)
        }
    }

    private fun fetchCategories() {
        val input = CategoryInput(status = "1")

        ApiClient.instance.stockCategoryApi(jwtToken, input)
            .enqueue(object : Callback<CategoryListOutput> {
                override fun onResponse(
                    call: Call<CategoryListOutput>,
                    response: Response<CategoryListOutput>
                ) {
                    if (response.isSuccessful ) {
                        categoryList.clear()
                        categoryList.addAll(response.body()!!.data!!)
                        setupCategorySpinner()
                    } else {
                        Toast.makeText(this@AddSubCategoryActivity, "No categories found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CategoryListOutput>, t: Throwable) {
                    Toast.makeText(this@AddSubCategoryActivity, "Error fails", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupCategorySpinner() {
        val categoryNames = categoryList.map { it.category_name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter


        // Default selection: first category
        if (categoryList.isNotEmpty()) {
            categorySpinner.setSelection(0)
            selectedCategoryId = categoryList[0].category_id
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategoryId = categoryList[position].category_id
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun addSubCategory(subCategoryName: String, categoryId: Int) {
        // Log the values before sending
        Log.d("AddSubCategory", "Adding SubCategory: $subCategoryName under CategoryId: $categoryId")

        val input = InputField(
            subCategoryName = subCategoryName,
            categoryId = categoryId,  // Ensure this is passed correctly
            status = "1",
        )

        // Log the request body
        Log.d("AddSubCategory", "Request Body: $input")

        ApiClient.instance.addSubCategory(jwtToken, input)
            ?.enqueue(object : Callback<StatusResponse?> {
                override fun onResponse(call: Call<StatusResponse?>, response: Response<StatusResponse?>) {
                    if (response.isSuccessful) {
                        Log.d("AddSubCategory", "Response Code: ${response.code()}")
                        Log.d("AddSubCategory", "Response Body: ${response.body()}")
                        if (response.body()?.status == true) {
                            Toast.makeText(this@AddSubCategoryActivity, "Sub Category added successfully", Toast.LENGTH_SHORT).show()
                            setResult(Activity.RESULT_OK)
                            finish()
                        } else {
                            Toast.makeText(this@AddSubCategoryActivity, response.body()?.message ?: "Failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.d("AddSubCategory", "Error Response: ${response.errorBody()?.string()}")
                        Toast.makeText(this@AddSubCategoryActivity, "Failed to add Sub Category", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<StatusResponse?>, t: Throwable) {
                    Log.e("AddSubCategory", "API Failure", t)
                    Toast.makeText(this@AddSubCategoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

}
