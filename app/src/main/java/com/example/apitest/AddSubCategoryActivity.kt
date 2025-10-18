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
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZGE0YjJmNjlmZGJkNjMwMDMyNGE3MWNkZWRhMDI2ZWI2YTIwMGM4NWIyNTI2MTNjOTZhZGIyMDA2MTE3YjMxMGI0MTFjYjczNzNmZmNlZDAiLCJpYXQiOjE3NjAzMjkzNTkuNDc3MTA1LCJuYmYiOjE3NjAzMjkzNTkuNDc3MTA4LCJleHAiOjE3OTE4NjUzNTkuNDcyNjI1LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.C3ySWdDX7BRHm4qzwWFZZofL_DEx3C2Qjy7iEUWxy9GdrL8OJS7m7Kk_Oe4HtFaT7DvPMEWE_c9kIC8RalMXflXTvPGKkfsw7yxdVxZOKSE20UNZiSbScAdvx3RxAz-XoHK4wJr7wepspLad5y5KCv4RyPXAJl8sIjFELfiCMoxt1CiYGp5_GhsOjbMeSLWSBoDwd3H4MLNvUyU2KN2zhvQaRRUh4T-L11mZgmd_8A8kWZbp_bO6AK-3hGHFGd7VaT2Xqoi4asmn0ABlxusVYWG6hw9UhnU-_uxOVFQLAHog-WKfbahCwfkssXtK07wMpk-ZGHfRn7ujbkrMAX5gNgNkcNQZPRMkUSrokHylEJXKC7UOAgUiK8fy32bIlmFuMQE9hTuuQjHWJ8hdEqtPaXVIcc1oXURtZhCWTp2APH9RE4_L41NYStog_bVMdXwRO_a6QEg_ex0moqxwtRZKivnIF4DKm6WLj45X0FLj-F7HTlZ-eoc9j3w_dVaVyhhxEKUiTyQSJ_AwVKMTbAUmxvWY3OnoIAmu4WYrbC4T4tA2cWoB9yXKna8Yfbil_vC46tLZweGF7RRZR2MPT16q-iCzKG73JqAMphV4NO7b-bMk6mhvgz8TR0_YUewsPg2CVvgdvEmnV4DE4znhnwiLMniN0kPGzF5pindkKTVNDb8"
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
