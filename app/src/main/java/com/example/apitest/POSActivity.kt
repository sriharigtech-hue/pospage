package com.example.apitest

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatTextView
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
import com.example.apitest.dataModel.UnitOutput
import com.example.apitest.helperClass.CartManager
import com.example.apitest.helperClass.NavigationActivity
import com.example.apitest.network.ApiClient
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class POSActivity : NavigationActivity() {
    private val jwtToken ="Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZGE0YjJmNjlmZGJkNjMwMDMyNGE3MWNkZWRhMDI2ZWI2YTIwMGM4NWIyNTI2MTNjOTZhZGIyMDA2MTE3YjMxMGI0MTFjYjczNzNmZmNlZDAiLCJpYXQiOjE3NjAzMjkzNTkuNDc3MTA1LCJuYmYiOjE3NjAzMjkzNTkuNDc3MTA4LCJleHAiOjE3OTE4NjUzNTkuNDcyNjI1LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.C3ySWdDX7BRHm4qzwWFZZofL_DEx3C2Qjy7iEUWxy9GdrL8OJS7m7Kk_Oe4HtFaT7DvPMEWE_c9kIC8RalMXflXTvPGKkfsw7yxdVxZOKSE20UNZiSbScAdvx3RxAz-XoHK4wJr7wepspLad5y5KCv4RyPXAJl8sIjFELfiCMoxt1CiYGp5_GhsOjbMeSLWSBoDwd3H4MLNvUyU2KN2zhvQaRRUh4T-L11mZgmd_8A8kWZbp_bO6AK-3hGHFGd7VaT2Xqoi4asmn0ABlxusVYWG6hw9UhnU-_uxOVFQLAHog-WKfbahCwfkssXtK07wMpk-ZGHfRn7ujbkrMAX5gNgNkcNQZPRMkUSrokHylEJXKC7UOAgUiK8fy32bIlmFuMQE9hTuuQjHWJ8hdEqtPaXVIcc1oXURtZhCWTp2APH9RE4_L41NYStog_bVMdXwRO_a6QEg_ex0moqxwtRZKivnIF4DKm6WLj45X0FLj-F7HTlZ-eoc9j3w_dVaVyhhxEKUiTyQSJ_AwVKMTbAUmxvWY3OnoIAmu4WYrbC4T4tA2cWoB9yXKna8Yfbil_vC46tLZweGF7RRZR2MPT16q-iCzKG73JqAMphV4NO7b-bMk6mhvgz8TR0_YUewsPg2CVvgdvEmnV4DE4znhnwiLMniN0kPGzF5pindkKTVNDb8"

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
    private lateinit var customItemIcon: ImageView

    private var showTaxField = false
    private var showMRPField = false
    private var showUnitField = false
    private var showWholesaleField = false


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posactivity)
        setupBottomNavigation("pos")
        getUserProfile()
        cartBadge = findViewById(R.id.cartBadge)
        updateCartBadge()


        customItemIcon = findViewById(R.id.customItem)
        customItemIcon.visibility = View.GONE // default hidden

        customItemIcon.setOnClickListener {
            showCustomItemDialog(
                showTax = showTaxField,
                showMRP = showMRPField,
                showUnit = showUnitField,
                showWholesale = showWholesaleField
            )
        }
        viewBillsLayout = findViewById(R.id.viewBillsLayout)
        viewBillsLayout.setOnClickListener {
            val totalItems = CartManager.getDistinctItemsCount() // count of items in cart
            Log.d("POSActivity", "Opening CartActivity. Total items in cart: $totalItems")
            CartManager.cartMap.forEach { (key, qty) ->
                Log.d("POSActivity", "Cart item key: $key, qty: $qty")
            }
            if (totalItems > 0) {
                // If cart has items → open CartActivity
                val intent = Intent(this, CartActivity::class.java)
                cartActivityLauncher.launch(intent)
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
            // If subcategory ID == category ID → show category-level products
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
                CartManager.cartChangedLiveData.value = true as Long?
            },
            quantityStatus = quantityStatus // pass the value from profile API
        )
        centerRecyclerView.adapter = productAdapter


