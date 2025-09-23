package com.example.apitest.fragment

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apitest.AddProductActivity
import com.example.apitest.adapter.ItemsAdapter
import com.example.apitest.adapter.SidebarCategoryAdapter
import com.example.apitest.adapter.SubCategoryHorizontalAdapter
import com.example.apitest.dataModel.*
import com.example.apitest.databinding.FragmentItemsBinding
import com.example.apitest.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ItemsFragment : Fragment() {

    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiNmZiMTgzMGFlYzFkNzUzOTdiNDBjMmE3NWJhOWRmZWU4MTU3YWMxZmZlMTg2NGYxZWMwOTYyYzA1NjE1YThjNTlkZjg3MjVjNDlmNmJmZTYiLCJpYXQiOjE3NTg2MTM2MzguMzQyMTUzLCJuYmYiOjE3NTg2MTM2MzguMzQyMTU3LCJleHAiOjE3OTAxNDk2MzguMzM2ODI2LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.QiPgcG55zwKdr_U471IaYnBQkUk437w5vRrmjxWos1zRADMFKLuovtJ8IqoTzbkQ1GMWE0fr6c22cdBq9Eq4PlY71A8ocqua6rhGKTS5h7ziYjA8y_KNRSmvWeSfrYF59FCv_sl_Yi8mci_Gl6lzLV5yzf-gQOcmxQ0m4NifHTxCZZEOXloSS0V2KtiECV1MwATuDGLGv7QYAwte7XEIZiCFjTJUjGGKdcXHGdCPXnsnlSSzyYUCuljabM4Of3dnP6QV6YVEuRkAiSn7HvyqgNpi34ux4lsVXFMWy1qnbI-VI_fo2Vcf5uZa8B4KBYVNT7YkV2_KcxEsdZ-ZhRzGbm6erYvUuwWUTLj8DHRUHfH-s1sOO4j3u8SQeVV47OfXB6Wo-CghPzuMQBtTvoQe2_zgAV9QrCS-xsk-uJxHLxp_dao9igrzB6fskUGshJ70IKLatiF3IDXuyVLZNqOJUblUYpoyvKuj4dBa-BZUnS35m9jm5Rz5XCkeIMIm26VTvwiNcuTg0cAXhpe97yZ_Ir0FV7-8YNQCPTAtCrC4IA0r6cuLTkWjZWa-gokQKbMWble8R9VoP-OIm0zFbISJUBtI-B0cif3oxTwr7m12jubPutnFl_HZyREBOticvsTf6q48cKUxNSYeyOW3aMdbZ584yRv95UYKUou0k0LbezM"
    private var _binding: FragmentItemsBinding? = null
    private val binding get() = _binding!!

    private val categoriesList = mutableListOf<Category>()
    private val itemsList = mutableListOf<StockProductData>()
    private val subCategoryList = mutableListOf<SubCategoryDetails>()

    private lateinit var itemsAdapter: ItemsAdapter
    private lateinit var subCategoryAdapter: SubCategoryHorizontalAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Floating Add Product
        binding.btnAddProduct.setOnClickListener {
            val stockEnabled = requireContext().getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("stock_enabled", false)
            val intent = Intent(requireContext(), AddProductActivity::class.java)
            intent.putExtra("stock_enabled", stockEnabled)
            addProductLauncher.launch(intent)
        }

        // Sidebar RecyclerView
        binding.serviceList.layoutManager = LinearLayoutManager(requireContext())
        binding.serviceList.adapter = SidebarCategoryAdapter(categoriesList) { category ->

            // Clear old products immediately
            itemsList.clear()
            itemsAdapter.notifyDataSetChanged()

            if (category.sub_category_status == "1") {
                fetchSubCategories(category.category_id.toString())
            } else {
                // Clear subcategory list and hide
                subCategoryList.clear()
                subCategoryAdapter.notifyDataSetChanged()
                binding.subCategoryList.visibility = View.GONE

                // Fetch items for this category only
                fetchItems(category.category_id.toString(), null)
            }
        }




        // Center RecyclerView
        binding.centerRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        itemsAdapter = ItemsAdapter(itemsList)
        binding.centerRecyclerView.adapter = itemsAdapter

        // Subcategory horizontal RecyclerView
        binding.subCategoryList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        subCategoryAdapter = SubCategoryHorizontalAdapter(subCategoryList) { subCategory ->
            fetchItems(
                categoryId = subCategory.categoryId.toString(),
                subCategoryId = subCategory.subcategoryId?.toString()
            )
        }
        binding.subCategoryList.adapter = subCategoryAdapter

        fetchCategories()
        fetchUserProfile()
    }
    private val addProductLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val itemName = data.getStringExtra("item_name")
                val newProducts = data.getParcelableArrayListExtra<StockProductData>("new_products")

                newProducts?.forEach { product ->
                    product.productName = itemName
                }

                if (!newProducts.isNullOrEmpty()) {
                    itemsList.addAll(newProducts)
                    itemsAdapter.notifyItemRangeInserted(itemsList.size - newProducts.size, newProducts.size)
                    binding.centerRecyclerView.scrollToPosition(itemsList.size - 1)
                }

                // Determine category and subcategory properly
                val categoryId = data.getIntExtra("category_id", -1).takeIf { it != -1 }?.toString()
                val subCategoryId = data.getIntExtra("sub_category_id", -1).takeIf { it != -1 }?.toString()
                val subCategoryStatus = data.getIntExtra("sub_category_status", 0) // pass from AddProductActivity

                if (!categoryId.isNullOrEmpty()) {
                    if (subCategoryStatus == 1 && !subCategoryId.isNullOrEmpty()) {
                        // Only fetch by subcategory if it exists
                        fetchItems(categoryId, subCategoryId)
                    } else {
                        // Category has no subcategory
                        fetchItems(categoryId, null)
                    }
                }
            }
        }


    private fun fetchCategories() {
        itemsList.clear()
        itemsAdapter.notifyDataSetChanged()
        val input = CategoryInput(status = "1")
        ApiClient.instance.stockCategoryApi(jwtToken, input)
            .enqueue(object : Callback<CategoryListOutput> {
                override fun onResponse(
                    call: Call<CategoryListOutput>,
                    response: Response<CategoryListOutput>
                ) {
                    if (!isAdded || _binding == null) return
                    if (response.isSuccessful) {
                        val categories = response.body()?.data ?: emptyList()
                        categoriesList.clear()
                        categoriesList.addAll(categories)
                        binding.serviceList.adapter?.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<CategoryListOutput>, t: Throwable) {
                    if (!isAdded || _binding == null) return
                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchItems(categoryId: String, subCategoryId: String? = null) {
        // Clear list immediately
        itemsList.clear()
        itemsAdapter.notifyDataSetChanged()  // Always notify

        val input = if (!subCategoryId.isNullOrEmpty()) {
            Input(category_id = categoryId, sub_category_id = subCategoryId, status = "1")
        } else {
            Input(category_id = categoryId, status = "1")
        }

        ApiClient.instance.getAllProduct(jwtToken, input)
            .enqueue(object : Callback<StockProductOutput> {
                override fun onResponse(
                    call: Call<StockProductOutput>,
                    response: Response<StockProductOutput>
                ) {
                    if (!isAdded || _binding == null) return

                    val products = if (response.isSuccessful && response.body()?.status == true) {
                        response.body()?.data ?: emptyList()
                    } else {
                        emptyList()
                    }

                    // Always update adapter
                    itemsList.clear()
                    itemsList.addAll(products)
                    itemsAdapter.notifyDataSetChanged()  // This ensures empty list is reflected

                    if (products.isEmpty()) {
                        Toast.makeText(requireContext(), "No items found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<StockProductOutput>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }







    private fun fetchSubCategories(categoryId: String? = null, status: String? = "1") {
        val input = Input(category_id = categoryId, status = status)
        ApiClient.instance.getAllSubCategoryApi(jwtToken, input)
            ?.enqueue(object : Callback<SubCategoryOutput?> {
                override fun onResponse(call: Call<SubCategoryOutput?>, response: Response<SubCategoryOutput?>) {
                    if (!isAdded || _binding == null) return
                    val data = response.body()?.data?.filter { it.categoryId.toString() == categoryId } ?: emptyList()
                    subCategoryList.clear()
                    subCategoryList.addAll(data)
                    subCategoryAdapter.notifyDataSetChanged()
                    binding.subCategoryList.visibility = if (data.isNotEmpty()) View.VISIBLE else View.GONE
                }

                override fun onFailure(call: Call<SubCategoryOutput?>, t: Throwable) {
                    if (!isAdded || _binding == null) return
                    binding.subCategoryList.visibility = View.GONE
                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchUserProfile(status: String? = "1") {
        val input = Input(status = "1")
        ApiClient.instance.getUserDetails(jwtToken, input)
            ?.enqueue(object : Callback<ProfileOutput?> {
                override fun onResponse(call: Call<ProfileOutput?>, response: Response<ProfileOutput?>) {
                    if (!isAdded || _binding == null) return
                    val profile = response.body() ?: return
                    val stockEnabled = profile.userDetails?.stock_status == "1"
                    val showMRP = profile.userDetails?.mrp_price_status == "1"
                    val showWholesale = profile.userDetails?.whole_sale_price_status == "1"
                    val showTax = profile.userDetails?.product_tax_status == "1"

                    requireContext().getSharedPreferences("app_prefs", AppCompatActivity.MODE_PRIVATE)
                        .edit {
                            putBoolean("stock_enabled", stockEnabled)
                            putBoolean("show_mrp", showMRP)
                            putBoolean("show_wholesale", showWholesale)
                            putBoolean("show_tax", showTax)
                        }
                }

                override fun onFailure(call: Call<ProfileOutput?>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
