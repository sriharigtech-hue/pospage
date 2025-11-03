package com.example.apitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.ProductReportList

class ProductReportAdapter(private val list: List<ProductReportList>) :
    RecyclerView.Adapter<ProductReportAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.product_name)
        val variation: TextView = view.findViewById(R.id.variation)
        val saleCount: TextView = view.findViewById(R.id.product_number)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_report, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.productName.text = item.product_name ?: ""
        holder.variation.text = item.product_variation_name ?: ""
        holder.saleCount.text = item.sale_count ?: "0"
    }

    override fun getItemCount(): Int = list.size
}
