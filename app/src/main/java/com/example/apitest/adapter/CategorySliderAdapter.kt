package com.example.apitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.Category
import com.makeramen.roundedimageview.RoundedImageView
import com.bumptech.glide.Glide

class CategorySidebarAdapter(
    private val categories: List<Category>,
    private val onItemClick: (Category) -> Unit
) : RecyclerView.Adapter<CategorySidebarAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: RoundedImageView = itemView.findViewById(R.id.imageView)
        val name: TextView = itemView.findViewById(R.id.ItemSideBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]

        holder.name.text = category.category_name

        // Load image using Glide
        Glide.with(holder.itemView.context)
            .load(category.category_image)
            .placeholder(R.mipmap.ic_launcher)
            .into(holder.image)

        holder.itemView.setOnClickListener {
            onItemClick(category)
        }
    }

    override fun getItemCount(): Int = categories.size
}
