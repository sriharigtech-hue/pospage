package com.example.apitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.apitest.R
import com.example.apitest.dataModel.NewProductList
import com.example.apitest.dataModel.NewProductPrice
import com.example.apitest.helperClass.CartManager
import com.makeramen.roundedimageview.RoundedImageView

class POSAdapter(
    private val products: List<NewProductList>,
    private val onCartChange: (List<NewProductList>) -> Unit
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
            holder.variationSpinner.onItemSelectedListener = null
            holder.variationSpinner.setSelection(lastSelectedIndex, false)

            val restoredPrice = priceList[lastSelectedIndex]
            val restoredKey = "${product.productId}_${restoredPrice.productPriceId}"
            restoredPrice.selectedQuantity = CartManager.cartMap[restoredKey] ?: 0
            CartManager.allProductsMap[restoredKey] = restoredPrice
            updatePriceViews(restoredPrice, holder)
            updateQuantityVisibility(restoredPrice, holder)

            holder.variationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    product.selectedVariationIndex = pos
                    variationMap[product.productId.toString()] = pos

                    val selectedPrice = priceList[pos]
                    val key = "${product.productId}_${selectedPrice.productPriceId}"
                    selectedPrice.selectedQuantity = CartManager.cartMap[key] ?: 0
                    CartManager.allProductsMap[key] = selectedPrice

                    updatePriceViews(selectedPrice, holder)
                    updateQuantityVisibility(selectedPrice, holder)

// only keep cart entry if quantity > 0
                    if (selectedPrice.selectedQuantity > 0) {
                        CartManager.cartMap[key] = selectedPrice.selectedQuantity
                    } else {
                        CartManager.cartMap.remove(key)
                    }

// ✅ Only trigger update if this variation is already in cart (has qty > 0)
                    if (selectedPrice.selectedQuantity > 0) {
                        onCartChange(products)
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
        holder.addToBag.setOnClickListener {
            val activePrice = priceList[product.selectedVariationIndex]
            val activeKey = "${product.productId}_${activePrice.productPriceId}"

            if (!CartManager.cartMap.containsKey(activeKey) || CartManager.cartMap[activeKey] == 0) {
                activePrice.selectedQuantity = 1
                CartManager.cartMap[activeKey] = activePrice.selectedQuantity
                CartManager.allProductsMap[activeKey] = activePrice
                updateQuantityVisibility(activePrice, holder)
                onCartChange(products) // updates cart bar & badge
            }
        }

        holder.btnIncrease.setOnClickListener {
            val activePrice = priceList[product.selectedVariationIndex]
            val activeKey = "${product.productId}_${activePrice.productPriceId}"
            activePrice.selectedQuantity++
            CartManager.cartMap[activeKey] = activePrice.selectedQuantity
            CartManager.allProductsMap[activeKey] = activePrice
            updateQuantityVisibility(activePrice, holder)
            onCartChange(products) // only updates cart bar, badge count handled automatically
        }

        holder.btnDecrease.setOnClickListener {
            val activePrice = priceList[product.selectedVariationIndex]
            val activeKey = "${product.productId}_${activePrice.productPriceId}"
            activePrice.selectedQuantity = maxOf(0, activePrice.selectedQuantity - 1)

            if (activePrice.selectedQuantity == 0) {
                CartManager.cartMap.remove(activeKey) // remove product if quantity 0
            } else {
                CartManager.cartMap[activeKey] = activePrice.selectedQuantity
            }

            CartManager.allProductsMap[activeKey] = activePrice
            updateQuantityVisibility(activePrice, holder)
            onCartChange(products) // updates cart bar & badge
        }

    }



    private fun updatePriceViews(price: NewProductPrice, holder: POSViewHolder) {
        holder.productAmount.text = price.productPrice ?: "0.00"
    }

    private fun updateQuantityVisibility(price: NewProductPrice, holder: POSViewHolder) {
        if (price.selectedQuantity > 0) {
            holder.addToBag.visibility = View.GONE
            holder.quantityLayout.visibility = View.VISIBLE
            holder.txtQuantity.text = price.selectedQuantity.toString()
        } else {
            holder.addToBag.visibility = View.VISIBLE
            holder.quantityLayout.visibility = View.GONE
            holder.txtQuantity.text = "0"
        }
    }

    override fun getItemCount(): Int = products.size
}
