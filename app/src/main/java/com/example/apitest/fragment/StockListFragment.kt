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

        private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZGE0YjJmNjlmZGJkNjMwMDMyNGE3MWNkZWRhMDI2ZWI2YTIwMGM4NWIyNTI2MTNjOTZhZGIyMDA2MTE3YjMxMGI0MTFjYjczNzNmZmNlZDAiLCJpYXQiOjE3NjAzMjkzNTkuNDc3MTA1LCJuYmYiOjE3NjAzMjkzNTkuNDc3MTA4LCJleHAiOjE3OTE4NjUzNTkuNDcyNjI1LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.C3ySWdDX7BRHm4qzwWFZZofL_DEx3C2Qjy7iEUWxy9GdrL8OJS7m7Kk_Oe4HtFaT7DvPMEWE_c9kIC8RalMXflXTvPGKkfsw7yxdVxZOKSE20UNZiSbScAdvx3RxAz-XoHK4wJr7wepspLad5y5KCv4RyPXAJl8sIjFELfiCMoxt1CiYGp5_GhsOjbMeSLWSBoDwd3H4MLNvUyU2KN2zhvQaRRUh4T-L11mZgmd_8A8kWZbp_bO6AK-3hGHFGd7VaT2Xqoi4asmn0ABlxusVYWG6hw9UhnU-_uxOVFQLAHog-WKfbahCwfkssXtK07wMpk-ZGHfRn7ujbkrMAX5gNgNkcNQZPRMkUSrokHylEJXKC7UOAgUiK8fy32bIlmFuMQE9hTuuQjHWJ8hdEqtPaXVIcc1oXURtZhCWTp2APH9RE4_L41NYStog_bVMdXwRO_a6QEg_ex0moqxwtRZKivnIF4DKm6WLj45X0FLj-F7HTlZ-eoc9j3w_dVaVyhhxEKUiTyQSJ_AwVKMTbAUmxvWY3OnoIAmu4WYrbC4T4tA2cWoB9yXKna8Yfbil_vC46tLZweGF7RRZR2MPT16q-iCzKG73JqAMphV4NO7b-bMk6mhvgz8TR0_YUewsPg2CVvgdvEmnV4DE4znhnwiLMniN0kPGzF5pindkKTVNDb8"

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
