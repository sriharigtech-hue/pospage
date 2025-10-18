package com.example.apitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import androidx.core.content.ContextCompat
import com.example.apitest.dataModel.StockProductData
import com.google.android.material.imageview.ShapeableImageView
import com.bumptech.glide.Glide
import com.example.apitest.dataModel.StatusResponse
import com.example.apitest.dataModel.StatusUpdateInput
import com.example.apitest.network.ApiClient

class StockProductAdapter(
    private val products: MutableList<StockProductData>
) : RecyclerView.Adapter<StockProductAdapter.StockViewHolder>() {
    private val allProducts = ArrayList<StockProductData>().apply { addAll(products) }  // backup list


    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZGE0YjJmNjlmZGJkNjMwMDMyNGE3MWNkZWRhMDI2ZWI2YTIwMGM4NWIyNTI2MTNjOTZhZGIyMDA2MTE3YjMxMGI0MTFjYjczNzNmZmNlZDAiLCJpYXQiOjE3NjAzMjkzNTkuNDc3MTA1LCJuYmYiOjE3NjAzMjkzNTkuNDc3MTA4LCJleHAiOjE3OTE4NjUzNTkuNDcyNjI1LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.C3ySWdDX7BRHm4qzwWFZZofL_DEx3C2Qjy7iEUWxy9GdrL8OJS7m7Kk_Oe4HtFaT7DvPMEWE_c9kIC8RalMXflXTvPGKkfsw7yxdVxZOKSE20UNZiSbScAdvx3RxAz-XoHK4wJr7wepspLad5y5KCv4RyPXAJl8sIjFELfiCMoxt1CiYGp5_GhsOjbMeSLWSBoDwd3H4MLNvUyU2KN2zhvQaRRUh4T-L11mZgmd_8A8kWZbp_bO6AK-3hGHFGd7VaT2Xqoi4asmn0ABlxusVYWG6hw9UhnU-_uxOVFQLAHog-WKfbahCwfkssXtK07wMpk-ZGHfRn7ujbkrMAX5gNgNkcNQZPRMkUSrokHylEJXKC7UOAgUiK8fy32bIlmFuMQE9hTuuQjHWJ8hdEqtPaXVIcc1oXURtZhCWTp2APH9RE4_L41NYStog_bVMdXwRO_a6QEg_ex0moqxwtRZKivnIF4DKm6WLj45X0FLj-F7HTlZ-eoc9j3w_dVaVyhhxEKUiTyQSJ_AwVKMTbAUmxvWY3OnoIAmu4WYrbC4T4tA2cWoB9yXKna8Yfbil_vC46tLZweGF7RRZR2MPT16q-iCzKG73JqAMphV4NO7b-bMk6mhvgz8TR0_YUewsPg2CVvgdvEmnV4DE4znhnwiLMniN0kPGzF5pindkKTVNDb8"


    inner class StockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val quantity: TextView = itemView.findViewById(R.id.quantity)
        val editButton: ShapeableImageView = itemView.findViewById(R.id.edit_icon)     }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stock_category, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val product = products[position]
        val nameWithVariation = if (!product.productVariationName.isNullOrEmpty()) {
            "${product.productName} - ${product.productVariationName}"
        } else {
            product.productName ?: ""
        }
        holder.productName.text = nameWithVariation
        holder.quantity.text = product.stockCount?.toString() ?: "0"

        val stock = product.stockCount ?: 0
        val lowAlert = product.low_stock_alert ?: 0

        holder.quantity.text = stock.toString()

        // Check if stock is below alert
        if (stock <= lowAlert) {
            holder.quantity.setTextColor(holder.itemView.context.getColor(android.R.color.holo_red_dark))
        } else {
            holder.quantity.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
        }



        // You can load product image if needed with Glide
        // Glide.with(holder.itemView.context).load(product.productImage).into(holder.editButton)

        holder.editButton.setOnClickListener {
            val context = holder.itemView.context
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_stock, null)
            val heading = dialogView.findViewById<TextView>(R.id.percentage)
            heading.text = "Stock Available"


            val dialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .create()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val cancel = dialogView.findViewById<ImageView>(R.id.cancel)
            val submit = dialogView.findViewById<TextView>(R.id.Submit)
            val addition = dialogView.findViewById<RadioButton>(R.id.Addition)
            val subtraction = dialogView.findViewById<RadioButton>(R.id.Subtraction)
            val quantityInput = dialogView.findViewById<EditText>(R.id.productVariation)


            quantityInput.inputType = android.text.InputType.TYPE_CLASS_NUMBER


            cancel.setOnClickListener { dialog.dismiss() }

            submit.setOnClickListener {
                val qtyStr = quantityInput.text.toString()
                if (qtyStr.isEmpty()) {
                    Toast.makeText(context, "Enter quantity", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val qty = qtyStr.toInt()
                val operation = when {
                    addition.isChecked -> "add"
                    subtraction.isChecked -> "subtract"
                    else -> ""
                }

                if (operation.isEmpty()) {
                    Toast.makeText(context, "Select Addition or Subtraction", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Prepare API input
                val input = StatusUpdateInput(
                    stock_id = product.id?.toString(),
                    stock_quantity = qty.toString(),
                    operation = operation,
                    status = 1
                )

                // Call API
                ApiClient.instance.productStockUpdate(jwtToken, input)
                    .enqueue(object : retrofit2.Callback<StatusResponse> {
                        override fun onResponse(
                            call: retrofit2.Call<StatusResponse>,
                            response: retrofit2.Response<StatusResponse>
                        ) {
                            if (response.isSuccessful && response.body()?.status == true) {

                                // Update local quantity
                                product.stockCount = if (operation == "add") {
                                    (product.stockCount ?: 0) + qty
                                } else {
                                    (product.stockCount ?: 0) - qty
                                }
                                notifyItemChanged(holder.adapterPosition)
                                Toast.makeText(context, "Stock updated: ${product.stockCount}", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to update stock", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: retrofit2.Call<StatusResponse>, t: Throwable) {
                            Toast.makeText(context, "API error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })

                dialog.dismiss()
            }

            dialog.show()
        }

    }


    override fun getItemCount(): Int = products.size
    fun filter(query: String) {
        products.clear()
        if (query.isEmpty()) {
            products.addAll(allProducts)
        } else {
            val lower = query.lowercase()
            products.addAll(
                allProducts.filter {
                    it.productName?.lowercase()?.contains(lower) == true ||
                            it.productVariationName?.lowercase()?.contains(lower) == true
                }
            )
        }
        notifyDataSetChanged()
    }
    fun updateList(newList: List<StockProductData>) {
        products.clear()
        products.addAll(newList)

        allProducts.clear()
        allProducts.addAll(newList)

        notifyDataSetChanged()
    }


}
