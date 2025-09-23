package com.example.apitest

import android.R.id.input
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apitest.adapter.ProductVariationAdapter
import com.example.apitest.dataModel.*
import com.example.apitest.network.ApiClient
import com.example.apitest.databinding.ActivityAddProductBinding
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

    private var categoryList: List<Category> = emptyList()
    private var subCategoryList: List<SubCategoryDetails> = emptyList()

    private lateinit var variationAdapter: ProductVariationAdapter
    private val localVariations = mutableListOf<StockProductData>()

    private var showMRP = true
    private var showWholesale = true
    private var showTax = false

    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiNmZiMTgzMGFlYzFkNzUzOTdiNDBjMmE3NWJhOWRmZWU4MTU3YWMxZmZlMTg2NGYxZWMwOTYyYzA1NjE1YThjNTlkZjg3MjVjNDlmNmJmZTYiLCJpYXQiOjE3NTg2MTM2MzguMzQyMTUzLCJuYmYiOjE3NTg2MTM2MzguMzQyMTU3LCJleHAiOjE3OTAxNDk2MzguMzM2ODI2LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.QiPgcG55zwKdr_U471IaYnBQkUk437w5vRrmjxWos1zRADMFKLuovtJ8IqoTzbkQ1GMWE0fr6c22cdBq9Eq4PlY71A8ocqua6rhGKTS5h7ziYjA8y_KNRSmvWeSfrYF59FCv_sl_Yi8mci_Gl6lzLV5yzf-gQOcmxQ0m4NifHTxCZZEOXloSS0V2KtiECV1MwATuDGLGv7QYAwte7XEIZiCFjTJUjGGKdcXHGdCPXnsnlSSzyYUCuljabM4Of3dnP6QV6YVEuRkAiSn7HvyqgNpi34ux4lsVXFMWy1qnbI-VI_fo2Vcf5uZa8B4KBYVNT7YkV2_KcxEsdZ-ZhRzGbm6erYvUuwWUTLj8DHRUHfH-s1sOO4j3u8SQeVV47OfXB6Wo-CghPzuMQBtTvoQe2_zgAV9QrCS-xsk-uJxHLxp_dao9igrzB6fskUGshJ70IKLatiF3IDXuyVLZNqOJUblUYpoyvKuj4dBa-BZUnS35m9jm5Rz5XCkeIMIm26VTvwiNcuTg0cAXhpe97yZ_Ir0FV7-8YNQCPTAtCrC4IA0r6cuLTkWjZWa-gokQKbMWble8R9VoP-OIm0zFbISJUBtI-B0cif3oxTwr7m12jubPutnFl_HZyREBOticvsTf6q48cKUxNSYeyOW3aMdbZ584yRv95UYKUou0k0LbezM"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindViews()
        loadPreferences()
        setupRecyclerView()
        setupAddVariationButton()
        loadCategories()

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

    private fun loadCategories() {
        val input = CategoryInput(status = "1")
        ApiClient.instance.stockCategoryApi(jwtToken, input)
            .enqueue(object : Callback<CategoryListOutput> {
                override fun onResponse(call: Call<CategoryListOutput>, response: Response<CategoryListOutput>) {
                    if (response.isSuccessful && response.body()?.data != null) {
                        categoryList = response.body()!!.data!!
                        val adapter = ArrayAdapter(
                            this@AddProductActivity,
                            android.R.layout.simple_spinner_item,
                            categoryList.map { it.category_name }
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        categorySpinner.adapter = adapter
                        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                                loadSubCategories(categoryList[position].category_id)
                            }
                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }
                    }
                }

                override fun onFailure(call: Call<CategoryListOutput>, t: Throwable) {
                    Toast.makeText(this@AddProductActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadSubCategories(categoryId: Int) {
        val input = Input(category_id = categoryId.toString(), status = "1")
        ApiClient.instance.addEditSubCategoryApi(jwtToken, input)?.enqueue(object : Callback<SubCategoryOutput?> {
            override fun onResponse(call: Call<SubCategoryOutput?>, response: Response<SubCategoryOutput?>) {
                val subCategories = response.body()?.data
                if (!subCategories.isNullOrEmpty()) {
                    subCategoryList = subCategories
                    val adapter = ArrayAdapter(
                        this@AddProductActivity,
                        android.R.layout.simple_spinner_item,
                        subCategoryList.map { it.subcategoryName ?: "Unnamed" }
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    subCategorySpinner.adapter = adapter
                    subCategoryTitle.visibility = View.VISIBLE
                    subCategoryLayout.visibility = View.VISIBLE
                } else {
                    subCategoryTitle.visibility = View.GONE
                    subCategoryLayout.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<SubCategoryOutput?>, t: Throwable) {
                Toast.makeText(this@AddProductActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
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
                productVariationName = variationName,
                product_price = price,
                stockCount = if (isStockOn) stockQty.toIntOrNull() else null,
                low_stock_alert = if (isStockOn) lowStock.toIntOrNull() else null,
                mrp_price = if (showMRP) mrp else null,
                whole_sale_price = if (showWholesale) wholeSale else null
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

        val input = Input(status = "1")  // Pass a valid body instead of null
        ApiClient.instance.getUserDetails(jwtToken, input)
            ?.enqueue(object : Callback<ProfileOutput?> {
                override fun onResponse(
                    call: Call<ProfileOutput?>,
                    response: Response<ProfileOutput?>
                ) {
                    Log.d("AddProduct", "Response: ${response.body()}")
                    if (response.isSuccessful) {

                        val userId = response.body()?.userDetails?.userId
                        Log.d("AddProduct", "User ID: $userId")
                        callback(userId)
                    } else {
                        Log.e("AddProduct", "Failed to fetch user profile: ${response.errorBody()?.string()}")
                        Toast.makeText(
                            this@AddProductActivity,
                            "Failed to fetch user profile",
                            Toast.LENGTH_SHORT
                        ).show()
                        callback(null)
                    }
                }

                override fun onFailure(call: Call<ProfileOutput?>, t: Throwable) {
                    Toast.makeText(
                        this@AddProductActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
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
        if (showTax && tax.isNullOrEmpty()) {
            binding.productTax.error = "Please enter tax"
            return
        }

        val categoryId = categoryList.getOrNull(categorySpinner.selectedItemPosition)?.category_id
        val subCategoryId: Int? = if (subCategoryLayout.visibility == View.VISIBLE &&
            subCategoryList.isNotEmpty()
        ) {
            subCategoryList.getOrNull(subCategorySpinner.selectedItemPosition)?.subcategoryId
        } else {
            null // No subcategory selected
        }



        if (categoryId == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch user ID first
        fetchUserId { userId ->
            if (userId == null) {
                Toast.makeText(this, "Invalid user data", Toast.LENGTH_SHORT).show()
                return@fetchUserId
            }

            val variationsForApi = localVariations.map { variation ->
                AddProductPrice(
                    product_variation = variation.productVariationName,
                    product_price = variation.product_price,
                    stock_quantity = variation.stockCount?.toString(),
                    low_stock_alert = variation.low_stock_alert?.toString(),
                    mrp_price = variation.mrp_price,
                    whole_sale_price = variation.whole_sale_price,
                    product_tax = tax,
                    unit_id = null,
                    unit_name = null,

                    )
            }

            val addProductInput = AddProductInput(
                product_name = itemName,
                category_id = categoryId,
                product_status = 1,
                stock_status = if (binding.onOffButton.isOn) "1" else "0",
                product_price = variationsForApi,
                product_tax = tax,
                user_id = userId,
                status = "1",
                sub_categoryid = subCategoryId // will be null if spinner is hidden
            )



            Log.d("AddProduct", "Request body: $addProductInput")

            ApiClient.instance.addProduct(jwtToken, addProductInput)
                ?.enqueue(object : Callback<StatusResponse?> {

                    override fun onResponse(call: Call<StatusResponse?>, response: Response<StatusResponse?>) {
                        Log.d("AddProduct", "Response: ${response.body()}")
                        if (response.isSuccessful && response.body()?.status == true) {
                            Toast.makeText(this@AddProductActivity, "Product added successfully", Toast.LENGTH_SHORT).show()
                            val categoryId = categoryList.getOrNull(categorySpinner.selectedItemPosition)?.category_id
                            val subCategoryId = subCategoryList.getOrNull(subCategorySpinner.selectedItemPosition)?.subcategoryId

                              // 🔹 Return result back to ItemsFragment
                            val resultIntent = Intent().apply {
                                putExtra("item_name", itemName)
                                putExtra("tax", tax)
                                putExtra("category_id", categoryId)
                                putExtra("sub_category_id", subCategoryId)
                                putExtra("sub_category_status", if (subCategoryList.isNullOrEmpty()) 0 else 1)
                                putParcelableArrayListExtra("new_products", ArrayList(localVariations))
                            }
                            setResult(RESULT_OK, resultIntent)

                            finish()
                        } else {
                            Log.e("AddProduct", "Failed to add product: ${response.errorBody()?.string()}")
                            Toast.makeText(this@AddProductActivity, "Failed to add product", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<StatusResponse?>, t: Throwable) {
                        Log.e("AddProduct", "Network error: ${t.message}")
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
                productVariationName = variationName,
                product_price = price,
                stockCount = if (isStockOn) stockQty.toIntOrNull() else null,
                low_stock_alert = if (isStockOn) lowStock.toIntOrNull() else null,
                mrp_price = if (showMRP) mrp else null,
                whole_sale_price = if (showWholesale) wholeSale else null
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
