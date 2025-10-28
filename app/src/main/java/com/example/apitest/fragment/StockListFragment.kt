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

        private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZTRjY2JlNDBmYWFiZmE5YTg1YmVkMDdjOTcyMzAzNWY3MTljNzQ4M2Q3YzIzMjg3M2E5ZjZmN2QyMjQ1Y2E3MjVhNjVlMTQ5ZGRiZjBkYjgiLCJpYXQiOjE3NjE2MzM3OTIuNDI5OTk5LCJuYmYiOjE3NjE2MzM3OTIuNDMwMDAzLCJleHAiOjE3OTMxNjk3OTIuNDIyNjY5LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.SutcB4SMDdEzgfDZ1lnzGDjY4gf1aSwNJMGFnKF3RXWGbB4MhP6R8wRLCa2wtCMZ1egUQc1LxPMq2S9P-hfCeh4stj4W-zlWf0xOfvswvWVos14aP5RBTjbZ8Rc3PrmAwiFvnzu7PpRtqy8hGF_h1nt2dJrZpT4k0CIuvpUK8afLBKYfpr0i1NOx1awb2Z6Uo5YfJRQrB7y4wrs8TEsnbuAIdC4JG6-9Oi_TPwZ42SGatw4UEUvm09I3SjKGjZRDCOMJZLbSc1O4C6B53aK9hNQKCjnwsSXWc37h-cA6lKg-DSfm6K1usg0yHeAsE-2uya2_b8_TNq3LN4Mb04S820FmpnP0RqtDoPeoqBUSTacd0bSINimYpNv8NyiaO0D6k0J3HzaNd5MmwORyHpTNnVEaM8l0O5iyI-UIa3bPaOBnltamiAydE2EmUA9pfRQy3HWd6yZnxfuITM0THdj73ju1Qh3D0WVP7aUFR-3XUbp5qVflBZkiBe0klClG94ubWNFMX6vebixLQ21KsDvEDLj2Xy0hoLY-g6sm33l14NwbSiUgZ0VQI_3WbOzSpUvU0sprU7ozX7y7-nwjIXjshOQ5ymktZUMCN7LyCbvgX-qcGyxbS8C9JN9BhmvhagIBatDpPZw75arXlksHzxKnytbLG3BVxFHXxkp9jSBqW2s"
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
