package com.example.apitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.StockProductData

class ItemsAdapter(
    private val items: MutableList<StockProductData>
) : RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.PName)
//        val price: TextView = itemView.findViewById(R.id.PPrice)
//        val image: ImageView = itemView.findViewById(R.id.PImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false) // your product layout
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Product name
        holder.name.text = item.productName ?: "N/A"

//        // Show first price if available
//        val firstPrice = item.productPrice?.firstOrNull()?.productPrice ?: "0.00"
//        holder.price.text = "₹ $firstPrice"

        // Load product image if available
//        Glide.with(holder.itemView.context)
//            .load(item.productImage)
//            .placeholder(R.drawable.placeholder) // add a placeholder in drawable
//            .into(holder.image)
    }
}
