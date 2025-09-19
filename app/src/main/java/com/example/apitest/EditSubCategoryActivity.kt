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
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiNjk3MmE4MjVjMjZhNmU2ZDAxMTk3NjdiNDMzYzc4NWEzNTdmYzYxYTZhMTBjNjQ2MGViYTc4ZWQxNDM4NmQyYjM2YTFkNWJjZTIwZTc0MmIiLCJpYXQiOjE3NTgyNTU4MTEuMjA1Njg1LCJuYmYiOjE3NTgyNTU4MTEuMjA1Njg3LCJleHAiOjE3ODk3OTE4MTEuMjAyMzI1LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.bWv1-ccFjlnUbHqgVKleTigmPBsohLqMMJ6KNx-zPgEsHzPvFn19WQV6kc85aXrKZOdbiazq4eb7y9I1wdXI8n-BQQ4jpqruQqW87KNzvuoOvd0qPKW-EbUFbst1I5QAgA4m0kySj8JxVHAFSgBtzocj42JUd0XuNHMtGjwLUH4QM4Njc-tkVSmVqIHN66LGoaslfkl5tQTxjFV0Xg1Ay1fyNdp5A1pSZPv4wTr9aAfR1nhrqA5FtU8x0BJyWcpk3ojQq3gWUB3CZgr0Qq7tMpzuZwufR-HWqCX-Be4YRZ1wyANAQHEt0JEp5HLV9htpfuCE7grP_sw-cywep-FxVlyKO0tyc7ykFnhOaI6YlREAms4m_NOpdleSnYv9or7uj8DQWI2F4318SoFSPFu1UsVI9n2Ygf54TZD4FDhMGjWSue2uBCS3HPleSyO6Qf2Lk64OovnYRXc_tJzddcvu8LEC8ihvZpDpr4KMmxGIM15c-4lh0gXpcOOPZXmHG4rG73ogFxVzJdp-nAs-h2U-Kh52kXmjFm2MAgfKSv-0IdgA2IXUxcWthRLO5WYtiggR-ghkTx9hy6Seyq5ZXYSmdnbJHcVRHyZYE6h7hl2pgeAIA3_er4oT_2DpmEW38pwoM__ezWYarjyPYkIEI2enMQTJE0FbCU7fe5wcuSMY140"

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
