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
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZGE0YjJmNjlmZGJkNjMwMDMyNGE3MWNkZWRhMDI2ZWI2YTIwMGM4NWIyNTI2MTNjOTZhZGIyMDA2MTE3YjMxMGI0MTFjYjczNzNmZmNlZDAiLCJpYXQiOjE3NjAzMjkzNTkuNDc3MTA1LCJuYmYiOjE3NjAzMjkzNTkuNDc3MTA4LCJleHAiOjE3OTE4NjUzNTkuNDcyNjI1LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.C3ySWdDX7BRHm4qzwWFZZofL_DEx3C2Qjy7iEUWxy9GdrL8OJS7m7Kk_Oe4HtFaT7DvPMEWE_c9kIC8RalMXflXTvPGKkfsw7yxdVxZOKSE20UNZiSbScAdvx3RxAz-XoHK4wJr7wepspLad5y5KCv4RyPXAJl8sIjFELfiCMoxt1CiYGp5_GhsOjbMeSLWSBoDwd3H4MLNvUyU2KN2zhvQaRRUh4T-L11mZgmd_8A8kWZbp_bO6AK-3hGHFGd7VaT2Xqoi4asmn0ABlxusVYWG6hw9UhnU-_uxOVFQLAHog-WKfbahCwfkssXtK07wMpk-ZGHfRn7ujbkrMAX5gNgNkcNQZPRMkUSrokHylEJXKC7UOAgUiK8fy32bIlmFuMQE9hTuuQjHWJ8hdEqtPaXVIcc1oXURtZhCWTp2APH9RE4_L41NYStog_bVMdXwRO_a6QEg_ex0moqxwtRZKivnIF4DKm6WLj45X0FLj-F7HTlZ-eoc9j3w_dVaVyhhxEKUiTyQSJ_AwVKMTbAUmxvWY3OnoIAmu4WYrbC4T4tA2cWoB9yXKna8Yfbil_vC46tLZweGF7RRZR2MPT16q-iCzKG73JqAMphV4NO7b-bMk6mhvgz8TR0_YUewsPg2CVvgdvEmnV4DE4znhnwiLMniN0kPGzF5pindkKTVNDb8"

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
