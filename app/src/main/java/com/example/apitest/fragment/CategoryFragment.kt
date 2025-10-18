package com.example.apitest.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.AddCategoryActivity
import com.example.apitest.EditCategoryActivity
import com.example.apitest.R
import com.example.apitest.adapter.CategoryAdapter
import com.example.apitest.dataModel.Category
import com.example.apitest.dataModel.CategoryInput
import com.example.apitest.dataModel.CategoryListOutput
import com.example.apitest.dataModel.StatusResponse
import com.example.apitest.network.ApiClient
import com.example.apitest.dataModel.StatusUpdateInput
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoryFragment : Fragment() {
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZGE0YjJmNjlmZGJkNjMwMDMyNGE3MWNkZWRhMDI2ZWI2YTIwMGM4NWIyNTI2MTNjOTZhZGIyMDA2MTE3YjMxMGI0MTFjYjczNzNmZmNlZDAiLCJpYXQiOjE3NjAzMjkzNTkuNDc3MTA1LCJuYmYiOjE3NjAzMjkzNTkuNDc3MTA4LCJleHAiOjE3OTE4NjUzNTkuNDcyNjI1LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.C3ySWdDX7BRHm4qzwWFZZofL_DEx3C2Qjy7iEUWxy9GdrL8OJS7m7Kk_Oe4HtFaT7DvPMEWE_c9kIC8RalMXflXTvPGKkfsw7yxdVxZOKSE20UNZiSbScAdvx3RxAz-XoHK4wJr7wepspLad5y5KCv4RyPXAJl8sIjFELfiCMoxt1CiYGp5_GhsOjbMeSLWSBoDwd3H4MLNvUyU2KN2zhvQaRRUh4T-L11mZgmd_8A8kWZbp_bO6AK-3hGHFGd7VaT2Xqoi4asmn0ABlxusVYWG6hw9UhnU-_uxOVFQLAHog-WKfbahCwfkssXtK07wMpk-ZGHfRn7ujbkrMAX5gNgNkcNQZPRMkUSrokHylEJXKC7UOAgUiK8fy32bIlmFuMQE9hTuuQjHWJ8hdEqtPaXVIcc1oXURtZhCWTp2APH9RE4_L41NYStog_bVMdXwRO_a6QEg_ex0moqxwtRZKivnIF4DKm6WLj45X0FLj-F7HTlZ-eoc9j3w_dVaVyhhxEKUiTyQSJ_AwVKMTbAUmxvWY3OnoIAmu4WYrbC4T4tA2cWoB9yXKna8Yfbil_vC46tLZweGF7RRZR2MPT16q-iCzKG73JqAMphV4NO7b-bMk6mhvgz8TR0_YUewsPg2CVvgdvEmnV4DE4znhnwiLMniN0kPGzF5pindkKTVNDb8"
    private lateinit var searchView: SearchView
    private lateinit var btnAddCategory: FloatingActionButton
    private lateinit var addCategoryLauncher: ActivityResultLauncher<Intent>
    private lateinit var editCategoryLauncher: ActivityResultLauncher<Intent>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private val categories = mutableListOf<Category>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category, container, false)

        recyclerView = view.findViewById(R.id.categoryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = CategoryAdapter(categories)
        recyclerView.adapter = adapter
        searchView = view.findViewById(R.id.searchView)
        searchView.isIconified = false
        searchView.clearFocus()

        btnAddCategory = view.findViewById(R.id.btnAddCategory)


        addCategoryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                fetchCategories() // refresh all categories after add
            }
        }


        // Edit category launcher
        editCategoryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val updatedId = data?.getStringExtra("category_id") ?: return@registerForActivityResult
                val updatedName = data.getStringExtra("category_name") ?: return@registerForActivityResult

                // Update in RecyclerView
                val updatedCategory = categories.find { it.category_id.toString() == updatedId }?.copy(category_name = updatedName)
                if (updatedCategory != null) {
                    adapter.updateCategory(updatedCategory)
                }
            }
        }

        btnAddCategory.setOnClickListener {
            val intent = Intent(requireContext(), AddCategoryActivity::class.java)
            addCategoryLauncher.launch(intent)
        }


        //  Attach search filter
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })


        // Adapter edit click
        adapter.setOnEditClickListener { category ->
            val intent = Intent(requireContext(), EditCategoryActivity::class.java)
            intent.putExtra("category_id", category.category_id.toString())
            intent.putExtra("category_name", category.category_name)
          intent.putExtra("category_image", category.category_image ?: "")
            editCategoryLauncher.launch(intent)
        }

        // Adapter delete click
        adapter.setOnDeleteClickListener { category ->
            showDeleteDialog(category)
        }


        fetchCategories()
        return view
    }
    private fun showDeleteDialog(category: Category) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete '${category.category_name}'?")
            .setPositiveButton("Delete") { _, _ ->
                deleteCategory(category)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun deleteCategory(category: Category) {
        val input = StatusUpdateInput(
            categoryId = category.category_id.toString(), // convert to String if needed
            categoryStatus = "0", // or whatever value
            status = 0
        )

        ApiClient.instance.deleteCategory(jwtToken, input)
            .enqueue(object : Callback<StatusResponse> {
                override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        Toast.makeText(requireContext(), "Deleted successfully", Toast.LENGTH_SHORT).show()
                        adapter.removeCategory(category)
                    } else {
                        Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }



    private fun fetchCategories() {
       val input = CategoryInput(status = "1")

        ApiClient.instance.stockCategoryApi(jwtToken, input)
            .enqueue(object : Callback<CategoryListOutput> {
                override fun onResponse(
                    call: Call<CategoryListOutput>,
                    response: Response<CategoryListOutput>
                ) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        val newList = response.body()?.data ?: emptyList()
                        categories.clear()
                        categories.addAll(newList)
                        adapter.updateData(newList)
                    } else {
                        Toast.makeText(requireContext(), "Error fetching categories", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CategoryListOutput>, t: Throwable) {
                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}

