package com.example.apitest.fragment

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apitest.AddProductActivity
import com.example.apitest.adapter.ItemsAdapter
import com.example.apitest.adapter.SidebarCategoryAdapter
import com.example.apitest.adapter.SubCategoryHorizontalAdapter
import com.example.apitest.dataModel.Category
import com.example.apitest.dataModel.CategoryInput
import com.example.apitest.dataModel.CategoryListOutput
import com.example.apitest.dataModel.Input
import com.example.apitest.dataModel.ProfileOutput
import com.example.apitest.dataModel.StockProductData
import com.example.apitest.dataModel.StockProductOutput
import com.example.apitest.dataModel.SubCategoryDetails
import com.example.apitest.dataModel.SubCategoryOutput
import com.example.apitest.databinding.FragmentItemsBinding
import com.example.apitest.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ItemsFragment : Fragment() {

    private val jwtToken =
        "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiNjk3MmE4MjVjMjZhNmU2ZDAxMTk3NjdiNDMzYzc4NWEzNTdmYzYxYTZhMTBjNjQ2MGViYTc4ZWQxNDM4NmQyYjM2YTFkNWJjZTIwZTc0MmIiLCJpYXQiOjE3NTgyNTU4MTEuMjA1Njg1LCJuYmYiOjE3NTgyNTU4MTEuMjA1Njg3LCJleHAiOjE3ODk3OTE4MTEuMjAyMzI1LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.bWv1-ccFjlnUbHqgVKleTigmPBsohLqMMJ6KNx-zPgEsHzPvFn19WQV6kc85aXrKZOdbiazq4eb7y9I1wdXI8n-BQQ4jpqruQqW87KNzvuoOvd0qPKW-EbUFbst1I5QAgA4m0kySj8JxVHAFSgBtzocj42JUd0XuNHMtGjwLUH4QM4Njc-tkVSmVqIHN66LGoaslfkl5tQTxjFV0Xg1Ay1fyNdp5A1pSZPv4wTr9aAfR1nhrqA5FtU8x0BJyWcpk3ojQq3gWUB3CZgr0Qq7tMpzuZwufR-HWqCX-Be4YRZ1wyANAQHEt0JEp5HLV9htpfuCE7grP_sw-cywep-FxVlyKO0tyc7ykFnhOaI6YlREAms4m_NOpdleSnYv9or7uj8DQWI2F4318SoFSPFu1UsVI9n2Ygf54TZD4FDhMGjWSue2uBCS3HPleSyO6Qf2Lk64OovnYRXc_tJzddcvu8LEC8ihvZpDpr4KMmxGIM15c-4lh0gXpcOOPZXmHG4rG73ogFxVzJdp-nAs-h2U-Kh52kXmjFm2MAgfKSv-0IdgA2IXUxcWthRLO5WYtiggR-ghkTx9hy6Seyq5ZXYSmdnbJHcVRHyZYE6h7hl2pgeAIA3_er4oT_2DpmEW38pwoM__ezWYarjyPYkIEI2enMQTJE0FbCU7fe5wcuSMY140"
    private var _binding: FragmentItemsBinding? = null
    private val binding get() = _binding!!

    private val categoriesList = mutableListOf<Category>()            // Left sidebar
    private val itemsList = mutableListOf<StockProductData>()           // Center items (fixed type)
    private val subCategoryList = mutableListOf<SubCategoryDetails>() // Top horizontal

    private lateinit var itemsAdapter: ItemsAdapter
    private lateinit var subCategoryAdapter: SubCategoryHorizontalAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Floating Add Product
        binding.btnAddProduct.setOnClickListener {
            // Read the stock_enabled value from SharedPreferences (set after profile API call)
            val stockEnabled = requireContext().getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("stock_enabled", false)

            val intent = Intent(requireContext(), AddProductActivity::class.java)
            intent.putExtra("stock_enabled", stockEnabled) // pass it to AddProductActivity
            startActivity(intent)
        }


        // Left sidebar
        binding.serviceList.layoutManager = LinearLayoutManager(requireContext())
        binding.serviceList.adapter = SidebarCategoryAdapter(categoriesList) { category ->
            fetchItems(category.category_id.toString())

            if (category.sub_category_status == "1") {
                fetchSubCategories(category.category_id.toString(), status = "1")
            } else {
                subCategoryList.clear()
                subCategoryAdapter.notifyDataSetChanged()
                binding.subCategoryList.visibility = View.GONE
            }
        }

        // Center RecyclerView
        binding.centerRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        itemsAdapter = ItemsAdapter(itemsList)
        binding.centerRecyclerView.adapter = itemsAdapter

        // Subcategory horizontal RecyclerView
        // Subcategory horizontal RecyclerView
        binding.subCategoryList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        subCategoryAdapter = SubCategoryHorizontalAdapter(subCategoryList) { subCategory ->
            // Pass both categoryId & subCategoryId
            fetchItems(
                categoryId = subCategory.categoryId?.toString(),
                subCategoryId = subCategory.subcategoryId?.toString()
            )
        }
        binding.subCategoryList.adapter = subCategoryAdapter

        binding.subCategoryList.adapter = subCategoryAdapter

        fetchCategories()
        fetchUserProfile()

    }

    // Fetch main categories
    private fun fetchCategories() {
        val input = CategoryInput(status = "1")

        ApiClient.instance.stockCategoryApi(jwtToken, input)
            .enqueue(object : Callback<CategoryListOutput> {
                override fun onResponse(
                    call: Call<CategoryListOutput>,
                    response: Response<CategoryListOutput>,
                ) {

                    if (!isAdded || _binding == null) return
                    if (response.isSuccessful && response.body() != null) {
                        val categories = response.body()!!.data ?: emptyList()
                        categoriesList.clear()
                        categoriesList.addAll(categories)
                        binding.serviceList.adapter?.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<CategoryListOutput>, t: Throwable) {
                    if (!isAdded || _binding == null) return
                    Toast.makeText(
                        requireContext(),
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    // Fetch items
    private fun fetchItems(
        categoryId: String? = null,
        subCategoryId: String? = null,
        status: String? = "1",
    ) {
        val input = Input(
            category_id = categoryId,
            sub_category_id = subCategoryId,
            status = status
        )
        Log.d("ItemsFragment", "Sending input: $input")

        ApiClient.instance.getAllProduct(jwtToken, input)
            .enqueue(object : Callback<StockProductOutput> {
                override fun onResponse(
                    call: Call<StockProductOutput>,
                    response: Response<StockProductOutput>,
                ) {
                    Log.d("ItemsFragment", "Full response: $response")
                    if (response.isSuccessful && response.body()?.status == true) {
                        itemsList.clear()
                        itemsList.addAll(response.body()?.data ?: emptyList())
                        itemsAdapter.notifyDataSetChanged()
                    } else {
                        Log.d("ItemsFragment", "Error response: ${response.errorBody()?.string()}")
                        itemsList.clear()
                        itemsAdapter.notifyDataSetChanged()
                        Toast.makeText(requireContext(), "No items found", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<StockProductOutput>, t: Throwable) {
                    Log.d("ItemsFragment", "Network error: ${t.message}")
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    // Fetch subcategories
    private fun fetchSubCategories(categoryId: String? = null, status: String? = "1") {
        val input = Input(category_id = categoryId, status = status)
        Log.d("SubCategoryAPI", "Sending input: $input")

        ApiClient.instance.getAllSubCategoryApi(jwtToken, input)
            ?.enqueue(object : Callback<SubCategoryOutput?> {
                override fun onResponse(
                    call: Call<SubCategoryOutput?>,
                    response: Response<SubCategoryOutput?>,
                ) {
                    if (!isAdded || _binding == null) return
                    Log.d("SubCategoryAPI", "Full response: $response")
                    val data =
                        response.body()?.data?.filter { it.categoryId.toString() == categoryId }
                            ?: emptyList()

                    subCategoryList.clear()
                    subCategoryList.addAll(data)
                    subCategoryAdapter.notifyDataSetChanged()
                    binding.subCategoryList.visibility =
                        if (data.isNotEmpty()) View.VISIBLE else View.GONE
                }

                override fun onFailure(call: Call<SubCategoryOutput?>, t: Throwable) {
                    if (!isAdded || _binding == null) return
                    binding.subCategoryList.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun fetchUserProfile(status: String? = "1") {
        val input = Input(status = "1")
        ApiClient.instance.getUserDetails(jwtToken, input)
            ?.enqueue(object : Callback<ProfileOutput?> {
                override fun onResponse(
                    call: Call<ProfileOutput?>,
                    response: Response<ProfileOutput?>,
                ) {
                    if (!isAdded || _binding == null) return
                    Log.d("UserProfileAPI", "Full response: $response")
                    if (response.isSuccessful && response.body() != null) {

                        val profile = response.body()!!

                        // Example: API tells if stock toggle should be visible
                        val stockEnabled = profile.userDetails?.stock_status == "1"
                        Log.d("UserProfileAPI", "Stock Enabled: $stockEnabled")


                        // Save in SharedPreferences so AddProductActivity can use it
                        requireContext().getSharedPreferences(
                            "app_prefs",
                            AppCompatActivity.MODE_PRIVATE
                        )
                            .edit() {
                                putBoolean("stock_enabled", stockEnabled)
                            }
                    } else {
                        if (!isAdded || _binding == null) return
                        Log.d("UserProfileAPI", "Error response: ${response.errorBody()?.string()}")
                        Toast.makeText(requireContext(), "Profile load failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<ProfileOutput?>, t: Throwable) {
                    Log.d("UserProfileAPI", "Network error: ${t.message}")
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
