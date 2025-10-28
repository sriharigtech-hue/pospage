package com.example.apitest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.adapter.POSAdapter
import com.example.apitest.adapter.PosCategoryAdapter
import com.example.apitest.adapter.PosSubCategoryAdapter
import com.example.apitest.cartScreen.CartActivity
import com.example.apitest.dataModel.*
import com.example.apitest.helperClass.NavigationActivity
import com.example.apitest.network.ApiClient
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class POSActivity : NavigationActivity() {

    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZTRjY2JlNDBmYWFiZmE5YTg1YmVkMDdjOTcyMzAzNWY3MTljNzQ4M2Q3YzIzMjg3M2E5ZjZmN2QyMjQ1Y2E3MjVhNjVlMTQ5ZGRiZjBkYjgiLCJpYXQiOjE3NjE2MzM3OTIuNDI5OTk5LCJuYmYiOjE3NjE2MzM3OTIuNDMwMDAzLCJleHAiOjE3OTMxNjk3OTIuNDIyNjY5LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.SutcB4SMDdEzgfDZ1lnzGDjY4gf1aSwNJMGFnKF3RXWGbB4MhP6R8wRLCa2wtCMZ1egUQc1LxPMq2S9P-hfCeh4stj4W-zlWf0xOfvswvWVos14aP5RBTjbZ8Rc3PrmAwiFvnzu7PpRtqy8hGF_h1nt2dJrZpT4k0CIuvpUK8afLBKYfpr0i1NOx1awb2Z6Uo5YfJRQrB7y4wrs8TEsnbuAIdC4JG6-9Oi_TPwZ42SGatw4UEUvm09I3SjKGjZRDCOMJZLbSc1O4C6B53aK9hNQKCjnwsSXWc37h-cA6lKg-DSfm6K1usg0yHeAsE-2uya2_b8_TNq3LN4Mb04S820FmpnP0RqtDoPeoqBUSTacd0bSINimYpNv8NyiaO0D6k0J3HzaNd5MmwORyHpTNnVEaM8l0O5iyI-UIa3bPaOBnltamiAydE2EmUA9pfRQy3HWd6yZnxfuITM0THdj73ju1Qh3D0WVP7aUFR-3XUbp5qVflBZkiBe0klClG94ubWNFMX6vebixLQ21KsDvEDLj2Xy0hoLY-g6sm33l14NwbSiUgZ0VQI_3WbOzSpUvU0sprU7ozX7y7-nwjIXjshOQ5ymktZUMCN7LyCbvgX-qcGyxbS8C9JN9BhmvhagIBatDpPZw75arXlksHzxKnytbLG3BVxFHXxkp9jSBqW2s"
    companion object {
        const val CART_REQUEST_CODE = 1001
    }

    // RecyclerViews
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

    // Profile flags
    private var customItemEnabled = false
    private var taxEnabled = false
    private var mrpEnabled = false
    private var wholeEnabled = false
    private var unitEnabled = false

    // Floating cart
    private lateinit var cartSummaryBar: RelativeLayout
    private lateinit var totalItemsText: TextView
    private lateinit var totalAmountText: TextView
    private lateinit var viewBillBtn: Button
    private lateinit var cartBadge: TextView
    private var cartItemCount = 0

    // Shared cart list
    private val cartList = mutableListOf<CartItem>()
    // Custom products
    private val customProductList = mutableListOf<CustomProduct>()

    data class CustomProduct(
        val name: String,
        var qty: Double,
        val price: Double
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posactivity)
        setupBottomNavigation("pos")


        // Cart summary views
        cartSummaryBar = findViewById(R.id.cartSummaryBar)
        totalItemsText = findViewById(R.id.totalItems)
        totalAmountText = findViewById(R.id.totalAmount)
        viewBillBtn = findViewById(R.id.viewBillBtn)
        cartBadge = findViewById(R.id.cartBadge)
        val viewBillsLayout = findViewById<RelativeLayout>(R.id.viewBillsLayout)
        viewBillsLayout.setOnClickListener {
            if (cartList.isNotEmpty()) {
                val input = Input(status = "1")
                ApiClient.instance.getUserDetails(jwtToken, input)
                    ?.enqueue(object : Callback<ProfileOutput?> {
                        override fun onResponse(call: Call<ProfileOutput?>, response: Response<ProfileOutput?>) {
                            val profileOutput = response.body()?.userDetails
                            val discountType = profileOutput?.discount_type ?: "0" // 👈 default = 0 (none)

                            val intent = Intent(this@POSActivity, CartActivity::class.java)
                            intent.putParcelableArrayListExtra("cart_items", ArrayList(cartList))
                            intent.putExtra("discount_type", discountType) // 👈 Pass only what we need
                            startActivityForResult(intent, CART_REQUEST_CODE)
                        }

                        override fun onFailure(call: Call<ProfileOutput?>, t: Throwable) {
                            Toast.makeText(this@POSActivity, "Failed to fetch profile", Toast.LENGTH_SHORT).show()
                        }
                    })
            } else {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
            }
        }



        setupRecyclerViews()
        setupCustomButton()
        getCategory()
        updateCartSummary()
    }



    private fun setupRecyclerViews() {
        // Category Recycler
        categoryRecyclerView = findViewById(R.id.serviceList)
        categoryRecyclerView.layoutManager = LinearLayoutManager(this)
        categoryAdapter = PosCategoryAdapter(categoryList) { category, _ ->
            val newCategoryId = category.categoryId?.toString()
            if (selectedCategoryId != newCategoryId) {
                selectedCategoryId = newCategoryId
                selectedSubCategoryId = null
                subCategoryList.clear()
                subCategoryAdapter.notifyDataSetChanged()
                subCategoryRecyclerView.visibility = View.GONE
            }
            getSubCategories(newCategoryId)
            getPOSProducts(newCategoryId.toString(), null)
        }
        categoryRecyclerView.adapter = categoryAdapter

        // Subcategory Recycler
        subCategoryRecyclerView = findViewById(R.id.subCategoryList)
        subCategoryRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        subCategoryAdapter = PosSubCategoryAdapter(subCategoryList) { subCategory, _ ->
            val categoryId = selectedCategoryId ?: return@PosSubCategoryAdapter
            val subCategoryId = subCategory.subcategoryId?.toString()
            selectedSubCategoryId = subCategoryId
            getPOSProducts(categoryId, if (subCategoryId == categoryId) null else subCategoryId)
        }
        subCategoryRecyclerView.adapter = subCategoryAdapter

        // Product Recycler
        centerRecyclerView = findViewById(R.id.centerRecyclerView)
        centerRecyclerView.layoutManager = LinearLayoutManager(this)
        productAdapter = POSAdapter(products = productList) {
            syncCartWithProducts()
            updateCartSummary()
        }
        centerRecyclerView.adapter = productAdapter
    }

    // -------------------- UPDATE CART --------------------
    private fun updateCartSummary() {
        var totalQty = 0.0
        var totalAmount = 0.0

        cartList.forEach {
            totalQty += it.quantity
            totalAmount += it.quantity * it.price
        }

        if (totalQty > 0) {
            cartSummaryBar.visibility = View.VISIBLE
            totalItemsText.text =
                "${if (totalQty % 1 == 0.0) totalQty.toInt() else totalQty} item(s)"
            totalAmountText.text = "₹%.2f".format(totalAmount)
            cartBadge.visibility = View.VISIBLE
            cartBadge.text = totalQty.toInt().toString()
        } else {
            cartSummaryBar.visibility = View.GONE
            cartBadge.visibility = View.GONE
        }

    }

    // ---------------------- CUSTOM BUTTON ----------------------
    private fun setupCustomButton() {
        val customButton = findViewById<ImageView>(R.id.customItem)
        customButton.setOnClickListener { showCustomItemDialog() }
        getUserProfile { isEnabled ->
            customItemEnabled = isEnabled
            customButton.visibility = if (isEnabled) View.VISIBLE else View.GONE
        }
    }

    // ---------------------- USER PROFILE API ----------------------
    private fun getUserProfile(callback: (Boolean) -> Unit) {
        val input = Input(status = "1")
        ApiClient.instance.getUserDetails(jwtToken, input)
            ?.enqueue(object : Callback<ProfileOutput?> {
                override fun onResponse(call: Call<ProfileOutput?>, response: Response<ProfileOutput?>) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        val profile = response.body()?.userDetails
                        taxEnabled = profile?.product_tax_status == "1"
                        mrpEnabled = profile?.mrp_price_status == "1"
                        wholeEnabled = profile?.whole_sale_price_status == "1"
                        unitEnabled = profile?.unit_status == "1"
                        callback(profile?.custom_product_status == "1")
                    } else callback(false)
                }
                override fun onFailure(call: Call<ProfileOutput?>, t: Throwable) { callback(false) }
            })
    }

    // ---------------------- CATEGORY / SUBCATEGORY / PRODUCTS ----------------------
    private fun getCategory() {
        val input = Input(status = "1")
        ApiClient.instance.categoryApi(jwtToken, input)
            .enqueue(object : Callback<CategoryOutput> {
                override fun onResponse(call: Call<CategoryOutput>, response: Response<CategoryOutput>) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        categoryList.clear()
                        response.body()?.categoryList?.let { categoryList.addAll(it) }
                        categoryAdapter.notifyDataSetChanged()
                        if (categoryList.isNotEmpty()) {
                            selectedCategoryId = categoryList[0].categoryId?.toString()
                            getSubCategories(selectedCategoryId)
                            getPOSProducts(selectedCategoryId.toString(), null)
                        }
                    }
                }
                override fun onFailure(call: Call<CategoryOutput>, t: Throwable) {}
            })
    }

    private fun getSubCategories(categoryId: String?) {
        if (categoryId.isNullOrEmpty()) return
        val input = Input(status = "1", category_id = categoryId)
        ApiClient.instance.subCategoryApi(jwtToken, input)
            ?.enqueue(object : Callback<SubCategoryOutput> {
                override fun onResponse(call: Call<SubCategoryOutput>, response: Response<SubCategoryOutput>) {
                    subCategoryList.clear()
                    val subList = response.body()?.data ?: emptyList()
                    subCategoryList.addAll(subList)
                    subCategoryRecyclerView.visibility = if (subList.isNotEmpty()) View.VISIBLE else View.GONE
                    subCategoryAdapter.notifyDataSetChanged()
                }
                override fun onFailure(call: Call<SubCategoryOutput>, t: Throwable) {}
            } as Callback<SubCategoryOutput?>)
    }

    private fun getPOSProducts(categoryId: String, subCategoryId: String?) {
        val input = ProductInput(categoryId = categoryId, subCategoryId = subCategoryId, status = "1", page = "1")

        // 🧠 Save current selections before clearing list
        val selectedMap = mutableMapOf<String, Double>()
        productList.forEach { product ->
            product.productPrice?.forEach { price ->
                if (price.selectedQuantityDecimal > 0) {
                    selectedMap[product.productId.toString()] = price.selectedQuantityDecimal
                }
            }
        }

        ApiClient.instance.posProductApi(jwtToken, input)
            ?.enqueue(object : Callback<NewProductOutput> {
                override fun onResponse(call: Call<NewProductOutput>, response: Response<NewProductOutput>) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        productList.clear()
                        response.body()?.data?.let { newProducts ->
                            // 🪄 Restore previous selections
                            newProducts.forEach { newProd ->
                                val savedQty = selectedMap[newProd.productId.toString()]
                                if (savedQty != null && savedQty > 0) {
                                    newProd.productPrice?.firstOrNull()?.selectedQuantityDecimal = savedQty
                                }
                            }
                            productList.addAll(newProducts)
                        }
                        productAdapter.notifyDataSetChanged()
                        syncCartWithProducts()
                        updateCartSummary()
                    }
                }
                override fun onFailure(call: Call<NewProductOutput>, t: Throwable) {}
            })
    }


    // -------------------- SYNC CART WITH POS PRODUCTS --------------------
    private fun syncCartWithProducts() {
        cartList.clear()

        // Normal products
        productList.forEach { product ->
            product.productPrice?.forEach { price ->
                if (price.selectedQuantityDecimal > 0) {
                    cartList.add(
                        CartItem(
                            productId = product.productId.toString(),
                            name = product.productName ?: "N/A",
                            quantity = price.selectedQuantityDecimal,
                            price = price.productPrice?.toDoubleOrNull() ?: 0.0,
                            isCustom = false,
                            stockCount = price.stockCount?.toDouble()
                        )
                    )
                }
            }
        }

        // Custom products
        customProductList.forEach { custom ->
            cartList.add(
                CartItem(
                    productId = null,
                    name = custom.name,
                    quantity = custom.qty,
                    price = custom.price,
                    isCustom = true
                )
            )
        }

    }

    // ---------------------- CUSTOM ITEM DIALOG ----------------------
    private fun showCustomItemDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_custom_box, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val nameEdit = dialogView.findViewById<TextInputEditText>(R.id.name)
        val qtyEdit = dialogView.findViewById<TextInputEditText>(R.id.qty)
        val priceEdit = dialogView.findViewById<TextInputEditText>(R.id.price)
        val unitSpinner = dialogView.findViewById<Spinner>(R.id.unitSpinner)

        val priceTaxLayout = dialogView.findViewById<View>(R.id.priceTaxLayout)
        val mrpLayout = dialogView.findViewById<View>(R.id.mrpPriceLayout)
        val wholeLayout = dialogView.findViewById<View>(R.id.productWholeSalePriceLayout)
        val unitLayout = dialogView.findViewById<View>(R.id.unitSpinnerLayout)
        val unitTitleLayout = dialogView.findViewById<View>(R.id.unitSpinnerTitleLayout)

        priceTaxLayout.visibility = if (taxEnabled) View.VISIBLE else View.GONE
        mrpLayout.visibility = if (mrpEnabled) View.VISIBLE else View.GONE
        wholeLayout.visibility = if (wholeEnabled) View.VISIBLE else View.GONE
        unitLayout.visibility = if (unitEnabled) View.VISIBLE else View.GONE
        unitTitleLayout.visibility = if (unitEnabled) View.VISIBLE else View.GONE

        if (unitEnabled) fetchUnits(unitSpinner)

        val yesBtn = dialogView.findViewById<AppCompatTextView>(R.id.yes_btn)
        val noBtn = dialogView.findViewById<AppCompatTextView>(R.id.no_btn)

        noBtn.setOnClickListener { dialog.dismiss() }

        yesBtn.setOnClickListener {
            val name = nameEdit.text.toString().trim()
            val qty = qtyEdit.text.toString().trim().toDoubleOrNull()
            val price = priceEdit.text.toString().trim().toDoubleOrNull()

            if (name.isEmpty() || qty == null || price == null) {
                Toast.makeText(this, "Please fill valid fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val existing = customProductList.find { it.name.equals(name, true) }
            if (existing != null) {
                existing.qty += qty
            } else {
                customProductList.add(CustomProduct(name, qty, price))
            }

            syncCartWithProducts()
            updateCartSummary()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun fetchUnits(spinner: Spinner) {
        val input = Input(status = "1")
        ApiClient.instance.unitApi(jwtToken, input)
            .enqueue(object : Callback<UnitOutput> {
                override fun onResponse(call: Call<UnitOutput>, response: Response<UnitOutput>) {
                    val units = response.body()?.unitList ?: emptyList()
                    val adapter = ArrayAdapter(
                        this@POSActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        units.map { it.unitName ?: "N/A" }
                    )
                    spinner.adapter = adapter
                }

                override fun onFailure(call: Call<UnitOutput>, t: Throwable) {}
            })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CART_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val updatedCartItems = data.getParcelableArrayListExtra<CartItem>("updated_cart_items") ?: return

            // Reset all product quantities
            productList.forEach { product ->
                product.productPrice?.forEach { it.selectedQuantityDecimal = 0.0 }
            }

            // Apply updated quantities
            updatedCartItems.forEach { cartItem ->
                if (!cartItem.isCustom) {
                    productList.find { it.productId.toString() == cartItem.productId }?.productPrice?.forEach {
                        it.selectedQuantityDecimal = cartItem.quantity
                    }
                } else {
                    val existing = customProductList.find { it.name.equals(cartItem.name, true) }
                    if (existing != null) existing.qty = cartItem.quantity
                    else customProductList.add(CustomProduct(cartItem.name, cartItem.quantity, cartItem.price))
                }
            }
            syncCartWithProducts()
            productAdapter.notifyDataSetChanged()
            updateCartSummary()
        }
    }
}
