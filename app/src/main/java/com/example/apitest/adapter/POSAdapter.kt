package com.example.apitest.adapter

import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
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

        // Restore last selected variation index
        val lastSelectedIndex = variationMap[product.productId.toString()] ?: 0
        product.selectedVariationIndex = lastSelectedIndex

        if (priceList.size > 1) {
            holder.spinnerLayout.visibility = View.VISIBLE
            val variationNames = priceList.map { it.productVariation ?: "Default" }
            val spinnerAdapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_item, variationNames)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            holder.variationSpinner.adapter = spinnerAdapter

            // Restore last selected variation index
            val lastSelectedIndex = variationMap[product.productId.toString()] ?: 0

            // Temporarily remove listener to prevent onItemSelected being triggered by setSelection
            holder.variationSpinner.onItemSelectedListener = null
            holder.variationSpinner.setSelection(lastSelectedIndex, false)

            val restoredPrice = priceList[lastSelectedIndex]
            val restoredKey = "${product.productId}_${restoredPrice.productPriceId}"
            restoredPrice.selectedQuantity = CartManager.cartMap[restoredKey] ?: 0
            CartManager.allProductsMap[restoredKey] = restoredPrice
            updatePriceViews(restoredPrice, holder)
            updateQuantityVisibility(restoredPrice, holder)

            // Re-attach listener
            holder.variationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    if (product.selectedVariationIndex != pos) { // only act if changed
                        product.selectedVariationIndex = pos
                        variationMap[product.productId.toString()] = pos

                        val selectedPrice = priceList[pos]
                        val key = "${product.productId}_${selectedPrice.productPriceId}"
                        selectedPrice.selectedQuantity = CartManager.cartMap[key] ?: 0
                        CartManager.allProductsMap[key] = selectedPrice
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
        val key = "${product.productId}_${currentPrice.productPriceId}"
        currentPrice.selectedQuantity = CartManager.cartMap[key] ?: 0
        CartManager.allProductsMap[key] = currentPrice
        updatePriceViews(currentPrice, holder)
        updateQuantityVisibility(currentPrice, holder)

        // Add to bag
        holder.addToBag.setOnClickListener {
            val selectedPrice = getSelectedPrice(product, holder)
            val key = "${product.productId}_${selectedPrice.productPriceId}"

            if (selectedPrice.stockCount == null || selectedPrice.stockCount == 0) {
                Toast.makeText(holder.itemView.context, "Out of stock", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedPrice.selectedQuantity < (selectedPrice.stockCount ?: 0)) {
                selectedPrice.selectedQuantity = 1
                CartManager.cartMap[key] = selectedPrice.selectedQuantity
                CartManager.allProductsMap[key] = selectedPrice
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

    private fun incrementQuantity(product: NewProductList, price: NewProductPrice, holder: POSViewHolder) {
        val stock = price.stockCount ?: 0
        if (price.selectedQuantity < stock) {
            price.selectedQuantity++
            val key = "${product.productId}_${price.productPriceId}"
            CartManager.cartMap[key] = price.selectedQuantity
            CartManager.allProductsMap[key] = price
            updateQuantityVisibility(price, holder)
            onCartChange(products)
        } else {
            Toast.makeText(holder.itemView.context, "Stock limit reached", Toast.LENGTH_SHORT).show()
        }
    }

    private fun decrementQuantity(product: NewProductList, price: NewProductPrice, holder: POSViewHolder) {
        price.selectedQuantity = maxOf(0, price.selectedQuantity - 1)
        val key = "${product.productId}_${price.productPriceId}"
        if (price.selectedQuantity == 0) CartManager.cartMap.remove(key)
        else CartManager.cartMap[key] = price.selectedQuantity
        CartManager.allProductsMap[key] = price
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

        // Show stock
        stockTextView.visibility = View.VISIBLE
        stockTextView.text = "Available: ${price.stockCount ?: 0}"

        // ✅ Set initial quantity in dialog with correct format
        val initialQuantity = if (quantityStatus == "1") {
            "%.1f".format(price.selectedQuantity.toDouble())
        } else {
            price.selectedQuantity.toString()
        }
        valueEditText.setText(initialQuantity)

        submitButton.setOnClickListener {
            val entered = valueEditText.text.toString().toDoubleOrNull()
            val stock = price.stockCount?.toDouble() ?: 0.0

            if (entered == null || entered <= 0) {
                Toast.makeText(context, "Enter a valid quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (entered > stock) {
                Toast.makeText(context, "Stock limit: ${price.stockCount}", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Store as Int if quantityStatus = "0", else keep decimal as Double
            price.selectedQuantity = if (quantityStatus == "1") {
                entered.toInt() // keep as Int in model, display as decimal
            } else {
                entered.toInt()
            }

            val key = "${product.productId}_${price.productPriceId}"
            CartManager.cartMap[key] = price.selectedQuantity
            CartManager.allProductsMap[key] = price

            // Update RecyclerView quantity display
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
        if (price.selectedQuantity > 0) {
            holder.addToBag.visibility = View.GONE
            holder.quantityLayout.visibility = View.VISIBLE

            holder.txtQuantity.text = if (quantityStatus == "1") {
                "%.1f".format(price.selectedQuantity.toDouble()) // show decimal
            } else {
                price.selectedQuantity.toString()                 // show integer
            }

        } else {
            holder.addToBag.visibility = View.VISIBLE
            holder.quantityLayout.visibility = View.GONE
            holder.txtQuantity.text = if (quantityStatus == "1") "0" else "-1"
        }
    }

    private fun getSelectedPrice(product: NewProductList, holder: POSViewHolder): NewProductPrice {
        val index = product.selectedVariationIndex
        return product.productPrice?.get(index) ?: throw IllegalStateException("No variation")
    }


    override fun getItemCount(): Int = products.size
}
