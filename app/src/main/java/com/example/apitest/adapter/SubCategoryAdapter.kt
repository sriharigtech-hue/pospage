package com.example.apitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.SubCategoryDetails


class SubCategoryAdapter(
    private val subCategoryList: List<SubCategoryDetails>
) : RecyclerView.Adapter<SubCategoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(subCategory: SubCategoryDetails) {
            val scNameTextView = itemView.findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.ScName)
            scNameTextView.text = subCategory.subcategoryName ?: "N/A"
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subcategory, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(subCategoryList[position])
    }

    override fun getItemCount(): Int = subCategoryList.size
}
