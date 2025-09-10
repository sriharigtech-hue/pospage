package com.example.apitest

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apitest.dataModel.AddCategoryOutput
import com.example.apitest.dataModel.CategoryInput

import com.example.apitest.databinding.ActivityAddCategoryBinding
import com.example.apitest.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        binding.saveButton.setOnClickListener {
            val categoryName = binding.categoryName.text.toString().trim()
            if (categoryName.isEmpty()) {
                Toast.makeText(this, "Please enter category name", Toast.LENGTH_SHORT).show()
            } else {
                saveCategoryApi(categoryName)
            }
        }
    }

    private fun saveCategoryApi(categoryName: String) {
        val token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiYjE1YTc2MWYyMTA3NTJiYTY2MjBiMDg4NTUzYmRiMGYxYWVhZmEwOTJlMWY4MGI1YmFjYmExNzA4MDFlN2Y2NGU1NDEyODQyMzBjYTJiM2YiLCJpYXQiOjE3NTY4MTA5MzUuNjIwMDU5LCJuYmYiOjE3NTY4MTA5MzUuNjIwMDYyLCJleHAiOjE3ODgzNDY5MzUuNjE2NjA2LCJzdWIiOiI2MCIsInNjb3BlcyI6W119.UzX5UwID0Xd9Ia9ZIahOq7ugA8k8viEIOX261q2H2rhR7vMGZHTmm6ymDhdWKmmtSN0fTmU8AijKQzHN4g9HRZvr9seeEHQN3doBFT4odcCbufig4LEH2E0oMjQMmOIYEIjbj-n8o5i2lcqOfchu3vCrDt6McE7GBuPzTA87wyQpMPyO4IAUKU7h7TnwVx3VB_Y8aAUR5DpLr9-LQ7PpOG_hPUvqfUJ3jLoaluDAXA-1hPQ8EXKRz15xAfQxHLR0LLNOCf31hIj7JOxJQFDU-wzXs9g-bH6aAPOY2Q5tye-JZPoLNDCFRxNIkK7HgddkFdH7w0DnW2r4s4vjtfKe0Ubc0aOAxgE74OS_50rw-QEZjl0SceQhoNqeSgSH43_JSjAWX5-VxlwNBgGBBVMXBsKUt52S_eVyyOJpA_qXCqFjVqmWh-MPgo55-dEHw9FFc1ptvzY6FUeYnwBs4Kd65SZyFfU0Esx9wqtq5ZarZDR-gfZUlaP-toKzOzpAs7QasunG62nHDBdUX4P-EdCDFjQdroxeX983vJGe_GzYj5sBRZauXsqg0wtvZmAR2qaxzK4HB853hs7BJn0QAQRZIM7qERV0VqgynnWIrPFhV1WDyU_NMxPQh3WM6jqICK30uqiD7-201ga-522-Dnbxd1vF8CQIFGMRgiwikl3cfoI"
        val input = CategoryInput(category_name = categoryName, status = "1")

        ApiClient.instance.addCategoryApi(token, input)
            .enqueue(object : Callback<AddCategoryOutput> {
                override fun onResponse(
                    call: Call<AddCategoryOutput>,
                    response: Response<AddCategoryOutput>
                ) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        Toast.makeText(this@AddCategoryActivity, "Category saved", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK) // just notify fragment
                        finish()
                    } else {
                        Toast.makeText(this@AddCategoryActivity, "Failed to save category", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AddCategoryOutput>, t: Throwable) {
                    Toast.makeText(this@AddCategoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}

