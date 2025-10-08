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


    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiOTMyZGFhM2IyNGZmM2Q0ZTY5NmQwYTdlODdhNmY1Y2ViOTA1Y2VhMzc0NWU5NmFlMTBhZWEwZjY2MDkxZDZiMWI1MjIwYjIwYmU4N2EyNDEiLCJpYXQiOjE3NTg3OTI2NDEuMjExMDEsIm5iZiI6MTc1ODc5MjY0MS4yMTEwMTMsImV4cCI6MTc5MDMyODY0MS4yMDQ5NjYsInN1YiI6IjYiLCJzY29wZXMiOltdfQ.lcZz0mc3u-to55t0p23p5g_Z1NQHM23K6xT8vaRAr7X9qHsMbyN9OEq-KuHhZGaqikcY-7ak26uM3_mtoLpLfq6jivhUPFC06JL7ID3HRHOUsWY05CqjIwPpOfjop127eWyz1CYmBmZx3mKsSuE2rvNK91OsROryf1Dz-Px0SnVJ1uDJGK9y1zsJ_Mi8wY7WIH_E3cBusI7uSgNHTQq7dh6GurCYymPxnvvR8cdMQ4Om9SnmfqX9f3GCUncHXBVfxYCuH6ElLsjq74Z9ZXRGBLdwdM4BJWiyv4jzKfU80LmErPo7XT90DPzqa40T0pRblACQsaSGLn64TBoKvxlsO4HjJV8nBg3az5PrCkDsj8QTwgPLzJtP7WcT3pvcCI6O3O8OKlL2lR2-csFHNzCHetHaT1fmOLnVWuc5YIjqhYFEOjp8IKVhzxmcocxsd3R8bcvrjR8NcutOX5H7zmfD-GX17f64RT2c0zqSRdVpRxFZYlNycxd3rI591w9ImgZSkeGQN4Eg8us8oqmlRqfF5mO7QZXi_OsjJgnMdovqP1NB1IxHuTHQIyfESkQ3DoA_KYBF4_8DXhyjvE2D5_SQfZitUpjSwfWqZ_ghgVoOdLJokz4TBJQ9j_ec4jK3uf3nCwS_6Evx9zwbZxmin2CpnIrg4lFRmMpO6YgLfYKPqoI"


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
