package com.example.apitest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.adapter.POSAdapter
import com.example.apitest.adapter.PosCategoryAdapter
import com.example.apitest.adapter.PosSubCategoryAdapter
import com.example.apitest.cartScreen.CartActivity
import com.example.apitest.dataModel.CategoryList
import com.example.apitest.dataModel.CategoryOutput
import com.example.apitest.dataModel.Input
import com.example.apitest.dataModel.NewProductList
import com.example.apitest.dataModel.NewProductOutput
import com.example.apitest.dataModel.ProductInput
import com.example.apitest.dataModel.ProfileOutput
import com.example.apitest.dataModel.SubCategoryDetails
import com.example.apitest.dataModel.SubCategoryOutput
import com.example.apitest.helperClass.CartManager
import com.example.apitest.helperClass.NavigationActivity
import com.example.apitest.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class POSActivity : NavigationActivity() {
    private val jwtToken ="Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiOTMyZGFhM2IyNGZmM2Q0ZTY5NmQwYTdlODdhNmY1Y2ViOTA1Y2VhMzc0NWU5NmFlMTBhZWEwZjY2MDkxZDZiMWI1MjIwYjIwYmU4N2EyNDEiLCJpYXQiOjE3NTg3OTI2NDEuMjExMDEsIm5iZiI6MTc1ODc5MjY0MS4yMTEwMTMsImV4cCI6MTc5MDMyODY0MS4yMDQ5NjYsInN1YiI6IjYiLCJzY29wZXMiOltdfQ.lcZz0mc3u-to55t0p23p5g_Z1NQHM23K6xT8vaRAr7X9qHsMbyN9OEq-KuHhZGaqikcY-7ak26uM3_mtoLpLfq6jivhUPFC06JL7ID3HRHOUsWY05CqjIwPpOfjop127eWyz1CYmBmZx3mKsSuE2rvNK91OsROryf1Dz-Px0SnVJ1uDJGK9y1zsJ_Mi8wY7WIH_E3cBusI7uSgNHTQq7dh6GurCYymPxnvvR8cdMQ4Om9SnmfqX9f3GCUncHXBVfxYCuH6ElLsjq74Z9ZXRGBLdwdM4BJWiyv4jzKfU80LmErPo7XT90DPzqa40T0pRblACQsaSGLn64TBoKvxlsO4HjJV8nBg3az5PrCkDsj8QTwgPLzJtP7WcT3pvcCI6O3O8OKlL2lR2-csFHNzCHetHaT1fmOLnVWuc5YIjqhYFEOjp8IKVhzxmcocxsd3R8bcvrjR8NcutOX5H7zmfD-GX17f64RT2c0zqSRdVpRxFZYlNycxd3rI591w9ImgZSkeGQN4Eg8us8oqmlRqfF5mO7QZXi_OsjJgnMdovqP1NB1IxHuTHQIyfESkQ3DoA_KYBF4_8DXhyjvE2D5_SQfZitUpjSwfWqZ_ghgVoOdLJokz4TBJQ9j_ec4jK3uf3nCwS_6Evx9zwbZxmin2CpnIrg4lFRmMpO6YgLfYKPqoI"

    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var subCategoryRecyclerView: RecyclerView
    private lateinit var centerRecyclerView: RecyclerView

    private lateinit var categoryAdapter: PosCategoryAdapter
    private lateinit var subCategoryAdapter: PosSubCategoryAdapter
    private lateinit var productAdapter: POSAdapter

    private var categoryList = mutableListOf<CategoryList>()
    private val subCategoryList = mutableListOf<SubCategoryDetails>()
    private val productList = mutableListOf<NewProductList>()

    private var selectedCategoryId: String? = null
    private var selectedSubCategoryId: String? = null

    private lateinit var cartSummaryBar: View
    private lateinit var totalItemsText: TextView
    private lateinit var totalAmountText: TextView
    private lateinit var viewBillBtn: Button
    private lateinit var cartBadge: TextView
    private var quantityStatus: String = "0"

    private lateinit var viewBillsLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posactivity)
        setupBottomNavigation("pos")

        getUserProfile()

        cartBadge = findViewById(R.id.cartBadge)
        updateCartBadge()

        viewBillsLayout = findViewById(R.id.viewBillsLayout)
        viewBillsLayout.setOnClickListener {
            val totalItems = CartManager.getDistinctItemsCount() // count of items in cart

            if (totalItems > 0) {
                // If cart has items → open CartActivity
                val intent = Intent(this, CartActivity::class.java)
                startActivity(intent)
            } else {
                // Cart empty → stay in POS screen (maybe show a Toast)
                Toast.makeText(this, "Add products to cart first", Toast.LENGTH_SHORT).show()
            }
        }




        cartSummaryBar = findViewById(R.id.cartSummaryBar)
        totalItemsText = findViewById(R.id.totalItems)
        totalAmountText = findViewById(R.id.totalAmount)
        viewBillBtn = findViewById(R.id.viewBillBtn)
        cartSummaryBar.visibility = View.GONE


        // Category Adapter
        categoryRecyclerView = findViewById(R.id.serviceList)
        categoryRecyclerView.layoutManager = LinearLayoutManager(this)
        categoryAdapter = PosCategoryAdapter(categoryList) { category, _ ->
            val newCategoryId = category.categoryId?.toString()
            if (selectedCategoryId != newCategoryId) {
                selectedCategoryId = newCategoryId
                selectedSubCategoryId = null
                subCategoryList.clear()
                subCategoryAdapter.resetSelection()
                subCategoryAdapter.notifyDataSetChanged()
                subCategoryRecyclerView.visibility = View.GONE
            }
            getSubCategories(newCategoryId)
            getPOSProducts(newCategoryId.toString(), null)
        }
        categoryRecyclerView.adapter = categoryAdapter


        // Subcategory Adapter
        subCategoryRecyclerView = findViewById(R.id.subCategoryList)
        subCategoryRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        subCategoryAdapter = PosSubCategoryAdapter(subCategoryList) { subCategory, _ ->
            val categoryId = selectedCategoryId ?: return@PosSubCategoryAdapter
            val subCategoryId = subCategory.subcategoryId?.toString()

            selectedSubCategoryId = subCategoryId

            // ✅ If subcategory ID == category ID → show category-level products
            if (subCategoryId == categoryId) {
                getPOSProducts(categoryId, null)
            } else {
                getPOSProducts(categoryId, subCategoryId)
            }
        }


        subCategoryRecyclerView.adapter = subCategoryAdapter


        // Products RecyclerView
        centerRecyclerView = findViewById(R.id.centerRecyclerView)
        centerRecyclerView.layoutManager = LinearLayoutManager(this)
        productAdapter = POSAdapter(
            products = productList,
            onCartChange = { updatedProducts ->
                updateCartBar()
                updateCartBadge()
            },
            quantityStatus = quantityStatus // pass the value from profile API
        )



        centerRecyclerView.adapter = productAdapter


        // Fetch categories
        getCategory()
    }

    private fun getCategory() {
        val input = Input(status = "1")
        ApiClient.instance.categoryApi(jwtToken, input)
            .enqueue(object : Callback<CategoryOutput> {
                override fun onResponse(
                    call: Call<CategoryOutput>,
                    response: Response<CategoryOutput>,
                ) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        categoryList.clear()
                        response.body()?.categoryList?.let { categoryList.addAll(it) }
                        categoryAdapter.notifyDataSetChanged()

                        // ✅ Auto-select first category (if available)
                        if (categoryList.isNotEmpty()) {
                            val firstCategory = categoryList[0]
                            selectedCategoryId = firstCategory.categoryId?.toString()
                            categoryRecyclerView.post {
                                categoryAdapter.selectCategory(0) // new helper
                            }

                            // Fetch subcategories & products
                            getSubCategories(selectedCategoryId)
                            getPOSProducts(selectedCategoryId.toString(), null)
                        }
                    } else {
                        Toast.makeText(this@POSActivity, "No categories found", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<CategoryOutput>, t: Throwable) {
                    Toast.makeText(
                        this@POSActivity,
                        "API Error: ${t.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }


    private fun getSubCategories(categoryId: String?) {
        if (categoryId.isNullOrEmpty()) return

        val input = Input(status = "1", category_id = categoryId)
        ApiClient.instance.subCategoryApi(jwtToken, input)
            ?.enqueue(object : Callback<SubCategoryOutput> {
                override fun onResponse(
                    call: Call<SubCategoryOutput>,
                    response: Response<SubCategoryOutput>,
                ) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        val subList = response.body()?.data ?: emptyList()

                        subCategoryList.clear()
                        subCategoryList.addAll(subList)

                        if (subList.isNotEmpty()) {
                            subCategoryRecyclerView.visibility = View.VISIBLE

                            // ✅ Auto-select first subcategory
                            subCategoryAdapter.notifyDataSetChanged()
                            subCategoryRecyclerView.post {
                                subCategoryAdapter.setSelectedIndex(0)
                                val firstSub = subList[0]

                                // ✅ If first subcategory has same ID as category → load category products
                                if (firstSub.subcategoryId?.toString() == categoryId) {
                                    getPOSProducts(categoryId, null)
                                } else {
                                    getPOSProducts(categoryId, firstSub.subcategoryId?.toString())
                                }
                            }
                        } else {
                            subCategoryRecyclerView.visibility = View.GONE
                            getPOSProducts(categoryId, null)
                        }
                    } else {
                        subCategoryList.clear()
                        subCategoryRecyclerView.visibility = View.GONE
                        subCategoryAdapter.notifyDataSetChanged()
                        getPOSProducts(categoryId, null)
                    }
                }

                override fun onFailure(call: Call<SubCategoryOutput>, t: Throwable) {}
            } as Callback<SubCategoryOutput?>)
    }


    private fun getPOSProducts(categoryId: String, subCategoryId: String?) {
        val input = ProductInput(
            categoryId = categoryId,
            subCategoryId = subCategoryId,
            status = "1",
            page = "1"
        )
        ApiClient.instance.posProductApi(jwtToken, input)
            ?.enqueue(object : Callback<NewProductOutput> {
                override fun onResponse(
                    call: Call<NewProductOutput>,
                    response: Response<NewProductOutput>,
                ) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        productList.clear()
                        response.body()?.data?.forEach { product ->
                            product.productPrice?.forEach { variation ->
                                val key = "${product.productId}_${variation.productPriceId}"
                                // restore previous quantity in cart
                                variation.selectedQuantity = CartManager.cartMap[key] ?: 0
                                CartManager.allProductsMap[key] = variation
                            }
                        }
                        response.body()?.data?.let { productList.addAll(it) }
                        productAdapter.notifyDataSetChanged()
                        updateCartBar()
                        updateCartBadge()
                    }
                }

                override fun onFailure(call: Call<NewProductOutput>, t: Throwable) {}
            })
    }


    private fun getUserProfile() {
        val input = Input(status = "1")
        ApiClient.instance.getUserDetails(jwtToken, input)
            ?.enqueue(object : Callback<ProfileOutput> {
                override fun onResponse(call: Call<ProfileOutput>, response: Response<ProfileOutput>) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        quantityStatus = response.body()?.userDetails?.quantity_status ?: "0"

                        // Recreate productAdapter with quantityStatus
                        productAdapter = POSAdapter(productList, { updatedProducts ->
                            updateCartBar()
                            updateCartBadge()
                        }, quantityStatus)

                        centerRecyclerView.adapter = productAdapter
                    }
                }

                override fun onFailure(call: Call<ProfileOutput>, t: Throwable) {}
            } as Callback<ProfileOutput?>)
    }


    // --- Cart summary update ---
    private fun updateCartBar() {
        val totalQty = CartManager.getDistinctItemsCount()          // total distinct variation items
        val totalAmount = CartManager.getTotalAmount()             // total price

        cartSummaryBar.visibility = if (totalQty > 0) View.VISIBLE else View.GONE
        totalItemsText.text = "$totalQty item${if (totalQty > 1) "s" else ""}"
        totalAmountText.text = "₹%.2f".format(totalAmount)
    }

    private fun updateCartBadge() {
        val totalItems = CartManager.getDistinctItemsCount()
        if (totalItems > 0) {
            cartBadge.visibility = View.VISIBLE
            cartBadge.text = totalItems.toString()
        } else {
            cartBadge.visibility = View.GONE
        }
    }


    private fun openCartScreen() {
        val intent = Intent(this, CartActivity::class.java)
        startActivity(intent)
    }


}
