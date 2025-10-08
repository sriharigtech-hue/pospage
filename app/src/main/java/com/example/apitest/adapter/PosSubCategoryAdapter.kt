package com.example.apitest.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.SubCategoryDetails
import com.google.android.material.card.MaterialCardView

class PosSubCategoryAdapter(
    private val subCategories: List<SubCategoryDetails>,
    private val onItemClick: (SubCategoryDetails, Int) -> Unit
) : RecyclerView.Adapter<PosSubCategoryAdapter.SubCategoryViewHolder>() {

    private var selectedPosition = -1

    inner class SubCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardSubCategory: MaterialCardView = itemView.findViewById(R.id.cardSubCategory)
        val txtSubCategory: TextView = itemView.findViewById(R.id.txtSubCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubCategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sub_category_horizontal, parent, false)
        return SubCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubCategoryViewHolder, position: Int) {
        val subCategory = subCategories[position]
        holder.txtSubCategory.text = subCategory.subcategoryName ?: "SubCategory"

        // Highlight selected
        if (position == selectedPosition) {
            holder.cardSubCategory.setCardBackgroundColor(holder.itemView.context.getColor(R.color.blue243757))
            holder.txtSubCategory.setTextColor(Color.WHITE)
        } else {
            holder.cardSubCategory.setCardBackgroundColor(holder.itemView.context.getColor(R.color.blueF1F4FF))
            holder.txtSubCategory.setTextColor(holder.itemView.context.getColor(R.color.blue243757))
        }

        holder.cardSubCategory.setOnClickListener {
            val previous = selectedPosition
            selectedPosition = holder.bindingAdapterPosition
            notifyItemChanged(previous)
            notifyItemChanged(selectedPosition)
            onItemClick(subCategory, position)
        }
    }


    override fun getItemCount(): Int = subCategories.size

    // Reset selection when category changes
    fun resetSelection() {
        val previous = selectedPosition
        selectedPosition = -1
        if (previous != -1) notifyItemChanged(previous)
    }
    fun setSelectedIndex(index: Int) {
        if (index in subCategories.indices) {
            val previous = selectedPosition
            selectedPosition = index
            notifyItemChanged(previous)
            notifyItemChanged(selectedPosition)
        }
    }

}