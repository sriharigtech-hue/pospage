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
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiOTMyZGFhM2IyNGZmM2Q0ZTY5NmQwYTdlODdhNmY1Y2ViOTA1Y2VhMzc0NWU5NmFlMTBhZWEwZjY2MDkxZDZiMWI1MjIwYjIwYmU4N2EyNDEiLCJpYXQiOjE3NTg3OTI2NDEuMjExMDEsIm5iZiI6MTc1ODc5MjY0MS4yMTEwMTMsImV4cCI6MTc5MDMyODY0MS4yMDQ5NjYsInN1YiI6IjYiLCJzY29wZXMiOltdfQ.lcZz0mc3u-to55t0p23p5g_Z1NQHM23K6xT8vaRAr7X9qHsMbyN9OEq-KuHhZGaqikcY-7ak26uM3_mtoLpLfq6jivhUPFC06JL7ID3HRHOUsWY05CqjIwPpOfjop127eWyz1CYmBmZx3mKsSuE2rvNK91OsROryf1Dz-Px0SnVJ1uDJGK9y1zsJ_Mi8wY7WIH_E3cBusI7uSgNHTQq7dh6GurCYymPxnvvR8cdMQ4Om9SnmfqX9f3GCUncHXBVfxYCuH6ElLsjq74Z9ZXRGBLdwdM4BJWiyv4jzKfU80LmErPo7XT90DPzqa40T0pRblACQsaSGLn64TBoKvxlsO4HjJV8nBg3az5PrCkDsj8QTwgPLzJtP7WcT3pvcCI6O3O8OKlL2lR2-csFHNzCHetHaT1fmOLnVWuc5YIjqhYFEOjp8IKVhzxmcocxsd3R8bcvrjR8NcutOX5H7zmfD-GX17f64RT2c0zqSRdVpRxFZYlNycxd3rI591w9ImgZSkeGQN4Eg8us8oqmlRqfF5mO7QZXi_OsjJgnMdovqP1NB1IxHuTHQIyfESkQ3DoA_KYBF4_8DXhyjvE2D5_SQfZitUpjSwfWqZ_ghgVoOdLJokz4TBJQ9j_ec4jK3uf3nCwS_6Evx9zwbZxmin2CpnIrg4lFRmMpO6YgLfYKPqoI"

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
