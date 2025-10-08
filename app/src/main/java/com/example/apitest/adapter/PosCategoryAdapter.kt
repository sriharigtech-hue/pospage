package com.example.apitest.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.CategoryList


class PosCategoryAdapter(
    private val categories: List<CategoryList>,
    private val onItemClick: (CategoryList, Int) -> Unit
) :RecyclerView.Adapter<PosCategoryAdapter.CategoryViewHolder>()
{

    private var selectedPosition = 0

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: LinearLayout = itemView.findViewById(R.id.itemParentLayout)
        val categoryText: TextView = itemView.findViewById(R.id.ItemSideBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryText.text = category.categoryName ?: "Category"

        // Highlight selected
        if (position == selectedPosition) {
            holder.container.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.blue))
            holder.categoryText.setTextColor(Color.WHITE)
        } else {
            holder.container.setBackgroundColor(Color.TRANSPARENT)
            holder.categoryText.setTextColor(Color.BLACK)
        }

        holder.itemView.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener

            val previousPosition = selectedPosition
            selectedPosition = currentPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(currentPosition)
            onItemClick(categories[currentPosition], currentPosition)
        }
    }
    fun selectCategory(index: Int) {
        if (index in categories.indices) {
            val previous = selectedPosition
            selectedPosition = index
            notifyItemChanged(previous)
            notifyItemChanged(selectedPosition)
        }
    }



    override fun getItemCount(): Int = categories.size
}
