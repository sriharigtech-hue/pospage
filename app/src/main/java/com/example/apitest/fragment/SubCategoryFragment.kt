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
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiNjk3MmE4MjVjMjZhNmU2ZDAxMTk3NjdiNDMzYzc4NWEzNTdmYzYxYTZhMTBjNjQ2MGViYTc4ZWQxNDM4NmQyYjM2YTFkNWJjZTIwZTc0MmIiLCJpYXQiOjE3NTgyNTU4MTEuMjA1Njg1LCJuYmYiOjE3NTgyNTU4MTEuMjA1Njg3LCJleHAiOjE3ODk3OTE4MTEuMjAyMzI1LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.bWv1-ccFjlnUbHqgVKleTigmPBsohLqMMJ6KNx-zPgEsHzPvFn19WQV6kc85aXrKZOdbiazq4eb7y9I1wdXI8n-BQQ4jpqruQqW87KNzvuoOvd0qPKW-EbUFbst1I5QAgA4m0kySj8JxVHAFSgBtzocj42JUd0XuNHMtGjwLUH4QM4Njc-tkVSmVqIHN66LGoaslfkl5tQTxjFV0Xg1Ay1fyNdp5A1pSZPv4wTr9aAfR1nhrqA5FtU8x0BJyWcpk3ojQq3gWUB3CZgr0Qq7tMpzuZwufR-HWqCX-Be4YRZ1wyANAQHEt0JEp5HLV9htpfuCE7grP_sw-cywep-FxVlyKO0tyc7ykFnhOaI6YlREAms4m_NOpdleSnYv9or7uj8DQWI2F4318SoFSPFu1UsVI9n2Ygf54TZD4FDhMGjWSue2uBCS3HPleSyO6Qf2Lk64OovnYRXc_tJzddcvu8LEC8ihvZpDpr4KMmxGIM15c-4lh0gXpcOOPZXmHG4rG73ogFxVzJdp-nAs-h2U-Kh52kXmjFm2MAgfKSv-0IdgA2IXUxcWthRLO5WYtiggR-ghkTx9hy6Seyq5ZXYSmdnbJHcVRHyZYE6h7hl2pgeAIA3_er4oT_2DpmEW38pwoM__ezWYarjyPYkIEI2enMQTJE0FbCU7fe5wcuSMY140"

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
            category_id = subCategory.categoryId ?: 0,
            sub_category_id = subCategory.subcategoryId.toString(),
            status = 0,
            category_status = 0 // important
        )

        ApiClient.instance.deleteSubCategory(jwtToken, input)?.enqueue(object : Callback<StatusResponse> {
            override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(requireContext(), "Deleted successfully", Toast.LENGTH_SHORT).show()
                    val index = subCategoryList.indexOfFirst { it.subcategoryId == subCategory.subcategoryId }
                    if (index != -1) {
                        subCategoryList.removeAt(index)
                        adapter.notifyItemRemoved(index)
                    }
                } else {
                    Log.d("DeleteAPI", "Failed response: ${response.errorBody()?.string()}")
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
                        subCategory.subcategoryStatus = newStatus
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
