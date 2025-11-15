package com.example.apitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.apitest.R
import android.widget.*
import com.example.apitest.dataModel.NewProductList
import com.example.apitest.dataModel.NewProductPrice
import com.makeramen.roundedimageview.RoundedImageView
import kotlin.math.max

class POSAdapter(
    private val products: List<NewProductList>,
    private val onCartChange: (List<NewProductList>) -> Unit
) : RecyclerView.Adapter<POSAdapter.POSViewHolder>() {

    inner class POSViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: RoundedImageView = itemView.findViewById(R.id.product_img)
        val productName: AppCompatTextView = itemView.findViewById(R.id.product_name)
        val productAmount: AppCompatTextView = itemView.findViewById(R.id.product_amount)
        val addButton: AppCompatTextView = itemView.findViewById(R.id.add_to_bag)
        val qtyLayout: LinearLayout = itemView.findViewById(R.id.quantity_layout)
        val btnIncrease: AppCompatTextView = itemView.findViewById(R.id.btn_increase)
        val btnDecrease: AppCompatTextView = itemView.findViewById(R.id.btn_decrease)
        val txtQuantity: AppCompatTextView = itemView.findViewById(R.id.txt_quantity)
        val productNumber: AppCompatTextView = itemView.findViewById(R.id.product_number) // <-- Seq No

        val container: View = itemView.findViewById(R.id.container)
        val spinnerLayout: LinearLayout = itemView.findViewById(R.id.spinnerLayout)
        val amountSpinner: Spinner = itemView.findViewById(R.id.amount_spinner)
        val variationText: AppCompatTextView = itemView.findViewById(R.id.variation)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): POSViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pos_product, parent, false)
        return POSViewHolder(view)
    }

    override fun onBindViewHolder(holder: POSViewHolder, position: Int) {
        val product = products[position]
        val price = product.productPrice?.firstOrNull() ?: return

        // Bind product details
        holder.productName.text = product.productName ?: ""
        holder.productAmount.text = "₹${price.productPrice ?: "0.00"}"
        holder.productNumber.text = product.seq_no ?: "" // <-- Set sequence number


        Glide.with(holder.itemView.context)
            .load(product.productImage.takeIf { !it.isNullOrEmpty() } ?: "https://via.placeholder.com/150")
            .into(holder.productImage)

        val stock = price.stockCount?.toDouble() ?: 0.0
        var qty = price.selectedQuantityDecimal.takeIf { it > 0 } ?: 0.0

        val variations = product.productPrice
        if (!variations.isNullOrEmpty() && variations.size > 1) {
            holder.container.visibility = View.VISIBLE
            holder.spinnerLayout.visibility = View.VISIBLE
            holder.variationText.visibility = View.GONE

            // ✅ Show variation names only
            val variantNames = variations.map { it.productVariation ?: "Variant" }

            val spinnerAdapter = ArrayAdapter(
                holder.itemView.context,
                android.R.layout.simple_spinner_item,
                variantNames
            )
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            holder.amountSpinner.adapter = spinnerAdapter

            // Set last selected variation for this product
            holder.amountSpinner.setSelection(product.selectedVariationIndex)

            holder.amountSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    id: Long
                ) {
                    product.selectedVariationIndex = pos
                    val selectedPrice = variations[pos]

                    // ✅ Update displayed price
                    holder.productAmount.text = "₹${selectedPrice.productPrice ?: "0.00"}"

                    // ✅ Update qty & stock info for the new variant
                    val stock = selectedPrice.stockCount?.toDouble() ?: 0.0
                    var qty = selectedPrice.selectedQuantityDecimal.takeIf { it > 0 } ?: 0.0

                    updateQtyUI(holder, qty)

                    // ✅ Add/Increase/Decrease now refer to *selected* variation
                    holder.addButton.setOnClickListener {
                        if (stock <= 0) {
                            Toast.makeText(holder.itemView.context, "Out of stock", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        qty = 1.0
                        selectedPrice.selectedQuantityDecimal = qty
                        updateQtyUI(holder, qty)
                        onCartChange(products)
                    }

                    holder.btnIncrease.setOnClickListener {
                        if (qty < stock) {
                            qty += 1.0
                            selectedPrice.selectedQuantityDecimal = qty
                            updateQtyUI(holder, qty)
                            onCartChange(products)
                        } else {
                            Toast.makeText(holder.itemView.context, "Stock limit reached", Toast.LENGTH_SHORT).show()
                        }
                    }

                    holder.btnDecrease.setOnClickListener {
                        if (qty > 0) {
                            qty = max(0.0, qty - 1.0)
                            selectedPrice.selectedQuantityDecimal = qty
                            updateQtyUI(holder, qty)
                            onCartChange(products)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        } else {
            holder.container.visibility = View.GONE
        }

        updateQtyUI(holder, qty)

        holder.addButton.setOnClickListener {
            if (stock <= 0) {
                Toast.makeText(holder.itemView.context, "Out of stock", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            qty = 1.0
            price.selectedQuantityDecimal = qty
            updateQtyUI(holder, qty)
            onCartChange(products)
        }

        holder.btnIncrease.setOnClickListener {
            if (qty < stock) {
                qty += 1.0
                price.selectedQuantityDecimal = qty
                updateQtyUI(holder, qty)
                onCartChange(products)
            } else {
                Toast.makeText(holder.itemView.context, "Stock limit reached", Toast.LENGTH_SHORT).show()
            }
        }

        holder.btnDecrease.setOnClickListener {
            if (qty > 0) {
                qty = max(0.0, qty - 1.0)
                price.selectedQuantityDecimal = qty
                updateQtyUI(holder, qty)
                onCartChange(products)
            }
        }
    }

    private fun updateQtyUI(holder: POSViewHolder, qty: Double) {
        if (qty > 0) {
            holder.addButton.visibility = View.GONE
            holder.qtyLayout.visibility = View.VISIBLE
            holder.txtQuantity.text = qty.toInt().toString()
        } else {
            holder.addButton.visibility = View.VISIBLE
            holder.qtyLayout.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = products.size
}
