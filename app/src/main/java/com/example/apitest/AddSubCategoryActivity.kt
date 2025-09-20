package com.example.apitest

import android.app.Activity
import android.os.Bundle
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
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiNjk3MmE4MjVjMjZhNmU2ZDAxMTk3NjdiNDMzYzc4NWEzNTdmYzYxYTZhMTBjNjQ2MGViYTc4ZWQxNDM4NmQyYjM2YTFkNWJjZTIwZTc0MmIiLCJpYXQiOjE3NTgyNTU4MTEuMjA1Njg1LCJuYmYiOjE3NTgyNTU4MTEuMjA1Njg3LCJleHAiOjE3ODk3OTE4MTEuMjAyMzI1LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.bWv1-ccFjlnUbHqgVKleTigmPBsohLqMMJ6KNx-zPgEsHzPvFn19WQV6kc85aXrKZOdbiazq4eb7y9I1wdXI8n-BQQ4jpqruQqW87KNzvuoOvd0qPKW-EbUFbst1I5QAgA4m0kySj8JxVHAFSgBtzocj42JUd0XuNHMtGjwLUH4QM4Njc-tkVSmVqIHN66LGoaslfkl5tQTxjFV0Xg1Ay1fyNdp5A1pSZPv4wTr9aAfR1nhrqA5FtU8x0BJyWcpk3ojQq3gWUB3CZgr0Qq7tMpzuZwufR-HWqCX-Be4YRZ1wyANAQHEt0JEp5HLV9htpfuCE7grP_sw-cywep-FxVlyKO0tyc7ykFnhOaI6YlREAms4m_NOpdleSnYv9or7uj8DQWI2F4318SoFSPFu1UsVI9n2Ygf54TZD4FDhMGjWSue2uBCS3HPleSyO6Qf2Lk64OovnYRXc_tJzddcvu8LEC8ihvZpDpr4KMmxGIM15c-4lh0gXpcOOPZXmHG4rG73ogFxVzJdp-nAs-h2U-Kh52kXmjFm2MAgfKSv-0IdgA2IXUxcWthRLO5WYtiggR-ghkTx9hy6Seyq5ZXYSmdnbJHcVRHyZYE6h7hl2pgeAIA3_er4oT_2DpmEW38pwoM__ezWYarjyPYkIEI2enMQTJE0FbCU7fe5wcuSMY140"
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

        val input = InputField(
            subCategoryName = subCategoryName,
            categoryId = categoryId,
            status = "1",

        )

        ApiClient.instance.addSubCategory(jwtToken, input)
            ?.enqueue(object : Callback<StatusResponse?> {
                override fun onResponse(call: Call<StatusResponse?>, response: Response<StatusResponse?>) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        Toast.makeText(this@AddSubCategoryActivity, "Sub Category added successfully", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK )
                                finish() // Close activity
                    } else {
                        Toast.makeText(this@AddSubCategoryActivity, response.body()?.message ?: "Failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<StatusResponse?>, t: Throwable) {
                    Toast.makeText(this@AddSubCategoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
