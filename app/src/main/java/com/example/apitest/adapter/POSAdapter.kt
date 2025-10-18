package com.example.apitest.adapter

import android.app.Dialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max
import com.bumptech.glide.Glide
import com.example.apitest.R
import com.example.apitest.dataModel.NewProductList
import com.example.apitest.dataModel.NewProductPrice
import com.example.apitest.helperClass.CartManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.makeramen.roundedimageview.RoundedImageView

class POSAdapter(
    private val products: List<NewProductList>,
    private val onCartChange: (List<NewProductList>) -> Unit,
    private val quantityStatus: String? = "0"
) : RecyclerView.Adapter<POSAdapter.POSViewHolder>() {

    // Stores last selected variation index per product
    private val variationMap = mutableMapOf<String, Int>()

    inner class POSViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: AppCompatTextView = itemView.findViewById(R.id.product_name)
        val productImage: RoundedImageView = itemView.findViewById(R.id.product_img)
        val addToBag: AppCompatTextView = itemView.findViewById(R.id.add_to_bag)
        val spinnerLayout: LinearLayout = itemView.findViewById(R.id.spinnerLayout)
        val variationSpinner: Spinner = itemView.findViewById(R.id.amount_spinner)
        val productNumber: AppCompatTextView = itemView.findViewById(R.id.product_number)
        val amountSymbol: AppCompatTextView = itemView.findViewById(R.id.amount_symbol)
        val productAmount: AppCompatTextView = itemView.findViewById(R.id.product_amount)
        val container: View = itemView.findViewById(R.id.container)

        val quantityLayout: LinearLayout = itemView.findViewById(R.id.quantity_layout)
        val btnIncrease: AppCompatTextView = itemView.findViewById(R.id.btn_increase)
        val btnDecrease: AppCompatTextView = itemView.findViewById(R.id.btn_decrease)
        val txtQuantity: AppCompatTextView = itemView.findViewById(R.id.txt_quantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): POSViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pos_product, parent, false)
        return POSViewHolder(view)
    }

    override fun onBindViewHolder(holder: POSViewHolder, position: Int) {
        val product = products[position]

        holder.productName.text = product.productName ?: ""
        holder.productNumber.text = product.seq_no ?: ""
        holder.amountSymbol.text = "₹"

        Glide.with(holder.itemView.context)
            .load(product.productImage.takeIf { !it.isNullOrEmpty() } ?: "https://via.placeholder.com/150")
            .into(holder.productImage)

        val priceList = product.productPrice ?: emptyList()
        if (priceList.isEmpty()) {
            holder.container.visibility = View.GONE
            return
        } else holder.container.visibility = View.VISIBLE

// 🔹 Sync quantities from CartManager for all variations
        priceList.forEach { variation ->
            syncQuantitiesFromCart(product.productId, variation, quantityStatus)
        }

        // Restore last selected variation index
        val lastSelectedIndex = variationMap[product.productId.toString()] ?: 0
        product.selectedVariationIndex = lastSelectedIndex

        if (priceList.size > 1) {
            holder.spinnerLayout.visibility = View.VISIBLE
            val variationNames = priceList.map { it.productVariation ?: "Default" }
            val spinnerAdapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_item, variationNames)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            holder.variationSpinner.adapter = spinnerAdapter


            // Temporarily remove listener to prevent onItemSelected being triggered by setSelection
            holder.variationSpinner.onItemSelectedListener = null
            holder.variationSpinner.setSelection(lastSelectedIndex, false)


            val restoredPrice = priceList[lastSelectedIndex]
            updatePriceViews(restoredPrice, holder)
            updateQuantityVisibility(restoredPrice, holder)

            // Re-attach listener
            holder.variationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    if (product.selectedVariationIndex != pos) { // only act if changed
                        product.selectedVariationIndex = pos
                        variationMap[product.productId.toString()] = pos

                        val selectedPrice = priceList[pos]
                        syncQuantitiesFromCart(product.productId, selectedPrice, quantityStatus)
//
//                        val key = "${product.productId}_${selectedPrice.productPriceId}"
//                        selectedPrice.selectedQuantity = CartManager.cartMap[key] ?: 0
//                        CartManager.allProductsMap[key] = selectedPrice

                        updatePriceViews(selectedPrice, holder)
                        updateQuantityVisibility(selectedPrice, holder)
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        } else {
            holder.spinnerLayout.visibility = View.GONE
        }


        val currentPrice = priceList[product.selectedVariationIndex]
        updatePriceViews(currentPrice, holder)
        updateQuantityVisibility(currentPrice, holder)

        // Add to bag
        holder.addToBag.setOnClickListener {
            // No need to set productName here, should be done in POSActivity
            val selectedPrice = getSelectedPrice(product, holder)
            Log.d("POSAdapter", "Adding to cart: $product")
            val stock = selectedPrice.stockCount?.toDouble() ?: 0.0

            if (stock <= 0.0) {
                Toast.makeText(holder.itemView.context, "Out of stock", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentQty = getCurrentQuantity(selectedPrice)
            if (currentQty < stock) {
                val newQty = if (quantityStatus == "1") 1.0 else 1.0 // Initial quantity is 1 or 1.0
                saveQuantity(product, selectedPrice, newQty)
                updateQuantityVisibility(selectedPrice, holder)
                onCartChange(products)
            } else {
                Toast.makeText(holder.itemView.context, "Stock limit reached", Toast.LENGTH_SHORT).show()
            }
        }


        holder.btnIncrease.setOnClickListener {
            val selectedPrice = getSelectedPrice(product, holder)
            incrementQuantity(product, selectedPrice, holder)
        }

        holder.btnDecrease.setOnClickListener {
            val selectedPrice = getSelectedPrice(product, holder)
            decrementQuantity(product, selectedPrice, holder)
        }

        holder.txtQuantity.setOnClickListener {
            val selectedPrice = getSelectedPrice(product, holder)
            showQuantityDialog(holder, product, selectedPrice)
        }

    }
    // 🟢 FIX 4: Central helper to read the correct quantity
    private fun getCurrentQuantity(price: NewProductPrice): Double {
        return if (quantityStatus == "1") {
            price.selectedQuantityDecimal
        } else {
            price.selectedQuantity.toDouble()
        }
    }
    // 🟢 FIX 5: Central helper to save the quantity
    private fun saveQuantity(product: NewProductList, price: NewProductPrice, newQty: Double) {
        val key = "${product.productId}_${price.productPriceId}"

        if (newQty <= 0.0) {
            price.selectedQuantity = 0
            price.selectedQuantityDecimal = 0.0
            CartManager.cartMap.remove(key)
            CartManager.allProductsMap.remove(key)
        } else {
            if (quantityStatus == "1") {
                // Save the precise decimal value locally
                price.selectedQuantityDecimal = newQty
                // Still save an Int value (truncated) for API compatibility if needed elsewhere
                price.selectedQuantity = newQty.toInt()
            } else {
                // Save the whole number locally (Int)
                price.selectedQuantity = newQty.toInt()
                // Keep decimal in sync for display consistency
                price.selectedQuantityDecimal = newQty.toDouble()
            }
            // Always save the Double value to the global cartMap for consistent calculation
            CartManager.cartMap[key] = newQty
            CartManager.allProductsMap[key] = price
        }
    }

    private fun incrementQuantity(product: NewProductList, price: NewProductPrice, holder: POSViewHolder) {
        val stock = price.stockCount?.toDouble() ?: 0.0
        val currentQty = getCurrentQuantity(price)

        if (currentQty < stock) {
            val increment = if (quantityStatus == "1") 1.0 else 1.0 // assuming 1.0 unit increment
            val newQty = currentQty + increment
            saveQuantity(product, price, newQty)
            updateQuantityVisibility(price, holder)
            onCartChange(products)
        } else {
            Toast.makeText(holder.itemView.context, "Stock limit reached", Toast.LENGTH_SHORT).show()
        }
    }

    private fun decrementQuantity(product: NewProductList, price: NewProductPrice, holder: POSViewHolder) {
        val currentQty = getCurrentQuantity(price)
        val decrement = if (quantityStatus == "1") 1.0 else 1.0 // assuming 1.0 unit decrement

        val newQty = max(0.0, currentQty - decrement)

        saveQuantity(product, price, newQty)

        // If removed (newQty is 0.0), reset variation logic (as was already implemented)
        if (newQty <= 0.0) {
            val firstAvailable = product.productPrice?.indexOfFirst { getCurrentQuantity(it) > 0.0 } ?: 0
            product.selectedVariationIndex = if (firstAvailable >= 0) firstAvailable else 0
        }

        updateQuantityVisibility(price, holder)
        onCartChange(products)
    }



    private fun showQuantityDialog(holder: POSViewHolder, product: NewProductList, price: NewProductPrice) {
        val context = holder.itemView.context
        val dialog = Dialog(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_qty, null)
        dialog.setContentView(dialogView)

        val stockTextView = dialogView.findViewById<MaterialTextView>(R.id.stock)
        val valueEditText = dialogView.findViewById<TextInputEditText>(R.id.value)
        val submitButton = dialogView.findViewById<MaterialTextView>(R.id.enter)
        val cancelBtn = dialogView.findViewById<View>(R.id.cancel)

        val stock = price.stockCount?.toDouble() ?: 0.0
        val currentQty = getCurrentQuantity(price)

        // Show stock
        stockTextView.visibility = View.VISIBLE
        stockTextView.text = "Available: ${price.stockCount ?: 0}"

        // Set initial quantity in dialog with correct format
        val initialQuantityText = if (quantityStatus == "1") {
            "%.1f".format(currentQty)
        } else {
            currentQty.toInt().toString()
        }
        valueEditText.setText(initialQuantityText)

        submitButton.setOnClickListener {

            val entered = valueEditText.text.toString().toDoubleOrNull()

            if (entered == null || entered < 0.0) { // Check against 0.0
                Toast.makeText(context, "Enter a valid quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (entered > stock) {
                Toast.makeText(context, "Stock limit: ${price.stockCount}", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            saveQuantity(product, price, entered)
            updateQuantityVisibility(price, holder)
            onCartChange(products)
            dialog.dismiss()
        }

        cancelBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun updatePriceViews(price: NewProductPrice, holder: POSViewHolder) {
        holder.productAmount.text = price.productPrice ?: "0.00"
    }

    private fun updateQuantityVisibility(price: NewProductPrice, holder: POSViewHolder) {
        val currentQty = getCurrentQuantity(price)

        if (currentQty > 0.0) {
            holder.addToBag.visibility = View.GONE
            holder.quantityLayout.visibility = View.VISIBLE
            holder.txtQuantity.text = if (quantityStatus == "1")
                "%.1f".format(currentQty)
            else
                currentQty.toInt().toString()
        } else {
            holder.addToBag.visibility = View.VISIBLE
            holder.quantityLayout.visibility = View.GONE
        }
    }

    private fun getSelectedPrice(product: NewProductList, holder: POSViewHolder): NewProductPrice {
        val index = product.selectedVariationIndex
        return product.productPrice?.get(index) ?: throw IllegalStateException("No variation")
    }

    private fun syncQuantitiesFromCart(productId: Int?, price: NewProductPrice, quantityStatus: String?) {
        val key = "${productId}_${price.productPriceId}"
        val quantityInCart = CartManager.cartMap[key] ?: 0.0 // Read Double

        if (quantityStatus == "1") {
            price.selectedQuantityDecimal = quantityInCart
            price.selectedQuantity = quantityInCart.toInt() // Still keep an Int value
        } else {
            price.selectedQuantity = quantityInCart.toInt()
            price.selectedQuantityDecimal = quantityInCart // Keep them in sync
        }

        if (quantityInCart > 0.0) {
            CartManager.allProductsMap[key] = price
        } else {
            CartManager.allProductsMap.remove(key)
        }
    }


    fun refreshCart() {
        products.forEach { product ->
            product.productPrice?.forEach { price ->
                val key = "${product.productId}_${price.productPriceId}"
                val quantityInCart = CartManager.cartMap[key] ?: 0.0

                if (quantityStatus == "1") {
                    price.selectedQuantityDecimal = quantityInCart
                    price.selectedQuantity = quantityInCart.toInt()
                } else {
                    price.selectedQuantity = quantityInCart.toInt()
                    price.selectedQuantityDecimal = quantityInCart
                }

                if (quantityInCart > 0.0) {
                    CartManager.allProductsMap[key] = price
                } else {
                    CartManager.allProductsMap.remove(key)
                }
            }
        }
        notifyDataSetChanged()
    }




    override fun getItemCount(): Int = products.size
}
