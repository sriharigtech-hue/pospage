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
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiOTMyZGFhM2IyNGZmM2Q0ZTY5NmQwYTdlODdhNmY1Y2ViOTA1Y2VhMzc0NWU5NmFlMTBhZWEwZjY2MDkxZDZiMWI1MjIwYjIwYmU4N2EyNDEiLCJpYXQiOjE3NTg3OTI2NDEuMjExMDEsIm5iZiI6MTc1ODc5MjY0MS4yMTEwMTMsImV4cCI6MTc5MDMyODY0MS4yMDQ5NjYsInN1YiI6IjYiLCJzY29wZXMiOltdfQ.lcZz0mc3u-to55t0p23p5g_Z1NQHM23K6xT8vaRAr7X9qHsMbyN9OEq-KuHhZGaqikcY-7ak26uM3_mtoLpLfq6jivhUPFC06JL7ID3HRHOUsWY05CqjIwPpOfjop127eWyz1CYmBmZx3mKsSuE2rvNK91OsROryf1Dz-Px0SnVJ1uDJGK9y1zsJ_Mi8wY7WIH_E3cBusI7uSgNHTQq7dh6GurCYymPxnvvR8cdMQ4Om9SnmfqX9f3GCUncHXBVfxYCuH6ElLsjq74Z9ZXRGBLdwdM4BJWiyv4jzKfU80LmErPo7XT90DPzqa40T0pRblACQsaSGLn64TBoKvxlsO4HjJV8nBg3az5PrCkDsj8QTwgPLzJtP7WcT3pvcCI6O3O8OKlL2lR2-csFHNzCHetHaT1fmOLnVWuc5YIjqhYFEOjp8IKVhzxmcocxsd3R8bcvrjR8NcutOX5H7zmfD-GX17f64RT2c0zqSRdVpRxFZYlNycxd3rI591w9ImgZSkeGQN4Eg8us8oqmlRqfF5mO7QZXi_OsjJgnMdovqP1NB1IxHuTHQIyfESkQ3DoA_KYBF4_8DXhyjvE2D5_SQfZitUpjSwfWqZ_ghgVoOdLJokz4TBJQ9j_ec4jK3uf3nCwS_6Evx9zwbZxmin2CpnIrg4lFRmMpO6YgLfYKPqoI"
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
