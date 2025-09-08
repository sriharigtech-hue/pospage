// CategoryAdapter.kt
package com.example.apitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


import com.example.apitest.R

import com.example.apitest.dataModel.Category

class CategoryAdapter(private val items: List<Category>) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvName)
        val image: ImageView = itemView.findViewById(R.id.ivImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = items[position]
        holder.name.text = category.category_name
        Glide.with(holder.itemView.context)
            .load(category.category_image)
            .into(holder.image)
    }

    override fun getItemCount(): Int = items.size
}
