package com.example.apitest

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.adapter.POSAdapter
import com.example.apitest.adapter.PosCategoryAdapter
import com.example.apitest.adapter.PosSubCategoryAdapter
import com.example.apitest.dataModel.CategoryList
import com.example.apitest.dataModel.CategoryOutput
import com.example.apitest.dataModel.Input
import com.example.apitest.dataModel.NewProductList
import com.example.apitest.dataModel.NewProductOutput
import com.example.apitest.dataModel.ProductInput
import com.example.apitest.dataModel.SubCategoryDetails
import com.example.apitest.dataModel.SubCategoryOutput

import com.example.apitest.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class POSActivity : NavigationActivity() {
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiOTMyZGFhM2IyNGZmM2Q0ZTY5NmQwYTdlODdhNmY1Y2ViOTA1Y2VhMzc0NWU5NmFlMTBhZWEwZjY2MDkxZDZiMWI1MjIwYjIwYmU4N2EyNDEiLCJpYXQiOjE3NTg3OTI2NDEuMjExMDEsIm5iZiI6MTc1ODc5MjY0MS4yMTEwMTMsImV4cCI6MTc5MDMyODY0MS4yMDQ5NjYsInN1YiI6IjYiLCJzY29wZXMiOltdfQ.lcZz0mc3u-to55t0p23p5g_Z1NQHM23K6xT8vaRAr7X9qHsMbyN9OEq-KuHhZGaqikcY-7ak26uM3_mtoLpLfq6jivhUPFC06JL7ID3HRHOUsWY05CqjIwPpOfjop127eWyz1CYmBmZx3mKsSuE2rvNK91OsROryf1Dz-Px0SnVJ1uDJGK9y1zsJ_Mi8wY7WIH_E3cBusI7uSgNHTQq7dh6GurCYymPxnvvR8cdMQ4Om9SnmfqX9f3GCUncHXBVfxYCuH6ElLsjq74Z9ZXRGBLdwdM4BJWiyv4jzKfU80LmErPo7XT90DPzqa40T0pRblACQsaSGLn64TBoKvxlsO4HjJV8nBg3az5PrCkDsj8QTwgPLzJtP7WcT3pvcCI6O3O8OKlL2lR2-csFHNzCHetHaT1fmOLnVWuc5YIjqhYFEOjp8IKVhzxmcocxsd3R8bcvrjR8NcutOX5H7zmfD-GX17f64RT2c0zqSRdVpRxFZYlNycxd3rI591w9ImgZSkeGQN4Eg8us8oqmlRqfF5mO7QZXi_OsjJgnMdovqP1NB1IxHuTHQIyfESkQ3DoA_KYBF4_8DXhyjvE2D5_SQfZitUpjSwfWqZ_ghgVoOdLJokz4TBJQ9j_ec4jK3uf3nCwS_6Evx9zwbZxmin2CpnIrg4lFRmMpO6YgLfYKPqoI"

    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var subCategoryRecyclerView: RecyclerView

    private lateinit var centerRecyclerView: RecyclerView



    private lateinit var categoryAdapter: PosCategoryAdapter
    private lateinit var subCategoryAdapter: PosSubCategoryAdapter
    private lateinit var productAdapter: POSAdapter


    private var categoryList = mutableListOf<CategoryList>()
    private val subCategoryList = mutableListOf<SubCategoryDetails>()
    private val productList = mutableListOf<NewProductList>()

    private var selectedCategoryId: String? = null

    private var selectedSubCategoryId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posactivity)
        setupBottomNavigation("pos")

        categoryRecyclerView = findViewById(R.id.serviceList)
        categoryRecyclerView.layoutManager = LinearLayoutManager(this)


        subCategoryRecyclerView = findViewById(R.id.subCategoryList)
        subCategoryRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Category Adapter
        categoryAdapter = PosCategoryAdapter(categoryList) { category, _ ->
            val newCategoryId = category.categoryId?.toString()

            if (selectedCategoryId != newCategoryId) {
                selectedCategoryId = newCategoryId

                // Reset subcategory selection
                selectedSubCategoryId = null
                subCategoryList.clear()
                subCategoryAdapter.resetSelection()
                subCategoryAdapter.notifyDataSetChanged()
                subCategoryRecyclerView.visibility = View.GONE
            }

            // Fetch subcategories for the selected category
            getSubCategories(newCategoryId)

            // If no subcategory selected, fetch products for the category
            getPOSProducts(newCategoryId.toString(), null)
        }


        categoryRecyclerView.adapter = categoryAdapter


        subCategoryAdapter = PosSubCategoryAdapter(subCategoryList) { subCategory, _ ->
            val categoryId = selectedCategoryId ?: return@PosSubCategoryAdapter
            val subCategoryId = subCategory.subcategoryId?.toString() ?: return@PosSubCategoryAdapter
            selectedSubCategoryId = subCategoryId

            // Fetch products for both category + subcategory
            getPOSProducts(categoryId, subCategoryId)
        }



        subCategoryRecyclerView.adapter = subCategoryAdapter

        centerRecyclerView = findViewById(R.id.centerRecyclerView)
        centerRecyclerView.layoutManager = LinearLayoutManager(this)

        productAdapter = POSAdapter(productList) { product, variation, quantity ->
            Toast.makeText(
                this,
                "${product.productName} (${variation.productVariation}) added! Quantity: $quantity",
                Toast.LENGTH_SHORT
            ).show()

        }


        centerRecyclerView.adapter = productAdapter

        // Call API
        getCategory()
    }

    private fun getCategory() {
        val input = Input(status = "1") // pass status = 1


        ApiClient.instance.categoryApi(jwtToken, input)
            .enqueue(object : Callback<CategoryOutput> {
                override fun onResponse(call: Call<CategoryOutput>, response: Response<CategoryOutput>) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        categoryList.clear()
                        response.body()?.categoryList?.let { categoryList.addAll(it) }
                        categoryAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@POSActivity, "No categories found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CategoryOutput>, t: Throwable) {
                    Toast.makeText(this@POSActivity, "API Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun getSubCategories(categoryId: String?) {
        if (categoryId.isNullOrEmpty()) return

        val input = Input(
            status = "1",
            category_id = categoryId
        )

        ApiClient.instance.subCategoryApi(jwtToken, input)
            ?.enqueue(object : Callback<SubCategoryOutput> {
                override fun onResponse(
                    call: Call<SubCategoryOutput>,
                    response: Response<SubCategoryOutput>
                ) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        val subList = response.body()?.data ?: emptyList()
                        subCategoryList.clear()
                        subCategoryList.addAll(subList)
                        subCategoryRecyclerView.visibility = if (subList.isEmpty()) View.GONE else View.VISIBLE
                        subCategoryAdapter.notifyDataSetChanged()
                    } else {
                        subCategoryList.clear()
                        subCategoryRecyclerView.visibility = View.GONE
                        subCategoryAdapter.notifyDataSetChanged()
                        Toast.makeText(this@POSActivity, "No subcategories found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SubCategoryOutput>, t: Throwable) {
                    Toast.makeText(this@POSActivity, "Subcategory API Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            } as Callback<SubCategoryOutput?>)
    }
    private fun getPOSProducts(categoryId: String, subCategoryId: String?) {
        val input = ProductInput(
            categoryId = categoryId,
            subCategoryId = subCategoryId, // nullable
            status = "1",
            page = "1"
        )

        ApiClient.instance.posProductApi(jwtToken, input)?.enqueue(object : Callback<NewProductOutput> {
            override fun onResponse(call: Call<NewProductOutput>, response: Response<NewProductOutput>) {
                productList.clear()
                if (response.isSuccessful && response.body()?.status == true) {
                    response.body()?.data?.let { productList.addAll(it) }
                }
                productAdapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<NewProductOutput>, t: Throwable) {
                Toast.makeText(this@POSActivity, "Products API Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }




}
