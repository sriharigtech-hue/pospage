package com.example.apitest.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.apitest.R
import com.example.apitest.dataModel.Category
import com.makeramen.roundedimageview.RoundedImageView

class SidebarCategoryAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
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
        holder.name.text = category.category_name

        if (!category.category_image.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(category.category_image)
                .placeholder(R.mipmap.ic_launcher) // or your placeholder
                .error(R.mipmap.ic_launcher)
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.mipmap.ic_launcher)
        }
        // 👇 call back to fragment
        holder.itemView.setOnClickListener {
            onCategoryClick(category)
        }
    }
}
