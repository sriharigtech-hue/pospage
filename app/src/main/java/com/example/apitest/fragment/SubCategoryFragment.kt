package com.example.apitest.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apitest.R
import com.example.apitest.adapter.SubCategoryAdapter
import com.example.apitest.dataModel.Input
import com.example.apitest.dataModel.SubCategoryDetails
import com.example.apitest.dataModel.SubCategoryOutput
import com.example.apitest.network.ApiClient
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SubCategoryFragment : Fragment() {

    private val subCategoryList = mutableListOf<SubCategoryDetails>()
    private lateinit var adapter: SubCategoryAdapter

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

        fetchSubCategories()
    }

    private fun fetchSubCategories() {
        val token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiYjE1YTc2MWYyMTA3NTJiYTY2MjBiMDg4NTUzYmRiMGYxYWVhZmEwOTJlMWY4MGI1YmFjYmExNzA4MDFlN2Y2NGU1NDEyODQyMzBjYTJiM2YiLCJpYXQiOjE3NTY4MTA5MzUuNjIwMDU5LCJuYmYiOjE3NTY4MTA5MzUuNjIwMDYyLCJleHAiOjE3ODgzNDY5MzUuNjE2NjA2LCJzdWIiOiI2MCIsInNjb3BlcyI6W119.UzX5UwID0Xd9Ia9ZIahOq7ugA8k8viEIOX261q2H2rhR7vMGZHTmm6ymDhdWKmmtSN0fTmU8AijKQzHN4g9HRZvr9seeEHQN3doBFT4odcCbufig4LEH2E0oMjQMmOIYEIjbj-n8o5i2lcqOfchu3vCrDt6McE7GBuPzTA87wyQpMPyO4IAUKU7h7TnwVx3VB_Y8aAUR5DpLr9-LQ7PpOG_hPUvqfUJ3jLoaluDAXA-1hPQ8EXKRz15xAfQxHLR0LLNOCf31hIj7JOxJQFDU-wzXs9g-bH6aAPOY2Q5tye-JZPoLNDCFRxNIkK7HgddkFdH7w0DnW2r4s4vjtfKe0Ubc0aOAxgE74OS_50rw-QEZjl0SceQhoNqeSgSH43_JSjAWX5-VxlwNBgGBBVMXBsKUt52S_eVyyOJpA_qXCqFjVqmWh-MPgo55-dEHw9FFc1ptvzY6FUeYnwBs4Kd65SZyFfU0Esx9wqtq5ZarZDR-gfZUlaP-toKzOzpAs7QasunG62nHDBdUX4P-EdCDFjQdroxeX983vJGe_GzYj5sBRZauXsqg0wtvZmAR2qaxzK4HB853hs7BJn0QAQRZIM7qERV0VqgynnWIrPFhV1WDyU_NMxPQh3WM6jqICK30uqiD7-201ga-522-Dnbxd1vF8CQIFGMRgiwikl3cfoI"
        val input = Input(status = "1",category_id = "230") // send request body if needed

        ApiClient.instance.getAllSubCategoryApi(token, input)
            ?.enqueue(object : Callback<SubCategoryOutput?> {
                override fun onResponse(
                    call: Call<SubCategoryOutput?>,
                    response: Response<SubCategoryOutput?>
                ) {
                    if (response.isSuccessful) {
                        val dataList = response.body()?.subCategoryList
                        Log.d("SubCategoryAPI", "Data: $dataList")
                        if (!dataList.isNullOrEmpty()) {
                            subCategoryList.clear()
                            subCategoryList.addAll(dataList)
                            adapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(requireContext(), "No subcategories found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SubCategoryOutput?>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
