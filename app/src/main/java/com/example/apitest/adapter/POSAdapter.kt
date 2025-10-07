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
import com.makeramen.roundedimageview.RoundedImageView

class POSAdapter(
    private val products: List<NewProductList>,
    private val cartMap: MutableMap<String, Int>,
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
        } else {
            holder.container.visibility = View.VISIBLE
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

            // Remove listener before setting selection to prevent unwanted triggers
            holder.variationSpinner.onItemSelectedListener = null
            holder.variationSpinner.setSelection(lastSelectedIndex, false)

            // Set listener after selection
            holder.variationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    product.selectedVariationIndex = pos
                    variationMap[product.productId.toString()] = pos

                    val selectedPrice = priceList[pos]
                    val key = "${product.productId}_${selectedPrice.productPriceId}"

                    // Restore previous quantity
                    selectedPrice.selectedQuantity = cartMap[key] ?: 0

                    updatePriceViews(selectedPrice, holder)
                    updateQuantityVisibility(selectedPrice, holder)

                    // Save current quantity in cart map
                    cartMap[key] = selectedPrice.selectedQuantity
                    onCartChange(products)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        } else {
            holder.spinnerLayout.visibility = View.GONE
        }

        // Restore quantity for currently selected variation
        val selectedPrice = priceList[product.selectedVariationIndex]
        val key = "${product.productId}_${selectedPrice.productPriceId}"
        selectedPrice.selectedQuantity = cartMap[key] ?: 0
        updatePriceViews(selectedPrice, holder)
        updateQuantityVisibility(selectedPrice, holder)

        // Add to cart button
        holder.addToBag.setOnClickListener {
            selectedPrice.selectedQuantity = 1
            cartMap[key] = selectedPrice.selectedQuantity
            updateQuantityVisibility(selectedPrice, holder)
            onCartChange(products)
        }

        holder.btnIncrease.setOnClickListener {
            selectedPrice.selectedQuantity += 1
            cartMap[key] = selectedPrice.selectedQuantity
            updateQuantityVisibility(selectedPrice, holder)
            onCartChange(products)
        }

        holder.btnDecrease.setOnClickListener {
            selectedPrice.selectedQuantity = if (selectedPrice.selectedQuantity > 1) selectedPrice.selectedQuantity - 1 else 0
            cartMap[key] = selectedPrice.selectedQuantity
            updateQuantityVisibility(selectedPrice, holder)
            onCartChange(products)
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
