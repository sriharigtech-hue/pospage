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
    }

    fun refreshData() {
        adapter.updateData(lowStockData)
    }
}


