package com.example.apitest.helperClass

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.adapter.SubCategoryHorizontalAdapter
import com.example.apitest.dataModel.Input
import com.example.apitest.dataModel.SubCategoryDetails
import com.example.apitest.dataModel.SubCategoryOutput
import com.example.apitest.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


// used in stock page fragment


object CategoryHelper {

    fun handleCategorySelection(
        jwtToken: String,
        categoryId: Int,
        categoryName: String,
        subCategoryList: RecyclerView,
        subCategories: MutableList<SubCategoryDetails>,
        subCategoryAdapter: SubCategoryHorizontalAdapter,
        fetchProducts: (selectedSubCategoryId: Int) -> Unit,
    ) {
        val input = Input(category_id = categoryId.toString(), status = "1")
        ApiClient.instance.subCategorySequenceApi(jwtToken, input)
            ?.enqueue(object : Callback<SubCategoryOutput?> {
                override fun onResponse(
                    call: Call<SubCategoryOutput?>,
                    response: Response<SubCategoryOutput?>,
                ) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        subCategories.clear()
                        val apiSubcategories = response.body()?.data ?: listOf()

                        // Remove any subcategory with same name as category
                        val filteredSubcategories = apiSubcategories.filter {
                            !it.subcategoryName.equals(categoryName, ignoreCase = true)
                        }

                        if (filteredSubcategories.isEmpty()) {
                            // No real subcategories: hide bar
                            subCategoryList.visibility = View.GONE
                            fetchProducts(0) // select category itself
                            return
                        }

                        // There are real subcategories: show bar
                        subCategoryList.visibility = View.VISIBLE

                        // Add category itself as first item
                        subCategories.add(
                            SubCategoryDetails(
                                subcategoryId = 0,
                                subcategoryName = categoryName
                            )
                        )

                        // Add filtered subcategories
                        subCategories.addAll(filteredSubcategories)

                        subCategoryAdapter.notifyDataSetChanged()
                        subCategoryAdapter.resetSelection()

                        // Auto-select first subcategory (category itself)
                        subCategoryAdapter.setSelectedPosition(0)
                        fetchProducts(0)

                    } else {
                        // API failed: hide bar
                        subCategoryList.visibility = View.GONE
                        fetchProducts(0)
                    }
                }

                override fun onFailure(call: Call<SubCategoryOutput?>, t: Throwable) {
                    Log.e("CategoryHelper", t.message ?: "")
                    subCategoryList.visibility = View.GONE
                    fetchProducts(0)
                }
            })
    }
}


