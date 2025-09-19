// AddProductActivity.kt
package com.example.apitest

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apitest.dataModel.*
import com.example.apitest.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddProductActivity : AppCompatActivity() {

    private lateinit var categorySpinner: Spinner
    private lateinit var subCategorySpinner: Spinner
    private lateinit var subCategoryTitle: TextView
    private lateinit var subCategoryLayout: LinearLayout

    private var categoryList: List<Category> = emptyList()
    private var subCategoryList: List<SubCategoryDetails> = emptyList()

    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiNjk3MmE4MjVjMjZhNmU2ZDAxMTk3NjdiNDMzYzc4NWEzNTdmYzYxYTZhMTBjNjQ2MGViYTc4ZWQxNDM4NmQyYjM2YTFkNWJjZTIwZTc0MmIiLCJpYXQiOjE3NTgyNTU4MTEuMjA1Njg1LCJuYmYiOjE3NTgyNTU4MTEuMjA1Njg3LCJleHAiOjE3ODk3OTE4MTEuMjAyMzI1LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.bWv1-ccFjlnUbHqgVKleTigmPBsohLqMMJ6KNx-zPgEsHzPvFn19WQV6kc85aXrKZOdbiazq4eb7y9I1wdXI8n-BQQ4jpqruQqW87KNzvuoOvd0qPKW-EbUFbst1I5QAgA4m0kySj8JxVHAFSgBtzocj42JUd0XuNHMtGjwLUH4QM4Njc-tkVSmVqIHN66LGoaslfkl5tQTxjFV0Xg1Ay1fyNdp5A1pSZPv4wTr9aAfR1nhrqA5FtU8x0BJyWcpk3ojQq3gWUB3CZgr0Qq7tMpzuZwufR-HWqCX-Be4YRZ1wyANAQHEt0JEp5HLV9htpfuCE7grP_sw-cywep-FxVlyKO0tyc7ykFnhOaI6YlREAms4m_NOpdleSnYv9or7uj8DQWI2F4318SoFSPFu1UsVI9n2Ygf54TZD4FDhMGjWSue2uBCS3HPleSyO6Qf2Lk64OovnYRXc_tJzddcvu8LEC8ihvZpDpr4KMmxGIM15c-4lh0gXpcOOPZXmHG4rG73ogFxVzJdp-nAs-h2U-Kh52kXmjFm2MAgfKSv-0IdgA2IXUxcWthRLO5WYtiggR-ghkTx9hy6Seyq5ZXYSmdnbJHcVRHyZYE6h7hl2pgeAIA3_er4oT_2DpmEW38pwoM__ezWYarjyPYkIEI2enMQTJE0FbCU7fe5wcuSMY140"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        // Bind views
        categorySpinner = findViewById(R.id.categorySpinner)
        subCategorySpinner = findViewById(R.id.subCategorySpinner)
        subCategoryTitle = findViewById(R.id.subCategorySpinnerTitleLayout)
        subCategoryLayout = findViewById(R.id.subCategorySpinnerLayout)

        // Initially hide subcategory section
        subCategoryTitle.visibility = View.GONE
        subCategoryLayout.visibility = View.GONE

        loadCategories()
    }
    override fun onBackPressed() {
        super.onBackPressed() // this will close AddProductActivity and return
        finish()
    }


    private fun loadCategories() {
        val input = CategoryInput(status = "1")

        ApiClient.instance.stockCategoryApi(jwtToken, input)
            .enqueue(object : Callback<CategoryListOutput> {
                override fun onResponse(
                    call: Call<CategoryListOutput>,
                    response: Response<CategoryListOutput>
                ) {
                    if (response.isSuccessful && response.body()?.data != null) {
                        categoryList = response.body()!!.data!!

                        val categoryNames = categoryList.map { it.category_name }
                        val adapter = ArrayAdapter(
                            this@AddProductActivity,
                            android.R.layout.simple_spinner_item,
                            categoryNames
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        categorySpinner.adapter = adapter

                        categorySpinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    val selectedCategory = categoryList[position]
                                    loadSubCategories(selectedCategory.category_id)
                                }

                                override fun onNothingSelected(parent: AdapterView<*>) {}
                            }
                    }
                }

                override fun onFailure(call: Call<CategoryListOutput>, t: Throwable) {
                    Toast.makeText(this@AddProductActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadSubCategories(categoryId: Int) {
        val input = Input(category_id = categoryId.toString(), status = "1")

        ApiClient.instance.addEditSubCategoryApi(jwtToken, input)
            ?.enqueue(object : Callback<SubCategoryOutput?> {
                override fun onResponse(
                    call: Call<SubCategoryOutput?>,
                    response: Response<SubCategoryOutput?>
                ) {
                    val subCategories = response.body()?.data
                    if (!subCategories.isNullOrEmpty()) {
                        subCategoryList = subCategories

                        val names = subCategoryList.map { it.subcategoryName ?: "Unnamed" }
                        val adapter = ArrayAdapter(
                            this@AddProductActivity,
                            android.R.layout.simple_spinner_item,
                            names
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        subCategorySpinner.adapter = adapter

                        // Show subcategory UI
                        subCategoryTitle.visibility = View.VISIBLE
                        subCategoryLayout.visibility = View.VISIBLE
                    } else {
                        // Hide subcategory UI
                        subCategoryTitle.visibility = View.GONE
                        subCategoryLayout.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<SubCategoryOutput?>, t: Throwable) {
                    Toast.makeText(this@AddProductActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}

