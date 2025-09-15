package com.example.apitest.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.SubCategoryDetails

class SubCategoryAdapter(
    private val subCategoryList: MutableList<SubCategoryDetails>
) : RecyclerView.Adapter<SubCategoryAdapter.ViewHolder>() {

    private var filteredList: MutableList<SubCategoryDetails> = subCategoryList.toMutableList()

    // Edit & Delete listeners
    private var editClickListener: ((SubCategoryDetails) -> Unit)? = null
    private var deleteClickListener: ((SubCategoryDetails) -> Unit)? = null

    fun setOnEditClickListener(listener: (SubCategoryDetails) -> Unit) {
        editClickListener = listener
    }

    fun setOnDeleteClickListener(listener: (SubCategoryDetails) -> Unit) {
        deleteClickListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val scNameTextView =
            itemView.findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.ScName)
        private val editButton = itemView.findViewById<ImageView>(R.id.ScEditOption)
        private val deleteButton = itemView.findViewById<ImageView>(R.id.ScDeleteProduct)

        fun bind(subCategory: SubCategoryDetails) {
            scNameTextView.text = subCategory.subcategoryName ?: "N/A"

            editButton.setOnClickListener { editClickListener?.invoke(subCategory) }
            deleteButton.setOnClickListener { deleteClickListener?.invoke(subCategory) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_subcategory, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    override fun getItemCount(): Int = filteredList.size

    // ✅ Refresh list from API
    fun setData(newList: List<SubCategoryDetails>) {
        subCategoryList.clear()
        subCategoryList.addAll(newList)
        filteredList = newList.toMutableList()
        notifyDataSetChanged()
    }
    // 🔎 Filter function
    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            subCategoryList.toMutableList()
        } else {
            subCategoryList.filter {
                it.subcategoryName?.contains(query, ignoreCase = true) == true
            }.toMutableList()
        }
        notifyDataSetChanged()
    }
}

