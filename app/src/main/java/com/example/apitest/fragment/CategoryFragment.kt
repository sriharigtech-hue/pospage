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
        // 🔍 Attach search filter
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
//            intent.putExtra("category_image", category.image ?: "")
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
        val token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiYjE1YTc2MWYyMTA3NTJiYTY2MjBiMDg4NTUzYmRiMGYxYWVhZmEwOTJlMWY4MGI1YmFjYmExNzA4MDFlN2Y2NGU1NDEyODQyMzBjYTJiM2YiLCJpYXQiOjE3NTY4MTA5MzUuNjIwMDU5LCJuYmYiOjE3NTY4MTA5MzUuNjIwMDYyLCJleHAiOjE3ODgzNDY5MzUuNjE2NjA2LCJzdWIiOiI2MCIsInNjb3BlcyI6W119.UzX5UwID0Xd9Ia9ZIahOq7ugA8k8viEIOX261q2H2rhR7vMGZHTmm6ymDhdWKmmtSN0fTmU8AijKQzHN4g9HRZvr9seeEHQN3doBFT4odcCbufig4LEH2E0oMjQMmOIYEIjbj-n8o5i2lcqOfchu3vCrDt6McE7GBuPzTA87wyQpMPyO4IAUKU7h7TnwVx3VB_Y8aAUR5DpLr9-LQ7PpOG_hPUvqfUJ3jLoaluDAXA-1hPQ8EXKRz15xAfQxHLR0LLNOCf31hIj7JOxJQFDU-wzXs9g-bH6aAPOY2Q5tye-JZPoLNDCFRxNIkK7HgddkFdH7w0DnW2r4s4vjtfKe0Ubc0aOAxgE74OS_50rw-QEZjl0SceQhoNqeSgSH43_JSjAWX5-VxlwNBgGBBVMXBsKUt52S_eVyyOJpA_qXCqFjVqmWh-MPgo55-dEHw9FFc1ptvzY6FUeYnwBs4Kd65SZyFfU0Esx9wqtq5ZarZDR-gfZUlaP-toKzOzpAs7QasunG62nHDBdUX4P-EdCDFjQdroxeX983vJGe_GzYj5sBRZauXsqg0wtvZmAR2qaxzK4HB853hs7BJn0QAQRZIM7qERV0VqgynnWIrPFhV1WDyU_NMxPQh3WM6jqICK30uqiD7-201ga-522-Dnbxd1vF8CQIFGMRgiwikl3cfoI"
        val input = StatusUpdateInput(category_id = category.category_id)

        ApiClient.instance.deleteCategory(token, input)
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
        val token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiYjE1YTc2MWYyMTA3NTJiYTY2MjBiMDg4NTUzYmRiMGYxYWVhZmEwOTJlMWY4MGI1YmFjYmExNzA4MDFlN2Y2NGU1NDEyODQyMzBjYTJiM2YiLCJpYXQiOjE3NTY4MTA5MzUuNjIwMDU5LCJuYmYiOjE3NTY4MTA5MzUuNjIwMDYyLCJleHAiOjE3ODgzNDY5MzUuNjE2NjA2LCJzdWIiOiI2MCIsInNjb3BlcyI6W119.UzX5UwID0Xd9Ia9ZIahOq7ugA8k8viEIOX261q2H2rhR7vMGZHTmm6ymDhdWKmmtSN0fTmU8AijKQzHN4g9HRZvr9seeEHQN3doBFT4odcCbufig4LEH2E0oMjQMmOIYEIjbj-n8o5i2lcqOfchu3vCrDt6McE7GBuPzTA87wyQpMPyO4IAUKU7h7TnwVx3VB_Y8aAUR5DpLr9-LQ7PpOG_hPUvqfUJ3jLoaluDAXA-1hPQ8EXKRz15xAfQxHLR0LLNOCf31hIj7JOxJQFDU-wzXs9g-bH6aAPOY2Q5tye-JZPoLNDCFRxNIkK7HgddkFdH7w0DnW2r4s4vjtfKe0Ubc0aOAxgE74OS_50rw-QEZjl0SceQhoNqeSgSH43_JSjAWX5-VxlwNBgGBBVMXBsKUt52S_eVyyOJpA_qXCqFjVqmWh-MPgo55-dEHw9FFc1ptvzY6FUeYnwBs4Kd65SZyFfU0Esx9wqtq5ZarZDR-gfZUlaP-toKzOzpAs7QasunG62nHDBdUX4P-EdCDFjQdroxeX983vJGe_GzYj5sBRZauXsqg0wtvZmAR2qaxzK4HB853hs7BJn0QAQRZIM7qERV0VqgynnWIrPFhV1WDyU_NMxPQh3WM6jqICK30uqiD7-201ga-522-Dnbxd1vF8CQIFGMRgiwikl3cfoI"
        val input = CategoryInput(status = "1")

        ApiClient.instance.stockCategoryApi(token, input)
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