// Auto-refresh POS screen whenever cart changes (normal/custom)
        CartManager.cartChangedLiveData.observe(this) { timestamp ->
            if (timestamp != null) {
                Log.d("POSActivity", "Cart changed at timestamp: $timestamp")
                CartManager.cartMap.forEach { (key, qty) ->
                    Log.d("POSActivity", "Updated cart item key: $key, qty: $qty")
                }
                productAdapter.refreshCart()
                updateCartBar()
                updateCartBadge()
            }
        }
        val refreshButton: ImageView = findViewById(R.id.refresh)
        refreshButton.setOnClickListener {
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show()

            val currentCategoryId = selectedCategoryId
            if (!currentCategoryId.isNullOrEmpty()) {
                getPOSProducts(currentCategoryId, selectedSubCategoryId)
            } else if (categoryList.isNotEmpty()) {
                getPOSProducts(categoryList[0].categoryId?.toString() ?: "", null)
            }
            refreshProductSelection()
        }



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

                        //  Auto-select first category (if available)
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
                                var quantityInCart = CartManager.cartMap[key] ?: 0.0
                                Log.d("POSActivity", "Before sync: key=$key, quantityInCart=$quantityInCart")


                                // 🟢 FIX: CRITICAL LOGIC FOR QUANTITY STATUS CHANGE
                                if (quantityStatus == "1") {
                                    // Decimal is ON (e.g., 1.5)
                                    variation.selectedQuantityDecimal = quantityInCart
                                    variation.selectedQuantity = quantityInCart.toInt()
                                } else {
                                    // Decimal is OFF (e.g., old qty was 1.5, must become 1)
                                    val integerQty = quantityInCart.toInt().toDouble()

                                    // Only update CartManager if the quantity was fractional
                                    if (quantityInCart > 0.0 && quantityInCart != integerQty) {
                                        Log.d("POSActivity", "Forcing qty down from $quantityInCart to $integerQty for key $key")
                                        CartManager.cartMap[key] = integerQty // 💡 This forces total recalculation
                                        quantityInCart = integerQty // Update local var for local assignments
                                    }

                                    variation.selectedQuantity = quantityInCart.toInt()
                                    variation.selectedQuantityDecimal = quantityInCart
                                }

                                // Only add to allProductsMap if the product has data
                                if (quantityInCart > 0.0) {
                                    CartManager.allProductsMap[key] = variation
                                } else {
                                    CartManager.allProductsMap.remove(key)
                                }
                                Log.d(
                                    "POSActivity",
                                    "After sync: key=$key, selectedQuantity=${variation.selectedQuantity}, selectedQuantityDecimal=${variation.selectedQuantityDecimal}"
                                )

                            }

                        }
                        response.body()?.data?.let { productList.addAll(it) }
                        Log.d("POSActivity", "POS Products synced with cart:")
                        productList.forEach { product ->
                            product.productPrice?.forEach { variation ->
                                val key = "${product.productId}_${variation.productPriceId}"
                                val qty = if (quantityStatus == "1") variation.selectedQuantityDecimal else variation.selectedQuantity.toDouble()
                                Log.d("POSActivity", "Product key: $key, qty: $qty")
                            }
                        }

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
                        val userDetails = response.body()?.userDetails
                        quantityStatus = userDetails?.quantity_status ?: "0"

                        // Show/hide customItem icon
                        customItemIcon.visibility = if (userDetails?.custom_product_status == "1") View.VISIBLE else View.GONE

                        // Set profile flags
                        showTaxField = userDetails?.product_tax_status == "1"
                        showMRPField = userDetails?.mrp_price_status == "1"
                        showUnitField = userDetails?.unit_status == "1"
                        showWholesaleField = userDetails?.whole_sale_price_status == "1"

                        // Recreate productAdapter
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
    override fun onResume() {
        super.onResume()
        CartManager.customProducts.removeAll { it.quantity <= 0.0 } // 🟢 FIX: Check against 0.0
        val currentCategoryId = selectedCategoryId
        if (!currentCategoryId.isNullOrEmpty()) {
            // This call performs the API request -> syncs with CartManager -> calls notifyDataSetChanged()
            getPOSProducts(currentCategoryId, selectedSubCategoryId)
        } else if (categoryList.isNotEmpty()) {
            // If no category was selected (e.g., initial load issue), refresh based on the first one
            val firstCategory = categoryList[0]
            getPOSProducts(firstCategory.categoryId?.toString() ?: "", null)
        } else {
            // If the list is completely empty, just update the badge/bar
            productAdapter.notifyDataSetChanged()
        }
        updateCartBar()
        updateCartBadge()
    }

    private fun refreshProductSelection() {
        Log.d("POSActivity", "Refreshing POS products. Current cart state:")
        CartManager.cartMap.forEach { (key, qty) ->
            Log.d("POSActivity", "Cart item key: $key, qty: $qty")
        }
        CartManager.customProducts.forEach { custom ->
            Log.d("POSActivity", "Custom product: $custom")
        }


        // Clean normal products
        CartManager.cartMap.entries.removeIf { it.value <= 0.0 } // 🟢 FIX: Check against 0.0

        // Update normal products
        productList.forEach { product ->
            val priceList = product.productPrice ?: emptyList()
            priceList.forEach { variation ->
                val key = "${product.productId}_${variation.productPriceId}"
                var quantityInCart = CartManager.cartMap[key] ?: 0.0
                Log.d("POSActivity", "Before update: key=$key, quantityInCart=$quantityInCart")
                if (quantityStatus == "1") {
                    // Decimal is ON (e.g., 1.5)
                    variation.selectedQuantityDecimal = quantityInCart
                    variation.selectedQuantity = quantityInCart.toInt()
                } else {
                    // Decimal is OFF (e.g., old qty was 1.5, must become 1)
                    val integerQty = quantityInCart.toInt().toDouble()

                    if (quantityInCart > 0.0 && quantityInCart != integerQty) {
                        Log.d("POSActivity", "Forcing qty down from $quantityInCart to $integerQty for key $key")

                        CartManager.cartMap[key] = integerQty // 💡 This forces total recalculation
                        quantityInCart = integerQty // Update local var for local assignments
                    }

                    variation.selectedQuantity = quantityInCart.toInt()
                    variation.selectedQuantityDecimal = quantityInCart
                }

                if (quantityInCart > 0.0) {
                    CartManager.allProductsMap[key] = variation
                } else {
                    CartManager.allProductsMap.remove(key)
                }
                Log.d(
                    "POSActivity",
                    "After update: key=$key, selectedQuantity=${variation.selectedQuantity}, selectedQuantityDecimal=${variation.selectedQuantityDecimal}"
                )
            }

            // Reset selected variation if current one is zero
            val currentIndex = product.selectedVariationIndex
            if (currentIndex >= 0 && currentIndex < priceList.size) {
                // Get the correct quantity based on status
                val currentQty = if (quantityStatus == "1") priceList[currentIndex].selectedQuantityDecimal else priceList[currentIndex].selectedQuantity.toDouble()

                if (currentQty <= 0.0) {
                    val firstAvailable = priceList.indexOfFirst {
                        (if (quantityStatus == "1") it.selectedQuantityDecimal else it.selectedQuantity.toDouble()) > 0.0
                    }
                    product.selectedVariationIndex = if (firstAvailable >= 0) firstAvailable else 0
                }
            }
        }
        // Clean custom products with quantity <= 0
        CartManager.customProducts.removeAll { it.quantity <= 0.0 } // 🟢 FIX: Check against 0.0
        Log.d("POSActivity", "After refresh, CartManager.cartMap: ${CartManager.cartMap}")
        Log.d("POSActivity", "After refresh, CartManager.customProducts: ${CartManager.customProducts}")
        // Refresh UI
        // productAdapter.notifyDataSetChanged() // Let adapter.refreshCart() handle this
        updateCartBar()
        updateCartBadge()
    }



    private fun updateCartBar() {
        val totalQty = CartManager.getDistinctItemsCount()
        val totalAmount = CartManager.getTotalAmount()

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

    private fun showCustomItemDialog(
        showTax: Boolean,
        showMRP: Boolean,
        showUnit: Boolean,
        showWholesale: Boolean
    ) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_custom_box)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(true)

        // --- Get fields ---
        val priceTaxLayout = dialog.findViewById<View>(R.id.priceTaxLayout)
        val mrpPriceLayout = dialog.findViewById<View>(R.id.mrpPriceLayout)
        val unitSpinnerLayout = dialog.findViewById<View>(R.id.unitSpinnerLayout)
        val unitSpinnerTitleLayout = dialog.findViewById<View>(R.id.unitSpinnerTitleLayout)
        val wholesaleLayout = dialog.findViewById<View>(R.id.productWholeSalePriceLayout)

        // --- Show/hide fields ---
        priceTaxLayout.visibility = if (showTax) View.VISIBLE else View.GONE
        mrpPriceLayout.visibility = if (showMRP) View.VISIBLE else View.GONE
        unitSpinnerLayout.visibility = if (showUnit) View.VISIBLE else View.GONE
        unitSpinnerTitleLayout.visibility = if (showUnit) View.VISIBLE else View.GONE
        wholesaleLayout.visibility = if (showWholesale) View.VISIBLE else View.GONE


        // --- Inputs ---
        val nameInput = dialog.findViewById<TextInputEditText>(R.id.name)
        val variationInput = dialog.findViewById<TextInputEditText>(R.id.variationName)
        val qtyInput = dialog.findViewById<TextInputEditText>(R.id.qty)
        val priceInput = dialog.findViewById<TextInputEditText>(R.id.price)
        val taxInput = dialog.findViewById<TextInputEditText>(R.id.priceTax)
        val mrpInput = dialog.findViewById<TextInputEditText>(R.id.mrpPrice)
        val wholesaleInput = dialog.findViewById<TextInputEditText>(R.id.productWholeSalePrice)
        val unitSpinner = dialog.findViewById<Spinner>(R.id.unitSpinner)
        val yesBtn = dialog.findViewById<AppCompatTextView>(R.id.yes_btn)
        val noBtn = dialog.findViewById<AppCompatTextView>(R.id.no_btn)

        // Optional: Set input type for quantity based on quantityStatus
        // if (quantityStatus == "1") qtyInput.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

        if (showUnit) {
            // Call unit API and populate spinner
            fetchUnits { unitList ->
                val adapter = android.widget.ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    unitList
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                unitSpinner.adapter = adapter
            }
        }


        yesBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val variation = variationInput.text.toString().trim()
            val qtyText = qtyInput.text.toString().trim()
            val priceText = priceInput.text.toString().trim()
            val tax = if (showTax) taxInput.text.toString().trim() else "0"
            val mrp = if (showMRP) mrpInput.text.toString().trim() else "0"
            val wholesale = if (showWholesale) wholesaleInput.text.toString().trim() else "0"
            val unit = if (showUnit) unitSpinner.selectedItem.toString() else ""

            if (name.isEmpty() || qtyText.isEmpty() || priceText.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val qty = qtyText.toDoubleOrNull()
            val price = priceText.toDoubleOrNull()

            if (qty == null || qty <= 0.0) {
                Toast.makeText(this, "Quantity must be a valid number greater than 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (price == null || price <= 0.0) {
                Toast.makeText(this, "Price must be a valid number greater than 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            // --- Save custom item in CartManager ---
            val key = "custom_${System.currentTimeMillis()}" // unique key

            val customProduct = CartManager.CustomProduct(
                id = key,
                name = name,
                variation = variation,
                quantity = qty, // 🟢 FIX: Use Double directly
                price = price, // Double
                tax = tax.toDoubleOrNull() ?: 0.0,
                mrp = mrp.toDoubleOrNull() ?: 0.0,
                wholesalePrice = wholesale.toDoubleOrNull() ?: 0.0,
                unit = unit
            )
            android.util.Log.d(
                "POSActivity",
                "Custom Product Added: $customProduct"
            )
            Log.d("POSActivity", "Custom Product Added: $customProduct")
            CartManager.addCustomProduct(customProduct)

            Toast.makeText(this, "Custom item added to cart", Toast.LENGTH_SHORT).show()

            dialog.dismiss()
            updateCartBar()
            updateCartBadge()
        }

        noBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun fetchUnits(onResult: (List<String>) -> Unit) {
        val input = Input(status = "1")
        ApiClient.instance.unitApi(jwtToken, input)
            .enqueue(object : Callback<UnitOutput> {
                override fun onResponse(call: Call<UnitOutput>, response: Response<UnitOutput>) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        val unitNames = response.body()?.unitList?.map { it.unitName ?: "" } ?: emptyList()
                        onResult(unitNames)
                    } else {
                        onResult(emptyList())
                    }
                }

                override fun onFailure(call: Call<UnitOutput>, t: Throwable) {
                    onResult(emptyList())
                }
            })
    }
    private val cartActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // This block executes when CartActivity finishes
        if (result.resultCode == Activity.RESULT_OK) {

            refreshProductSelection()
        }
    }



}
