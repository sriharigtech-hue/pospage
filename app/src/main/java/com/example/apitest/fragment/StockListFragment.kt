    package com.example.apitest.fragment

    import android.os.Bundle
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import androidx.appcompat.widget.SearchView
    import android.widget.Toast
    import androidx.fragment.app.Fragment
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.example.apitest.R
    import com.example.apitest.adapter.CategorySidebarAdapter
    import com.example.apitest.adapter.StockProductAdapter
    import com.example.apitest.adapter.SubCategoryHorizontalAdapter
    import com.example.apitest.dataModel.*
    import com.example.apitest.fragment.LowStockListFragment.Companion.lowStockData
    import com.example.apitest.helperClass.CategoryHelper
    import com.example.apitest.network.ApiClient
    import retrofit2.Call
    import retrofit2.Callback
    import retrofit2.Response



    // used helper class for category selection and subcategory selection , category helper file name


    class StockListFragment : Fragment() {

        private lateinit var serviceList: RecyclerView       // Left sidebar
        private lateinit var subCategoryList: RecyclerView   // Top horizontal
        private lateinit var centerRecyclerView: RecyclerView

        private lateinit var categoryAdapter: CategorySidebarAdapter
        private lateinit var subCategoryAdapter: SubCategoryHorizontalAdapter
        private lateinit var stockAdapter: StockProductAdapter

        private var categories: MutableList<Category> = mutableListOf()
        private var subCategories: MutableList<SubCategoryDetails> = mutableListOf()
        private var stockProducts: MutableList<StockProductData> = mutableListOf()

        private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiOTMyZGFhM2IyNGZmM2Q0ZTY5NmQwYTdlODdhNmY1Y2ViOTA1Y2VhMzc0NWU5NmFlMTBhZWEwZjY2MDkxZDZiMWI1MjIwYjIwYmU4N2EyNDEiLCJpYXQiOjE3NTg3OTI2NDEuMjExMDEsIm5iZiI6MTc1ODc5MjY0MS4yMTEwMTMsImV4cCI6MTc5MDMyODY0MS4yMDQ5NjYsInN1YiI6IjYiLCJzY29wZXMiOltdfQ.lcZz0mc3u-to55t0p23p5g_Z1NQHM23K6xT8vaRAr7X9qHsMbyN9OEq-KuHhZGaqikcY-7ak26uM3_mtoLpLfq6jivhUPFC06JL7ID3HRHOUsWY05CqjIwPpOfjop127eWyz1CYmBmZx3mKsSuE2rvNK91OsROryf1Dz-Px0SnVJ1uDJGK9y1zsJ_Mi8wY7WIH_E3cBusI7uSgNHTQq7dh6GurCYymPxnvvR8cdMQ4Om9SnmfqX9f3GCUncHXBVfxYCuH6ElLsjq74Z9ZXRGBLdwdM4BJWiyv4jzKfU80LmErPo7XT90DPzqa40T0pRblACQsaSGLn64TBoKvxlsO4HjJV8nBg3az5PrCkDsj8QTwgPLzJtP7WcT3pvcCI6O3O8OKlL2lR2-csFHNzCHetHaT1fmOLnVWuc5YIjqhYFEOjp8IKVhzxmcocxsd3R8bcvrjR8NcutOX5H7zmfD-GX17f64RT2c0zqSRdVpRxFZYlNycxd3rI591w9ImgZSkeGQN4Eg8us8oqmlRqfF5mO7QZXi_OsjJgnMdovqP1NB1IxHuTHQIyfESkQ3DoA_KYBF4_8DXhyjvE2D5_SQfZitUpjSwfWqZ_ghgVoOdLJokz4TBJQ9j_ec4jK3uf3nCwS_6Evx9zwbZxmin2CpnIrg4lFRmMpO6YgLfYKPqoI"

        private var selectedCategoryId: Int? = null
        private var selectedSubCategoryId: Int? = null


        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.fragment_stock_list, container, false)

            serviceList = view.findViewById(R.id.serviceList)
            subCategoryList = view.findViewById(R.id.subCategoryList)
            centerRecyclerView = view.findViewById(R.id.centerRecyclerView)

            setupCategorySidebar()
            setupSubCategoryList()
            setupStockRecycler()

            fetchLowStockList()
            fetchCategories()

            val searchView = view.findViewById<SearchView>(R.id.search_keyword)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    stockAdapter.filter(query ?: "")
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    stockAdapter.filter(newText ?: "")
                    return true
                }
            })


            return view
        }

        private fun setupCategorySidebar() {
            serviceList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            categoryAdapter = CategorySidebarAdapter(categories) { category ->
                selectedCategoryId = category.category_id
                selectedSubCategoryId = null // reset subcategory

                // ✅ Use the helper class now
                CategoryHelper.handleCategorySelection(
                    jwtToken,
                    category.category_id,
                    category.category_name,
                    subCategoryList,
                    subCategories,
                    subCategoryAdapter
                ) { selectedSubId ->
                    selectedSubCategoryId = selectedSubId
                    fetchStockProducts()
                }
            }

            serviceList.adapter = categoryAdapter
        }

        private fun setupSubCategoryList() {
            subCategoryList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            subCategoryAdapter = SubCategoryHorizontalAdapter(subCategories) { subCategory ->
                selectedSubCategoryId = subCategory.subcategoryId  // keep 0 for category itself
                fetchStockProducts()

            }
            subCategoryList.adapter = subCategoryAdapter
        }

        private fun setupStockRecycler() {
            centerRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            stockAdapter = StockProductAdapter(stockProducts)
            centerRecyclerView.adapter = stockAdapter
        }
        private fun fetchCategories() {
            val input = CategoryInput(status = "1")
            ApiClient.instance.stockCategoryApi(jwtToken, input)
                .enqueue(object : Callback<CategoryListOutput> {
                    override fun onResponse(call: Call<CategoryListOutput>, response: Response<CategoryListOutput>) {
                        if (response.isSuccessful && response.body()?.status == true) {
                            categories.clear()
                            response.body()?.data?.let { categories.addAll(it) }
                            categoryAdapter.notifyDataSetChanged()

                            if (categories.isNotEmpty()) {
                                selectedCategoryId = categories[0].category_id
                                categoryAdapter.setSelectedIndex(0)

                                // Use helper for first category
                                CategoryHelper.handleCategorySelection(
                                    jwtToken,
                                    categories[0].category_id,
                                    categories[0].category_name,
                                    subCategoryList,
                                    subCategories,
                                    subCategoryAdapter
                                ) { selectedSubId ->
                                    selectedSubCategoryId = selectedSubId
                                    fetchStockProducts()
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<CategoryListOutput>, t: Throwable) {
                        Log.e("StockListFragment", t.message ?: "")
                    }
                })
        }

        private fun fetchStockProducts() {
            val input = if (selectedSubCategoryId == 0) {
                Input(status = "1", category_id = selectedCategoryId?.toString(), sub_category_id = null)
            } else {
                Input(status = "1", category_id = selectedCategoryId?.toString(), sub_category_id = selectedSubCategoryId?.toString())
            }

            ApiClient.instance.stockProductApi(jwtToken, input)
                .enqueue(object : Callback<StockProductOutput> {
                    override fun onResponse(call: Call<StockProductOutput>, response: Response<StockProductOutput>) {
                        if (!isAdded) return
                        if (response.isSuccessful && response.body()?.status == true) {
                            response.body()?.data?.let { stockAdapter.updateList(it) }
                        } else {
                            stockProducts.clear()
                            stockAdapter.notifyDataSetChanged()
                        }
                    }

                    override fun onFailure(call: Call<StockProductOutput>, t: Throwable) {
                        Log.e("StockListFragment", t.message ?: "")
                    }
                })
        }


        private fun fetchLowStockList() {
            val input = Input(status = "1")
            ApiClient.instance.lowStockList(jwtToken, input)
                .enqueue(object : Callback<LowStockProductOutput> {
                    override fun onResponse(call: Call<LowStockProductOutput>, response: Response<LowStockProductOutput>) {
                        if (response.isSuccessful && response.body()?.status == true) {
                            lowStockData.clear()
                            response.body()?.data?.let { lowStockData.addAll(it) }

                            val fragment = parentFragmentManager.findFragmentByTag("LowStockListFragment") as? LowStockListFragment
                            fragment?.refreshData()
                        }
                    }

                    override fun onFailure(call: Call<LowStockProductOutput>, t: Throwable) {
                        Log.e("StockListFragment", "Low stock API failed: ${t.message}")
                    }
                })
        }



    }
