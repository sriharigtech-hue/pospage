package com.example.apitest.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.AddSubCategoryActivity
import com.example.apitest.EditSubCategoryActivity
import com.example.apitest.R
import com.example.apitest.adapter.SubCategoryAdapter
import com.example.apitest.dataModel.Input
import com.example.apitest.dataModel.StatusResponse
import com.example.apitest.dataModel.StatusUpdateInput
import com.example.apitest.dataModel.SubCategoryDetails
import com.example.apitest.dataModel.SubCategoryOutput
import com.example.apitest.dataModel.SubCategoryStatusUpdateInput
import com.example.apitest.network.ApiClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SubCategoryFragment : Fragment() {

    private val subCategoryList = mutableListOf<SubCategoryDetails>()
    private lateinit var adapter: SubCategoryAdapter
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZGE0YjJmNjlmZGJkNjMwMDMyNGE3MWNkZWRhMDI2ZWI2YTIwMGM4NWIyNTI2MTNjOTZhZGIyMDA2MTE3YjMxMGI0MTFjYjczNzNmZmNlZDAiLCJpYXQiOjE3NjAzMjkzNTkuNDc3MTA1LCJuYmYiOjE3NjAzMjkzNTkuNDc3MTA4LCJleHAiOjE3OTE4NjUzNTkuNDcyNjI1LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.C3ySWdDX7BRHm4qzwWFZZofL_DEx3C2Qjy7iEUWxy9GdrL8OJS7m7Kk_Oe4HtFaT7DvPMEWE_c9kIC8RalMXflXTvPGKkfsw7yxdVxZOKSE20UNZiSbScAdvx3RxAz-XoHK4wJr7wepspLad5y5KCv4RyPXAJl8sIjFELfiCMoxt1CiYGp5_GhsOjbMeSLWSBoDwd3H4MLNvUyU2KN2zhvQaRRUh4T-L11mZgmd_8A8kWZbp_bO6AK-3hGHFGd7VaT2Xqoi4asmn0ABlxusVYWG6hw9UhnU-_uxOVFQLAHog-WKfbahCwfkssXtK07wMpk-ZGHfRn7ujbkrMAX5gNgNkcNQZPRMkUSrokHylEJXKC7UOAgUiK8fy32bIlmFuMQE9hTuuQjHWJ8hdEqtPaXVIcc1oXURtZhCWTp2APH9RE4_L41NYStog_bVMdXwRO_a6QEg_ex0moqxwtRZKivnIF4DKm6WLj45X0FLj-F7HTlZ-eoc9j3w_dVaVyhhxEKUiTyQSJ_AwVKMTbAUmxvWY3OnoIAmu4WYrbC4T4tA2cWoB9yXKna8Yfbil_vC46tLZweGF7RRZR2MPT16q-iCzKG73JqAMphV4NO7b-bMk6mhvgz8TR0_YUewsPg2CVvgdvEmnV4DE4znhnwiLMniN0kPGzF5pindkKTVNDb8"

    // Launcher for AddSubCategoryActivity
    private val addSubCategoryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                fetchSubCategories()
            }
        }

    // Launcher for EditSubCategoryActivity
    private val editSubCategoryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val updatedSubCategory =
                    result.data?.getParcelableExtra<SubCategoryDetails>("updated_subcategory")
                updatedSubCategory?.let { updated ->
                    val index =
                        subCategoryList.indexOfFirst { it.subcategoryId == updated.subcategoryId }
                    if (index != -1) {
                        subCategoryList[index] = updated
                        adapter.notifyItemChanged(index)
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_sub_category, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerSubCategory)
        adapter = SubCategoryAdapter(subCategoryList)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        val searchView = view.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchSubCategory)

        //  Add this to make full search bar clickable
        searchView.setIconifiedByDefault(false)
        searchView.isIconified = false
        searchView.clearFocus()

        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.filter(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })

        // Handle edit click from adapter
        adapter.setOnEditClickListener { subCategory ->
            val intent = Intent(requireContext(), EditSubCategoryActivity::class.java)
            intent.putExtra("subcategory_id", subCategory.subcategoryId)
            intent.putExtra("category_id", subCategory.categoryId)
            intent.putExtra("subcategory_name", subCategory.subcategoryName)
            editSubCategoryLauncher.launch(intent)
        }
        //Handle delete click from adapter
        adapter.setOnDeleteClickListener { subCategory ->
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Subcategory")
                .setMessage("Are you sure you want to delete \"${subCategory.subcategoryName}\"?")
                .setPositiveButton("Delete") { dialog, _ ->
                    deleteSubCategory(subCategory)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }
        // Handle status toggle from adapter
        adapter.setOnStatusToggleListener { subCategory, isChecked ->
            updateSubCategoryStatus(subCategory, isChecked)
        }




        val addButton = view.findViewById<FloatingActionButton>(R.id.btnAddSubCategory)
        addButton.setOnClickListener {
            val intent = Intent(requireContext(), AddSubCategoryActivity::class.java)
            addSubCategoryLauncher.launch(intent)
        }

        fetchSubCategories()
    }

    private fun fetchSubCategories() {

        val input = Input(status = "1")

        ApiClient.instance.getAllSubCategoryApi(jwtToken, input)
            ?.enqueue(object : Callback<SubCategoryOutput?> {
                override fun onResponse(
                    call: Call<SubCategoryOutput?>,
                    response: Response<SubCategoryOutput?>
                ) {
                    if (response.isSuccessful) {
                        val dataList = response.body()?.data
                        if (!dataList.isNullOrEmpty()) {
                            adapter.setData(dataList)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "No subcategories found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<SubCategoryOutput?>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }
    private fun deleteSubCategory(subCategory: SubCategoryDetails) {
        val input = StatusUpdateInput(
            categoryId = subCategory.categoryId?.toString() ?: "0",
            sub_category_id = subCategory.subcategoryId.toString(),
            status = 0,
            categoryStatus = "0"
        )

        ApiClient.instance.deleteSubCategory(jwtToken, input)
            ?.enqueue(object : Callback<StatusResponse> {
                override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        Toast.makeText(requireContext(), "Deleted successfully", Toast.LENGTH_SHORT).show()
                        adapter.removeItem(subCategory.subcategoryId) // ✅ this updates UI properly
                    } else {
                        Toast.makeText(requireContext(), response.body()?.message ?: "Delete failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateSubCategoryStatus(subCategory: SubCategoryDetails, isChecked: Boolean) {
        val newStatus = if (isChecked) 1 else 0

        val input = SubCategoryStatusUpdateInput(
            subcategory_id = subCategory.subcategoryId.toString(),
            subcategory_status = newStatus.toString(),
            status = "1"
        )
        Log.d("UpdateAPI", "Input: $input")

        ApiClient.instance.subCategoryStatusUpdate(jwtToken, input)
            .enqueue(object : Callback<StatusResponse> {

                override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                    Log.d("UpdateAPI", "Response: ${response.body()}")
                    if (response.isSuccessful && response.body()?.status == true) {
                        // Update local list
                        subCategory.subcategoryStatus = newStatus.toString()
                        val index = subCategoryList.indexOfFirst { it.subcategoryId == subCategory.subcategoryId }
                        if (index != -1) adapter.notifyItemChanged(index)
                        Toast.makeText(requireContext(), "Status updated!", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("UpdateAPI", "Failed response: ${response.errorBody()?.string()}")
                        // Revert switch if API fails
                        val index = subCategoryList.indexOfFirst { it.subcategoryId == subCategory.subcategoryId }
                        if (index != -1) adapter.notifyItemChanged(index)
                        Toast.makeText(requireContext(), response.body()?.message ?: "Update failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                    // Revert switch if network error
                    val index = subCategoryList.indexOfFirst { it.subcategoryId == subCategory.subcategoryId }
                    if (index != -1) adapter.notifyItemChanged(index)
                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }






}
