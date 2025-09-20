
package com.example.apitest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.example.apitest.dataModel.*
import com.example.apitest.network.ApiClient
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import retrofit2.Call
import retrofit2.Callback
import android.app.AlertDialog
import android.widget.ImageView
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apitest.adapter.ProductVariationAdapter
import com.example.apitest.databinding.ActivityAddProductBinding
import com.google.android.material.textfield.TextInputLayout
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
    private val localVariations = mutableListOf<StockProductData>() //save data locally


    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiNjk3MmE4MjVjMjZhNmU2ZDAxMTk3NjdiNDMzYzc4NWEzNTdmYzYxYTZhMTBjNjQ2MGViYTc4ZWQxNDM4NmQyYjM2YTFkNWJjZTIwZTc0MmIiLCJpYXQiOjE3NTgyNTU4MTEuMjA1Njg1LCJuYmYiOjE3NTgyNTU4MTEuMjA1Njg3LCJleHAiOjE3ODk3OTE4MTEuMjAyMzI1LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.bWv1-ccFjlnUbHqgVKleTigmPBsohLqMMJ6KNx-zPgEsHzPvFn19WQV6kc85aXrKZOdbiazq4eb7y9I1wdXI8n-BQQ4jpqruQqW87KNzvuoOvd0qPKW-EbUFbst1I5QAgA4m0kySj8JxVHAFSgBtzocj42JUd0XuNHMtGjwLUH4QM4Njc-tkVSmVqIHN66LGoaslfkl5tQTxjFV0Xg1Ay1fyNdp5A1pSZPv4wTr9aAfR1nhrqA5FtU8x0BJyWcpk3ojQq3gWUB3CZgr0Qq7tMpzuZwufR-HWqCX-Be4YRZ1wyANAQHEt0JEp5HLV9htpfuCE7grP_sw-cywep-FxVlyKO0tyc7ykFnhOaI6YlREAms4m_NOpdleSnYv9or7uj8DQWI2F4318SoFSPFu1UsVI9n2Ygf54TZD4FDhMGjWSue2uBCS3HPleSyO6Qf2Lk64OovnYRXc_tJzddcvu8LEC8ihvZpDpr4KMmxGIM15c-4lh0gXpcOOPZXmHG4rG73ogFxVzJdp-nAs-h2U-Kh52kXmjFm2MAgfKSv-0IdgA2IXUxcWthRLO5WYtiggR-ghkTx9hy6Seyq5ZXYSmdnbJHcVRHyZYE6h7hl2pgeAIA3_er4oT_2DpmEW38pwoM__ezWYarjyPYkIEI2enMQTJE0FbCU7fe5wcuSMY140"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Assuming you have a RecyclerView in your layout called productVariation
        variationAdapter = ProductVariationAdapter(
            localVariations,
            onEditClick = { item, position -> showEditVariationDialog(item, position) },
            onDeleteClick = { position -> deleteVariation(position) }
        )
        binding.productVariation.adapter = variationAdapter
        binding.productVariation.layoutManager = LinearLayoutManager(this)


        // Bind views
        categorySpinner = findViewById(R.id.categorySpinner)
        subCategorySpinner = findViewById(R.id.subCategorySpinner)
        subCategoryTitle = findViewById(R.id.subCategorySpinnerTitleLayout)
        subCategoryLayout = findViewById(R.id.subCategorySpinnerLayout)

        // Initially hide subcategory section
        subCategoryTitle.visibility = View.GONE
        subCategoryLayout.visibility = View.GONE

        loadCategories()


        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val stockEnabled = prefs.getBoolean("stock_enabled", false)
        binding.stockLayout.visibility = if (stockEnabled) View.VISIBLE else View.GONE
        binding.onOffButton.isOn = stockEnabled


        // Add Variation button click
        binding.addVariation.setOnClickListener {
            val isToggleOn = binding.onOffButton.isOn
            showAddVariationDialog(isToggleOn)
        }


    }
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun loadCategories() {
        val input = CategoryInput(status = "1")

        ApiClient.instance.stockCategoryApi(jwtToken, input)
            .enqueue(object : Callback<CategoryListOutput> {
                override fun onResponse(
                    call: Call<CategoryListOutput>,
                    response: Response<CategoryListOutput>
                ) {
                    if (response.isSuccessful && response.body()?.data != null) {
                        categoryList = response.body()!!.data!!

                        val categoryNames = categoryList.map { it.category_name }
                        val adapter = ArrayAdapter(
                            this@AddProductActivity,
                            android.R.layout.simple_spinner_item,
                            categoryNames
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        categorySpinner.adapter = adapter

                        categorySpinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    val selectedCategory = categoryList[position]
                                    loadSubCategories(selectedCategory.category_id)
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

        ApiClient.instance.addEditSubCategoryApi(jwtToken, input)
            ?.enqueue(object : Callback<SubCategoryOutput?> {
                override fun onResponse(
                    call: Call<SubCategoryOutput?>,
                    response: Response<SubCategoryOutput?>
                ) {
                    val subCategories = response.body()?.data
                    if (!subCategories.isNullOrEmpty()) {
                        subCategoryList = subCategories

                        val names = subCategoryList.map { it.subcategoryName ?: "Unnamed" }
                        val adapter = ArrayAdapter(
                            this@AddProductActivity,
                            android.R.layout.simple_spinner_item,
                            names
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        subCategorySpinner.adapter = adapter

                        // Show subcategory UI
                        subCategoryTitle.visibility = View.VISIBLE
                        subCategoryLayout.visibility = View.VISIBLE
                    } else {
                        // Hide subcategory UI
                        subCategoryTitle.visibility = View.GONE
                        subCategoryLayout.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<SubCategoryOutput?>, t: Throwable) {
                    Toast.makeText(this@AddProductActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun showAddVariationDialog(isStockOn: Boolean) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_variation, null)

        val productVariation = dialogView.findViewById<TextInputEditText>(R.id.productVariation)
        val productPrice = dialogView.findViewById<TextInputEditText>(R.id.productPrice)
        val productLowStock = dialogView.findViewById<TextInputEditText>(R.id.productLowStockAlert)
        val productStockQty = dialogView.findViewById<TextInputEditText>(R.id.productStockQuantity)
        val productMrp = dialogView.findViewById<TextInputEditText>(R.id.mrpPrice)

        val productWholeSale = dialogView.findViewById<TextInputEditText>(R.id.productWholeSalePrice)
        val saveBtn = dialogView.findViewById<MaterialTextView>(R.id.save)
        val cancelBtn = dialogView.findViewById<ImageView>(R.id.cancel)

        val hintLowStock = dialogView.findViewById<TextInputLayout>(R.id.hint2)
        val hintStockQty = dialogView.findViewById<TextInputLayout>(R.id.hint3)

        hintLowStock.visibility = if (isStockOn) View.VISIBLE else View.GONE
        hintStockQty.visibility = if (isStockOn) View.VISIBLE else View.GONE

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
            val wholeSale = productWholeSale.text.toString().trim()
            val mrp = productMrp.text.toString().trim()

            if (variationName.isEmpty() || price.isEmpty() ||
                (isStockOn && (lowStock.isEmpty() || stockQty.isEmpty())) ||
                wholeSale.isEmpty() || mrp.isEmpty()
            ) {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
            } else {
                val newVariation = StockProductData(
                    productVariationName = variationName,
                    product_price = price,
                    whole_sale_price = wholeSale,
                    mrp_price = mrp,
                    stockCount = if (isStockOn) stockQty.toIntOrNull() else null,
                    low_stock_alert = if (isStockOn) lowStock.toIntOrNull() else null
                )

                localVariations.add(newVariation)
                variationAdapter.notifyItemInserted(localVariations.size - 1)

                // 🔹 Make RecyclerView visible and hide "no data" message
                binding.productVariation.visibility = View.VISIBLE
                val noListError = findViewById<LinearLayout>(R.id.no_list_error) // no data design
                noListError.visibility = View.GONE

                Toast.makeText(this, "Variation added", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }


        dialog.show()
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

        val cancelBtn = dialogView.findViewById<ImageView>(R.id.cancel)
        val saveBtn = dialogView.findViewById<MaterialTextView>(R.id.save)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Prefill values and show/hide dynamically
        fun setField(editText: TextInputEditText?, value: Any?, hintLayout: TextInputLayout? = null) {
            if (value == null || value.toString().isEmpty()) {
                editText?.visibility = View.GONE
                hintLayout?.visibility = View.GONE
            } else {
                editText?.visibility = View.VISIBLE
                hintLayout?.visibility = View.VISIBLE
                editText?.setText(value.toString())
            }
        }

        setField(productVariation, item.productVariationName, dialogView.findViewById(R.id.hint))
        setField(productPrice, item.product_price, dialogView.findViewById(R.id.hint1))
        setField(productLowStock, item.low_stock_alert, hintLowStock)
        setField(productStockQty, item.stockCount, hintStockQty)
        setField(productMrp, item.mrp_price, dialogView.findViewById(R.id.hint5))
        setField(productWholeSale, item.whole_sale_price, dialogView.findViewById(R.id.hint6))

        cancelBtn.setOnClickListener { dialog.dismiss() }

        saveBtn.setOnClickListener {
            // Update item with new values
            item.productVariationName = productVariation.text.toString().trim()
            item.product_price = productPrice.text.toString().trim()
            item.stockCount = productStockQty.text.toString().trim().toIntOrNull()
            item.low_stock_alert = productLowStock.text.toString().trim().toIntOrNull()

            item.mrp_price = productMrp.text.toString().trim()
            item.whole_sale_price = productWholeSale.text.toString().trim()

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
            .setPositiveButton("Yes") { _, _ ->
                localVariations.removeAt(position)
                variationAdapter.notifyItemRemoved(position)
                variationAdapter.notifyItemRangeChanged(position, localVariations.size)

                if (localVariations.isEmpty()) {
                    binding.productVariation.visibility = View.GONE
                    findViewById<LinearLayout>(R.id.no_list_error)?.visibility = View.VISIBLE
                }

                Toast.makeText(this, "Variation deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }
}




