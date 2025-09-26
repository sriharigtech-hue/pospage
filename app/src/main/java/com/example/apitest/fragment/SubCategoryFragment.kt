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
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiOTMyZGFhM2IyNGZmM2Q0ZTY5NmQwYTdlODdhNmY1Y2ViOTA1Y2VhMzc0NWU5NmFlMTBhZWEwZjY2MDkxZDZiMWI1MjIwYjIwYmU4N2EyNDEiLCJpYXQiOjE3NTg3OTI2NDEuMjExMDEsIm5iZiI6MTc1ODc5MjY0MS4yMTEwMTMsImV4cCI6MTc5MDMyODY0MS4yMDQ5NjYsInN1YiI6IjYiLCJzY29wZXMiOltdfQ.lcZz0mc3u-to55t0p23p5g_Z1NQHM23K6xT8vaRAr7X9qHsMbyN9OEq-KuHhZGaqikcY-7ak26uM3_mtoLpLfq6jivhUPFC06JL7ID3HRHOUsWY05CqjIwPpOfjop127eWyz1CYmBmZx3mKsSuE2rvNK91OsROryf1Dz-Px0SnVJ1uDJGK9y1zsJ_Mi8wY7WIH_E3cBusI7uSgNHTQq7dh6GurCYymPxnvvR8cdMQ4Om9SnmfqX9f3GCUncHXBVfxYCuH6ElLsjq74Z9ZXRGBLdwdM4BJWiyv4jzKfU80LmErPo7XT90DPzqa40T0pRblACQsaSGLn64TBoKvxlsO4HjJV8nBg3az5PrCkDsj8QTwgPLzJtP7WcT3pvcCI6O3O8OKlL2lR2-csFHNzCHetHaT1fmOLnVWuc5YIjqhYFEOjp8IKVhzxmcocxsd3R8bcvrjR8NcutOX5H7zmfD-GX17f64RT2c0zqSRdVpRxFZYlNycxd3rI591w9ImgZSkeGQN4Eg8us8oqmlRqfF5mO7QZXi_OsjJgnMdovqP1NB1IxHuTHQIyfESkQ3DoA_KYBF4_8DXhyjvE2D5_SQfZitUpjSwfWqZ_ghgVoOdLJokz4TBJQ9j_ec4jK3uf3nCwS_6Evx9zwbZxmin2CpnIrg4lFRmMpO6YgLfYKPqoI"

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
