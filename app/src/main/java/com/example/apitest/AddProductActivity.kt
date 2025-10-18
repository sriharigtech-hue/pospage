package com.example.apitest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apitest.adapter.ProductVariationAdapter
import com.example.apitest.dataModel.*
import com.example.apitest.databinding.ActivityAddProductBinding
import com.example.apitest.network.ApiClient
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private lateinit var categorySpinner: Spinner
    private lateinit var subCategorySpinner: Spinner
    private lateinit var subCategoryTitle: TextView
    private lateinit var subCategoryLayout: LinearLayout
    private var selectedSubCategoryId: Int? = null

    private var categoryList: List<Category> = emptyList()
    private var subCategoryList: List<SubCategoryDetails> = emptyList()

    private lateinit var variationAdapter: ProductVariationAdapter
    private val localVariations = mutableListOf<StockProductData>()
    private var selectedCategoryId: Int? = null

    private var showMRP = true
    private var showWholesale = true
    private var showTax = false
    private var isEditMode = false

    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZGE0YjJmNjlmZGJkNjMwMDMyNGE3MWNkZWRhMDI2ZWI2YTIwMGM4NWIyNTI2MTNjOTZhZGIyMDA2MTE3YjMxMGI0MTFjYjczNzNmZmNlZDAiLCJpYXQiOjE3NjAzMjkzNTkuNDc3MTA1LCJuYmYiOjE3NjAzMjkzNTkuNDc3MTA4LCJleHAiOjE3OTE4NjUzNTkuNDcyNjI1LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.C3ySWdDX7BRHm4qzwWFZZofL_DEx3C2Qjy7iEUWxy9GdrL8OJS7m7Kk_Oe4HtFaT7DvPMEWE_c9kIC8RalMXflXTvPGKkfsw7yxdVxZOKSE20UNZiSbScAdvx3RxAz-XoHK4wJr7wepspLad5y5KCv4RyPXAJl8sIjFELfiCMoxt1CiYGp5_GhsOjbMeSLWSBoDwd3H4MLNvUyU2KN2zhvQaRRUh4T-L11mZgmd_8A8kWZbp_bO6AK-3hGHFGd7VaT2Xqoi4asmn0ABlxusVYWG6hw9UhnU-_uxOVFQLAHog-WKfbahCwfkssXtK07wMpk-ZGHfRn7ujbkrMAX5gNgNkcNQZPRMkUSrokHylEJXKC7UOAgUiK8fy32bIlmFuMQE9hTuuQjHWJ8hdEqtPaXVIcc1oXURtZhCWTp2APH9RE4_L41NYStog_bVMdXwRO_a6QEg_ex0moqxwtRZKivnIF4DKm6WLj45X0FLj-F7HTlZ-eoc9j3w_dVaVyhhxEKUiTyQSJ_AwVKMTbAUmxvWY3OnoIAmu4WYrbC4T4tA2cWoB9yXKna8Yfbil_vC46tLZweGF7RRZR2MPT16q-iCzKG73JqAMphV4NO7b-bMk6mhvgz8TR0_YUewsPg2CVvgdvEmnV4DE4znhnwiLMniN0kPGzF5pindkKTVNDb8"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindViews()
        loadPreferences()
        setupRecyclerView()
        setupAddVariationButton()

        binding.saveButton.setOnClickListener { saveProduct() }
    }

    private fun bindViews() {
        categorySpinner = binding.categorySpinner
        subCategorySpinner = binding.subCategorySpinner
        subCategoryTitle = binding.subCategorySpinnerTitleLayout
        subCategoryLayout = binding.subCategorySpinnerLayout
        subCategoryTitle.visibility = View.GONE
        subCategoryLayout.visibility = View.GONE
    }

    private fun loadPreferences() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val stockEnabled = prefs.getBoolean("stock_enabled", false)

        val typeMethod = intent?.getIntExtra("type_method", 1) ?: 1
        val titleView = findViewById<AppCompatTextView>(R.id.title)
        isEditMode = (typeMethod == 2)

        if (isEditMode) {
            titleView.text = "Edit Product"
            val productId = intent.getIntExtra("product_id", -1)
            if (productId != -1) {
                fetchProductDetails(productId)
            }
        } else {
            titleView.text = "Add Product"
            loadCategoriesAndSubCategories()
        }

        showMRP = prefs.getBoolean("show_mrp", true)
        showWholesale = prefs.getBoolean("show_wholesale", true)
        showTax = prefs.getBoolean("show_tax", false)

        binding.stockLayout.visibility = if (stockEnabled) View.VISIBLE else View.GONE
        binding.onOffButton.isOn = stockEnabled

        binding.hint7.visibility = if (showTax) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerView() {
        variationAdapter = ProductVariationAdapter(
            localVariations,
            onEditClick = { item, position -> showEditVariationDialog(item, position) },
            onDeleteClick = { position -> deleteVariation(position) }
        )
        binding.productVariation.adapter = variationAdapter
        binding.productVariation.layoutManager = LinearLayoutManager(this)
    }

    private fun setupAddVariationButton() {
        binding.addVariation.setOnClickListener {
            val isStockOn = binding.onOffButton.isOn
            showAddVariationDialog(isStockOn)
        }
    }

    private fun fetchProductDetails(productId: Int) {
        val input = InputField(product_id = productId, status = "1")
        ApiClient.instance.singleProductDetail(jwtToken, input)
            ?.enqueue(object : Callback<AddProductOutput?> {
                override fun onResponse(call: Call<AddProductOutput?>, response: Response<AddProductOutput?>) {
                    val product = response.body()?.categoryList?.firstOrNull { it.product_id == productId }
                    product?.let {
                        populateProductFields(it)
                        loadCategoriesAndSubCategories(it.category_id, it.sub_category_id)
                    }
                }

                override fun onFailure(call: Call<AddProductOutput?>, t: Throwable) {
                    Toast.makeText(this@AddProductActivity, "Failed to load product", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun populateProductFields(product: AddProductInput) {
        binding.productName.setText(product.product_name)
        binding.onOffButton.isOn = product.stock_status == "1"
        binding.productTax.setText(product.product_tax ?: "")

        // Load variations
        localVariations.clear()
        product.product_price?.forEach { price ->
            localVariations.add(
                StockProductData(
                    productVariationId = price.product_variation_id,
                    productVariationName = price.product_variation ?: "",
                    product_price = price.product_price ?: "",
                    stockCount = price.stock_quantity?.toIntOrNull(),
                    low_stock_alert = price.low_stock_alert?.toIntOrNull(),
                    mrp_price = price.mrp_price,
                    whole_sale_price = price.whole_sale_price,
                    unit = price.unit_name
                )
            )
        }

        variationAdapter.notifyDataSetChanged()
        binding.productVariation.visibility = if (localVariations.isEmpty()) View.GONE else View.VISIBLE
        findViewById<LinearLayout>(R.id.no_list_error).visibility =
            if (localVariations.isEmpty()) View.VISIBLE else View.GONE
    }




    private fun loadCategoriesAndSubCategories(
        preselectCategoryId: Int? = null,
        preselectSubCategoryId: Int? = null
    ) {
        ApiClient.instance.stockCategoryApi(jwtToken, CategoryInput(status = "1"))
            .enqueue(object : Callback<CategoryListOutput> {
                override fun onResponse(call: Call<CategoryListOutput>, response: Response<CategoryListOutput>) {
                    categoryList = response.body()?.data ?: emptyList()

                    if (categoryList.isEmpty()) {
                        Toast.makeText(this@AddProductActivity, "No categories available", Toast.LENGTH_SHORT).show()
                        selectedCategoryId = null
                        categorySpinner.adapter = null
                        subCategoryTitle.visibility = View.GONE
                        subCategoryLayout.visibility = View.GONE
                        return
                    }

                    val adapter = ArrayAdapter(
                        this@AddProductActivity,
                        android.R.layout.simple_spinner_item,
                        categoryList.map { it.category_name ?: "Unnamed" }
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    categorySpinner.adapter = adapter

                    val categoryIndex = categoryList.indexOfFirst { it.category_id == preselectCategoryId }
                        .takeIf { it >= 0 } ?: 0
                    categorySpinner.setSelection(categoryIndex)
                    selectedCategoryId = categoryList[categoryIndex].category_id

                    // Load subcategories for the selected category
                    loadSubCategories(selectedCategoryId!!, preselectSubCategoryId)

                    categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                            val newCatId = categoryList[position].category_id
                            if (selectedCategoryId != newCatId) {
                                selectedCategoryId = newCatId
                                selectedSubCategoryId = null
                                loadSubCategories(newCatId)
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }
                }

                override fun onFailure(call: Call<CategoryListOutput>, t: Throwable) {
                    Toast.makeText(this@AddProductActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
                    categoryList = emptyList()
                    selectedCategoryId = null
                    categorySpinner.adapter = null
                    subCategoryTitle.visibility = View.GONE
                    subCategoryLayout.visibility = View.GONE
                }
            })
    }

    private fun loadSubCategories(categoryId: Int, preselectSubCategoryId: Int? = null) {
        val input = Input(status = "1", category_id = categoryId.toString())
        ApiClient.instance.addEditSubCategoryApi(jwtToken, input)
            ?.enqueue(object : Callback<SubCategoryOutput?> {
                override fun onResponse(call: Call<SubCategoryOutput?>, response: Response<SubCategoryOutput?>) {
                    val filteredSubCategories = response.body()?.data?.filter { it.categoryId == categoryId } ?: emptyList()

                    if (filteredSubCategories.isEmpty()) {
                        subCategoryTitle.visibility = View.GONE
                        subCategoryLayout.visibility = View.GONE
                        selectedSubCategoryId = null
                        subCategorySpinner.adapter = null
                        return
                    }

                    val subCategoryNames = filteredSubCategories.map { it.subcategoryName ?: "Unnamed" }
                    val adapter = ArrayAdapter(this@AddProductActivity, android.R.layout.simple_spinner_item, subCategoryNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    subCategorySpinner.adapter = adapter

                    subCategoryTitle.visibility = View.VISIBLE
                    subCategoryLayout.visibility = View.VISIBLE

                    val subIndex = filteredSubCategories.indexOfFirst { it.subcategoryId == preselectSubCategoryId }
                        .takeIf { it >= 0 } ?: 0
                    subCategorySpinner.setSelection(subIndex)
                    selectedSubCategoryId = filteredSubCategories[subIndex].subcategoryId

                    subCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                            selectedSubCategoryId = filteredSubCategories[position].subcategoryId
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {
                            selectedSubCategoryId = 0
                        }
                    }

                }

                override fun onFailure(call: Call<SubCategoryOutput?>, t: Throwable) {
                    subCategoryTitle.visibility = View.GONE
                    subCategoryLayout.visibility = View.GONE
                    selectedSubCategoryId = null
                    subCategorySpinner.adapter = null
                }
            })
    }

















    private fun setFieldVisibility(editText: TextInputEditText?, hintLayout: TextInputLayout?, show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE
        editText?.visibility = visibility
        hintLayout?.visibility = visibility
    }

    private fun showAddVariationDialog(isStockOn: Boolean) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_variation, null)
        val productVariation = dialogView.findViewById<TextInputEditText>(R.id.productVariation)
        val productPrice = dialogView.findViewById<TextInputEditText>(R.id.productPrice)
        val productLowStock = dialogView.findViewById<TextInputEditText>(R.id.productLowStockAlert)
        val productStockQty = dialogView.findViewById<TextInputEditText>(R.id.productStockQuantity)
        val productMrp = dialogView.findViewById<TextInputEditText>(R.id.mrpPrice)
        val productWholeSale = dialogView.findViewById<TextInputEditText>(R.id.productWholeSalePrice)
        val unitSpinner = dialogView.findViewById<Spinner>(R.id.unitSpinner)


        val hintLowStock = dialogView.findViewById<TextInputLayout>(R.id.hint2)
        val hintStockQty = dialogView.findViewById<TextInputLayout>(R.id.hint3)
        val hintMrp = dialogView.findViewById<TextInputLayout>(R.id.hint5)
        val hintWholeSale = dialogView.findViewById<TextInputLayout>(R.id.hint6)

        val saveBtn = dialogView.findViewById<MaterialTextView>(R.id.save)
        val cancelBtn = dialogView.findViewById<ImageView>(R.id.cancel)

        setFieldVisibility(productMrp, hintMrp, showMRP)
        setFieldVisibility(productWholeSale, hintWholeSale, showWholesale)
        setFieldVisibility(productLowStock, hintLowStock, isStockOn)
        setFieldVisibility(productStockQty, hintStockQty, isStockOn)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        cancelBtn.setOnClickListener { dialog.dismiss() }

        ApiClient.instance.unitApi(jwtToken, Input(status = "1"))
            .enqueue(object : Callback<UnitOutput> {
                override fun onResponse(call: Call<UnitOutput>, response: Response<UnitOutput>) {
                    val units = response.body()?.unitList ?: emptyList()
                    if (units.isNotEmpty()) {
                        val adapter = ArrayAdapter(
                            this@AddProductActivity,
                            android.R.layout.simple_spinner_item,
                            units.map { it.unitName ?: "Unnamed" }
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        unitSpinner.adapter = adapter

                        // Optional: preselect first unit
                        unitSpinner.setSelection(0)
                    }
                }

                override fun onFailure(call: Call<UnitOutput>, t: Throwable) {
                    Toast.makeText(this@AddProductActivity, "Failed to load units: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })


        saveBtn.setOnClickListener {
            val variationName = productVariation.text.toString().trim()
            val price = productPrice.text.toString().trim()
            val lowStock = productLowStock.text.toString().trim()
            val stockQty = productStockQty.text.toString().trim()
            val mrp = productMrp.text.toString().trim()
            val wholeSale = productWholeSale.text.toString().trim()

            if (variationName.isEmpty() || price.isEmpty() ||
                (isStockOn && (lowStock.isEmpty() || stockQty.isEmpty())) ||
                (showMRP && mrp.isEmpty()) ||
                (showWholesale && wholeSale.isEmpty())
            ) {
                Toast.makeText(this, "Please enter all required details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newVariation = StockProductData(
                productVariationId = null, // new item
                productVariationName = variationName,
                product_price = price,
                stockCount = if (isStockOn) stockQty.toIntOrNull() else null,
                low_stock_alert = if (isStockOn) lowStock.toIntOrNull() else null,
                mrp_price = if (showMRP) mrp else null,
                whole_sale_price = if (showWholesale) wholeSale else null,
                unit = unitSpinner.selectedItem.toString()
            )
            localVariations.add(newVariation)
            variationAdapter.notifyItemInserted(localVariations.size - 1)

            binding.productVariation.visibility = View.VISIBLE
            findViewById<LinearLayout>(R.id.no_list_error)?.visibility = View.GONE
            Toast.makeText(this, "Variation added", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }


    // Fetch user_id automatically from profile API
    private fun fetchUserId(callback: (Int?) -> Unit) {
        ApiClient.instance.getUserDetails(jwtToken, Input(status = "1"))
            ?.enqueue(object : Callback<ProfileOutput?> {
                override fun onResponse(call: Call<ProfileOutput?>, response: Response<ProfileOutput?>) {
                    callback(response.body()?.userDetails?.userId)
                }

                override fun onFailure(call: Call<ProfileOutput?>, t: Throwable) {
                    callback(null)
                }
            })
    }

    private fun saveProduct() {
        val itemName = binding.productName.text.toString().trim()
        if (itemName.isEmpty()) {
            binding.productName.error = "Please enter item name"
            return
        }
        if (localVariations.isEmpty()) {
            Toast.makeText(this, "Please add at least one variation", Toast.LENGTH_SHORT).show()
            return
        }

        val tax = if (showTax) binding.productTax.text.toString().trim().takeIf { it.isNotEmpty() } else null

        val categoryId = categoryList.getOrNull(categorySpinner.selectedItemPosition)?.category_id
        if (categoryId == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val subCategoryId: Int = if (subCategoryLayout.visibility == View.VISIBLE) {
            selectedSubCategoryId ?: 0
        } else 0



        fetchUserId { userId ->
            if (userId == null) {
                Toast.makeText(this, "Invalid user data", Toast.LENGTH_SHORT).show()
                return@fetchUserId
            }

            val variationsForApi = localVariations.map { variation ->
                AddProductPrice(
                    product_variation_id = variation.productVariationId ?: 0,
                    product_variation = variation.productVariationName,
                    product_price = variation.product_price,
                    stock_quantity = variation.stockCount?.toString(),
                    low_stock_alert = variation.low_stock_alert?.toString(),
                    mrp_price = variation.mrp_price,
                    whole_sale_price = variation.whole_sale_price,
                    product_tax = tax,
                    unit_id = "",  // if needed, map actual unit id
                    unit_name = variation.unit,
                    sku = ""
                )
            }

            val productInput = AddProductInput(
                product_id = if (isEditMode) intent.getIntExtra("product_id", -1) else 0, // use 0 instead of null
                product_name = itemName,
                category_id = categoryId ?: 0, // ensure integer
                sub_category_id = subCategoryId, // already fixed above
                product_status = 1,
                stock_status = if (binding.onOffButton.isOn) "1" else "0",
                product_price = variationsForApi,
                product_tax = tax ?: "0",
                user_id = userId ?: 0,
                status = "1"
            )



            Log.d("AddProduct", "Request body: $productInput")

            val apiCall = if (isEditMode) {
                ApiClient.instance.editProduct(jwtToken, productInput)
            } else {
                ApiClient.instance.addProduct(jwtToken, productInput)
            }

            apiCall?.enqueue(object : Callback<StatusResponse?> {
                override fun onResponse(call: Call<StatusResponse?>, response: Response<StatusResponse?>) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        Toast.makeText(this@AddProductActivity, if (isEditMode) "Product updated" else "Product added", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this@AddProductActivity, "Failed to save product", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<StatusResponse?>, t: Throwable) {
                    Toast.makeText(this@AddProductActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }







    private fun showEditVariationDialog(item: StockProductData, position: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_variation, null)

        val productVariation = dialogView.findViewById<TextInputEditText>(R.id.productVariation)
        val productPrice = dialogView.findViewById<TextInputEditText>(R.id.productPrice)
        val productLowStock = dialogView.findViewById<TextInputEditText>(R.id.productLowStockAlert)
        val productStockQty = dialogView.findViewById<TextInputEditText>(R.id.productStockQuantity)
        val productMrp = dialogView.findViewById<TextInputEditText>(R.id.mrpPrice)
        val productWholeSale = dialogView.findViewById<TextInputEditText>(R.id.productWholeSalePrice)
        val unitSpinner = dialogView.findViewById<Spinner>(R.id.unitSpinner)


        val hintLowStock = dialogView.findViewById<TextInputLayout>(R.id.hint2)
        val hintStockQty = dialogView.findViewById<TextInputLayout>(R.id.hint3)
        val hintMrp = dialogView.findViewById<TextInputLayout>(R.id.hint5)
        val hintWholeSale = dialogView.findViewById<TextInputLayout>(R.id.hint6)

        val saveBtn = dialogView.findViewById<MaterialTextView>(R.id.save)
        val cancelBtn = dialogView.findViewById<ImageView>(R.id.cancel)

        val isStockOn = binding.onOffButton.isOn

        // Prefill existing data
        productVariation.setText(item.productVariationName)
        productPrice.setText(item.product_price)
        productLowStock.setText(item.low_stock_alert?.toString())
        productStockQty.setText(item.stockCount?.toString())
        productMrp.setText(item.mrp_price)
        productWholeSale.setText(item.whole_sale_price)

        // Show/hide fields based on settings
        setFieldVisibility(productMrp, hintMrp, showMRP)
        setFieldVisibility(productWholeSale, hintWholeSale, showWholesale)
        setFieldVisibility(productLowStock, hintLowStock, isStockOn)
        setFieldVisibility(productStockQty, hintStockQty, isStockOn)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        cancelBtn.setOnClickListener { dialog.dismiss() }

        ApiClient.instance.unitApi(jwtToken, Input(status = "1"))
            .enqueue(object : Callback<UnitOutput> {
                override fun onResponse(call: Call<UnitOutput>, response: Response<UnitOutput>) {
                    val units = response.body()?.unitList ?: emptyList()
                    if (units.isNotEmpty()) {
                        val adapter = ArrayAdapter(
                            this@AddProductActivity,
                            android.R.layout.simple_spinner_item,
                            units.map { it.unitName ?: "Unnamed" }
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        unitSpinner.adapter = adapter

                        // Preselect the unit saved in this variation
                        val preselectIndex = units.indexOfFirst { it.unitName == item.unit }.takeIf { it >= 0 } ?: 0
                        unitSpinner.setSelection(preselectIndex)
                    }
                }

                override fun onFailure(call: Call<UnitOutput>, t: Throwable) {
                    Toast.makeText(this@AddProductActivity, "Failed to load units: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })

        saveBtn.setOnClickListener {
            val variationName = productVariation.text.toString().trim()
            val price = productPrice.text.toString().trim()
            val lowStock = productLowStock.text.toString().trim()
            val stockQty = productStockQty.text.toString().trim()
            val mrp = productMrp.text.toString().trim()
            val wholeSale = productWholeSale.text.toString().trim()

            if (variationName.isEmpty() || price.isEmpty() ||
                (isStockOn && (lowStock.isEmpty() || stockQty.isEmpty())) ||
                (showMRP && mrp.isEmpty()) ||
                (showWholesale && wholeSale.isEmpty())
            ) {
                Toast.makeText(this, "Please enter all required details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update the variation
            localVariations[position] = StockProductData(
                productVariationId = item.productVariationId, // keep existing ID
                productVariationName = variationName,
                product_price = price,
                stockCount = if (isStockOn) stockQty.toIntOrNull() else null,
                low_stock_alert = if (isStockOn) lowStock.toIntOrNull() else null,
                mrp_price = if (showMRP) mrp else null,
                whole_sale_price = if (showWholesale) wholeSale else null,
                unit = unitSpinner.selectedItem.toString()
            )
            variationAdapter.notifyItemChanged(position)

            Toast.makeText(this, "Variation updated", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun deleteVariation(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Variation")
            .setMessage("Are you sure you want to delete this variation?")
            .setPositiveButton("Yes") { dialog, _ ->
                // Remove the item
                localVariations.removeAt(position)
                variationAdapter.notifyItemRemoved(position)
                variationAdapter.notifyItemRangeChanged(position, localVariations.size)

                // Correctly reference the no data layout
                val noListError = findViewById<LinearLayout>(R.id.no_list_error)
                noListError.visibility = if (localVariations.isEmpty()) View.VISIBLE else View.GONE

                Toast.makeText(this, "Variation deleted", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }



}
