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
    private val onAddClick: (NewProductList, NewProductPrice, Double) -> Unit
) : RecyclerView.Adapter<POSAdapter.POSViewHolder>() {

    inner class POSViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: AppCompatTextView = itemView.findViewById(R.id.product_name)
        val productImage: RoundedImageView = itemView.findViewById(R.id.product_img)
        val addToBag: AppCompatTextView = itemView.findViewById(R.id.add_to_bag)
        val outOfStock: AppCompatTextView = itemView.findViewById(R.id.out_of_stock)
        val lowStockAlert: View = itemView.findViewById(R.id.low_stock_alert)
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

        // Spinner for variations
        if (priceList.size > 1) {
            holder.container.visibility = View.VISIBLE
            holder.spinnerLayout.visibility = View.VISIBLE
            val variationList = priceList.map { it.productVariation ?: "Default" }
            val adapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_item, variationList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            holder.variationSpinner.adapter = adapter
            priceList.getOrNull(0)?.let { updatePriceViews(it, holder) }
            holder.variationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    priceList.getOrNull(pos)?.let { updatePriceViews(it, holder) }
                    updateAddQuantityVisibility(product, holder, priceList.getOrNull(pos))
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        } else {
            holder.container.visibility = View.GONE
            holder.spinnerLayout.visibility = View.GONE
            val priceObj = priceList.firstOrNull()
            holder.productAmount.text = priceObj?.productPrice ?: "0.00"
            updateStockViews(priceObj, holder)
        }

        // Initialize Add / Quantity / OutOfStock layout
        val selectedPrice = if (priceList.size > 1)
            priceList.getOrNull(holder.variationSpinner.selectedItemPosition)
        else
            priceList.firstOrNull()

        updateAddQuantityVisibility(product, holder, selectedPrice)

        // ADD button click
        holder.addToBag.setOnClickListener {
            if ((selectedPrice?.stockCount ?: 0) <= 0) return@setOnClickListener
            product.selectedQuantity = 1
            holder.txtQuantity.text = "1"
            holder.addToBag.visibility = View.GONE
            holder.quantityLayout.visibility = View.VISIBLE
            onAddClick(product, selectedPrice!!, 1.0)
        }

        // INCREASE button
        holder.btnIncrease.setOnClickListener {
            if ((selectedPrice?.stockCount ?: 0) <= 0) return@setOnClickListener
            product.selectedQuantity += 1
            holder.txtQuantity.text = product.selectedQuantity.toString()
            onAddClick(product, selectedPrice!!, product.selectedQuantity.toDouble())
        }

        // DECREASE button
        holder.btnDecrease.setOnClickListener {
            if (product.selectedQuantity > 1) {
                product.selectedQuantity -= 1
                holder.txtQuantity.text = product.selectedQuantity.toString()
                onAddClick(product, selectedPrice!!, product.selectedQuantity.toDouble())
            } else {
                product.selectedQuantity = 0
                holder.txtQuantity.text = "0"
                holder.quantityLayout.visibility = View.GONE
                holder.addToBag.visibility = View.VISIBLE
            }
        }
    }

    private fun updatePriceViews(price: NewProductPrice, holder: POSViewHolder) {
        holder.productAmount.text = price.productPrice ?: "0.00"
        updateStockViews(price, holder)
    }

    private fun updateStockViews(price: NewProductPrice?, holder: POSViewHolder) {
        val stock = price?.stockCount ?: 0
        val alertLimit = price?.low_stock_alert ?: 0
        holder.outOfStock.visibility = if (stock <= 0) View.VISIBLE else View.GONE
        holder.lowStockAlert.visibility = if (stock in 1..alertLimit) View.VISIBLE else View.GONE
    }

    private fun updateAddQuantityVisibility(product: NewProductList, holder: POSViewHolder, price: NewProductPrice?) {
        val stock = price?.stockCount ?: 0
        if (stock <= 0) {
            holder.addToBag.visibility = View.GONE
            holder.quantityLayout.visibility = View.GONE
        } else {
            if (product.selectedQuantity > 0) {
                holder.addToBag.visibility = View.GONE
                holder.quantityLayout.visibility = View.VISIBLE
                holder.txtQuantity.text = product.selectedQuantity.toString()
            } else {
                holder.addToBag.visibility = View.VISIBLE
                holder.quantityLayout.visibility = View.GONE
                holder.txtQuantity.text = "0"
            }
        }
    }

    override fun getItemCount(): Int = products.size
}
