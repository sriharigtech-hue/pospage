package com.example.apitest.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.adapter.LowStockAdapter

import com.example.apitest.dataModel.LowStockProductData


class LowStockListFragment : Fragment() {
    companion object {
        val lowStockData: MutableList<LowStockProductData> = mutableListOf()
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LowStockAdapter
    private val jwtToken = "Bearer <your_token_here>"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_low_stock_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.LowStockListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Load saved data from StockListFragment
        adapter = LowStockAdapter(lowStockData)
        recyclerView.adapter = adapter


        val searchView = view.findViewById<SearchView>(R.id.searchView)


        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText.orEmpty())
                return true
            }

        })
        // 2️⃣ Make entire search bar clickable
        searchView.setOnClickListener {
            searchView.isIconified = false  // Expand search
            searchView.requestFocus()       // Focus on input
        }
//        fetchLowStockProducts()
    }

//    private fun fetchLowStockProducts() {
//        val input = Input(status = "1") // fetch all active stock
//        ApiClient.instance.stockProductApi(jwtToken, input)
//            .enqueue(object : Callback<StockProductOutput> {
//                override fun onResponse(
//                    call: Call<StockProductOutput>,
//                    response: Response<StockProductOutput>
//                ) {
//                    if (response.isSuccessful && response.body()?.status == true) {
//                        val allProducts = response.body()?.data ?: emptyList()
//
//                        // filter low stock locally
//                        val lowStockProducts = allProducts.filter {
//                            (it.stockCount ?: 0) <= (it.low_stock_alert ?: 0)
//                        }
//
//                        // Update your LowStockListFragment's data
//                        LowStockListFragment.lowStockData.clear()
//                        LowStockListFragment.lowStockData.addAll(lowStockProducts)
//
//                        // Notify adapter if fragment is visible
//                        val fragment = parentFragmentManager.findFragmentByTag("LowStockListFragment")
//                                as? LowStockListFragment
//                        fragment?.refreshData()
//                    }
//                }
//
//                override fun onFailure(call: Call<StockProductOutput>, t: Throwable) {
//                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
//                }
//            })
//    }





    fun refreshData() {
        adapter.updateData(lowStockData)
    }
}


