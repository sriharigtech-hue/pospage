package com.example.apitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.apitest.R
import com.example.apitest.dataModel.CategoryList
import com.makeramen.roundedimageview.RoundedImageView

class SidebarCategoryAdapter(
    private val categories: List<CategoryList>,
    private val onCategoryClick: (CategoryList) -> Unit
) : RecyclerView.Adapter<SidebarCategoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: RoundedImageView = itemView.findViewById(R.id.imageView)
        val name: TextView = itemView.findViewById(R.id.ItemSideBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = categories.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.name.text = category.categoryName

        if (!category.categoryImage.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(category.categoryImage)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.mipmap.ic_launcher)
        }

        holder.itemView.setOnClickListener {
            onCategoryClick(category)
        }
    }
}
