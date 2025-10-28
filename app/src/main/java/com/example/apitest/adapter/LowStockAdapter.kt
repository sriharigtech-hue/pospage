package com.example.apitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.LowStockProductData
import com.example.apitest.dataModel.StatusResponse
import com.example.apitest.dataModel.StatusUpdateInput
import com.example.apitest.dataModel.StockProductData
import com.example.apitest.network.ApiClient
import com.google.android.material.imageview.ShapeableImageView

class LowStockAdapter(
    private val products: MutableList<LowStockProductData>,
) : RecyclerView.Adapter<LowStockAdapter.LowStockViewHolder>() {

    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZTRjY2JlNDBmYWFiZmE5YTg1YmVkMDdjOTcyMzAzNWY3MTljNzQ4M2Q3YzIzMjg3M2E5ZjZmN2QyMjQ1Y2E3MjVhNjVlMTQ5ZGRiZjBkYjgiLCJpYXQiOjE3NjE2MzM3OTIuNDI5OTk5LCJuYmYiOjE3NjE2MzM3OTIuNDMwMDAzLCJleHAiOjE3OTMxNjk3OTIuNDIyNjY5LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.SutcB4SMDdEzgfDZ1lnzGDjY4gf1aSwNJMGFnKF3RXWGbB4MhP6R8wRLCa2wtCMZ1egUQc1LxPMq2S9P-hfCeh4stj4W-zlWf0xOfvswvWVos14aP5RBTjbZ8Rc3PrmAwiFvnzu7PpRtqy8hGF_h1nt2dJrZpT4k0CIuvpUK8afLBKYfpr0i1NOx1awb2Z6Uo5YfJRQrB7y4wrs8TEsnbuAIdC4JG6-9Oi_TPwZ42SGatw4UEUvm09I3SjKGjZRDCOMJZLbSc1O4C6B53aK9hNQKCjnwsSXWc37h-cA6lKg-DSfm6K1usg0yHeAsE-2uya2_b8_TNq3LN4Mb04S820FmpnP0RqtDoPeoqBUSTacd0bSINimYpNv8NyiaO0D6k0J3HzaNd5MmwORyHpTNnVEaM8l0O5iyI-UIa3bPaOBnltamiAydE2EmUA9pfRQy3HWd6yZnxfuITM0THdj73ju1Qh3D0WVP7aUFR-3XUbp5qVflBZkiBe0klClG94ubWNFMX6vebixLQ21KsDvEDLj2Xy0hoLY-g6sm33l14NwbSiUgZ0VQI_3WbOzSpUvU0sprU7ozX7y7-nwjIXjshOQ5ymktZUMCN7LyCbvgX-qcGyxbS8C9JN9BhmvhagIBatDpPZw75arXlksHzxKnytbLG3BVxFHXxkp9jSBqW2s"

    private val allProducts = products.toMutableList()
    private val displayedProducts = products.toMutableList()
    inner class LowStockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val quantity: TextView = itemView.findViewById(R.id.quantity)
        val editButton: ShapeableImageView = itemView.findViewById(R.id.edit_icon)



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LowStockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_low_stock_list, parent, false)
        return LowStockViewHolder(view)
    }

    override fun onBindViewHolder(holder: LowStockViewHolder, position: Int) {
        val product = displayedProducts[position]

        // Combine product name and variation name
        val nameWithVariation = if (!product.productVariationName.isNullOrEmpty()) {
            "${product.productName} - ${product.productVariationName}"
        } else {
            product.productName ?: ""
        }

        holder.productName.text = nameWithVariation
        holder.quantity.text = product.stockCount?.toString() ?: "0"


        holder.editButton.setOnClickListener {
            val context = holder.itemView.context
            val dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_edit_stock, null)

            val heading = dialogView.findViewById<TextView>(R.id.percentage)
            heading.text = "Low Stock"


            val dialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .create()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)




            val cancel = dialogView.findViewById<AppCompatImageView>(R.id.cancel)
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

                val input = StatusUpdateInput(
                    stock_id = product.id?.toString(),
                    stock_quantity = qty.toString(),
                    operation = operation,
                    status = 1
                )

                // Call API to update stock
                ApiClient.instance.productStockUpdate(jwtToken, input)
                    .enqueue(object : retrofit2.Callback<StatusResponse> {
                        override fun onResponse(
                            call: retrofit2.Call<StatusResponse>,
                            response: retrofit2.Response<StatusResponse>
                        ) {
                            if (response.isSuccessful && response.body()?.status == true) {
                                // Update local stock
                                product.stockCount = if (operation == "add") {
                                    (product.stockCount ?: 0) + qty
                                } else {
                                    (product.stockCount ?: 0) - qty
                                }

                                // Remove from low-stock list if stock exceeds alert
                                if ((product.stockCount ?: 0) > (product.low_stock_alert ?: 0)) {
                                    val pos = holder.adapterPosition
                                    displayedProducts.removeAt(pos)
                                    notifyItemRemoved(pos)
                                } else {
                                    notifyItemChanged(holder.adapterPosition)
                                }

                                Toast.makeText(context, "Stock updated", Toast.LENGTH_SHORT).show()
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

    override fun getItemCount(): Int = displayedProducts.size
    fun updateData(newData: List<LowStockProductData>) {
        allProducts.clear()
        allProducts.addAll(newData)
        displayedProducts.clear()
        displayedProducts.addAll(newData)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val filtered = if (query.isEmpty()) {
            allProducts
        } else {
            allProducts.filter { it.productName?.contains(query, ignoreCase = true) == true }
        }
        displayedProducts.clear()
        displayedProducts.addAll(filtered)
        notifyDataSetChanged()
    }
}

