package com.example.apitest.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.StatusResponse
import com.example.apitest.dataModel.StatusUpdateInput
import com.example.apitest.dataModel.StockProductData
import com.example.apitest.network.ApiClient
import com.github.angads25.toggle.widget.LabeledSwitch
import com.google.android.material.imageview.ShapeableImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ItemsAdapter(
    private val items: MutableList<StockProductData>
) : RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {
    private var onEditClickListener: ((StockProductData) -> Unit)? = null
    private var onDeleteClickListener: ((StockProductData) -> Unit)? = null
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZTRjY2JlNDBmYWFiZmE5YTg1YmVkMDdjOTcyMzAzNWY3MTljNzQ4M2Q3YzIzMjg3M2E5ZjZmN2QyMjQ1Y2E3MjVhNjVlMTQ5ZGRiZjBkYjgiLCJpYXQiOjE3NjE2MzM3OTIuNDI5OTk5LCJuYmYiOjE3NjE2MzM3OTIuNDMwMDAzLCJleHAiOjE3OTMxNjk3OTIuNDIyNjY5LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.SutcB4SMDdEzgfDZ1lnzGDjY4gf1aSwNJMGFnKF3RXWGbB4MhP6R8wRLCa2wtCMZ1egUQc1LxPMq2S9P-hfCeh4stj4W-zlWf0xOfvswvWVos14aP5RBTjbZ8Rc3PrmAwiFvnzu7PpRtqy8hGF_h1nt2dJrZpT4k0CIuvpUK8afLBKYfpr0i1NOx1awb2Z6Uo5YfJRQrB7y4wrs8TEsnbuAIdC4JG6-9Oi_TPwZ42SGatw4UEUvm09I3SjKGjZRDCOMJZLbSc1O4C6B53aK9hNQKCjnwsSXWc37h-cA6lKg-DSfm6K1usg0yHeAsE-2uya2_b8_TNq3LN4Mb04S820FmpnP0RqtDoPeoqBUSTacd0bSINimYpNv8NyiaO0D6k0J3HzaNd5MmwORyHpTNnVEaM8l0O5iyI-UIa3bPaOBnltamiAydE2EmUA9pfRQy3HWd6yZnxfuITM0THdj73ju1Qh3D0WVP7aUFR-3XUbp5qVflBZkiBe0klClG94ubWNFMX6vebixLQ21KsDvEDLj2Xy0hoLY-g6sm33l14NwbSiUgZ0VQI_3WbOzSpUvU0sprU7ozX7y7-nwjIXjshOQ5ymktZUMCN7LyCbvgX-qcGyxbS8C9JN9BhmvhagIBatDpPZw75arXlksHzxKnytbLG3BVxFHXxkp9jSBqW2s"
    fun setOnEditClickListener(listener: (StockProductData) -> Unit) {
        onEditClickListener = listener
    }

    fun setOnDeleteClickListener(listener: (StockProductData) -> Unit) {
        onDeleteClickListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.PName)
        val editBtn: ShapeableImageView = itemView.findViewById(R.id.PEditOption)
        val deleteBtn: ShapeableImageView = itemView.findViewById(R.id.PDeleteProduct)
        val statusToggle: LabeledSwitch = itemView.findViewById(R.id.Status_on_off)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false) // your product layout
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.productName ?: "N/A"


        // Toggle listener
        // Reset toggle listener to prevent multiple triggers
        holder.statusToggle.setOnToggledListener(null)
        holder.statusToggle.isOn = item.productStatus == 1

        // Toggle listener
        holder.statusToggle.setOnToggledListener { _, isOn ->
            val newStatusInt = if (isOn) 1 else 0

            // Prepare API input
            val input = StatusUpdateInput(
                product_id = item.productId?.toString() ?: "0",
                product_variation_id = item.productVariationId?.toString() ?: "0",
                product_status = newStatusInt.toString(),
                status = 1
            )
            // API call
            ApiClient.instance.productStatusUpdate(jwtToken, input)
                .enqueue(object : Callback<StatusResponse> {
                    override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {


                        if (response.isSuccessful && response.body()?.status == true) {
                            item.productStatus = newStatusInt
                        } else {
                            holder.statusToggle.isOn = !isOn // revert visually
                            Toast.makeText(holder.itemView.context, "Failed to update status", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                        holder.statusToggle.isOn = !isOn // revert visually
                        Toast.makeText(holder.itemView.context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // Handle edit click
        holder.editBtn.setOnClickListener {
            Log.d("ItemsAdapter", "Edit clicked: ${item.productId}")
            onEditClickListener?.invoke(item)
        }

        // Handle delete click
        holder.deleteBtn.setOnClickListener {
            onDeleteClickListener?.invoke(item)
        }


    }
}
