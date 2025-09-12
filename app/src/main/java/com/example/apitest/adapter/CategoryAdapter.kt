// CategoryAdapter.kt
package com.example.apitest.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.apitest.EditCategoryActivity
import com.example.apitest.R
import com.example.apitest.dataModel.Category

class CategoryAdapter(private val categories: MutableList<Category>) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var onEditClickListener: ((Category) -> Unit)? = null
    private var filteredList: MutableList<Category> = categories.toMutableList()
    private var onDeleteClickListener: ((Category) -> Unit)? = null

    fun setOnEditClickListener(listener: (Category) -> Unit) {
        onEditClickListener = listener
    }




    fun setOnDeleteClickListener(listener: (Category) -> Unit) {
        onDeleteClickListener = listener
    }

    fun updateCategory(updatedCategory: Category) {
        // Update in main list
        val index = categories.indexOfFirst { it.category_id == updatedCategory.category_id }
        if (index != -1) categories[index] = updatedCategory

        // Update in filtered list
        val fIndex = filteredList.indexOfFirst { it.category_id == updatedCategory.category_id }
        if (fIndex != -1) {
            filteredList[fIndex] = updatedCategory
            notifyItemChanged(fIndex)
        } else {
            // If filtered list does not contain it (maybe due to filter), reset filtered list
            filteredList = categories.toMutableList()
            notifyDataSetChanged()
        }
    }


    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvName)
        val editButton: LinearLayout = itemView.findViewById(R.id.editOption)
        val deleteButton: LinearLayout = itemView.findViewById(R.id.deleteProduct)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = filteredList[position]
        holder.name.text = category.category_name
//        Glide.with(holder.itemView.context)
//            .load(category.category_image)
//            .into(holder.image)
        // Delegate edit click back to fragment
        holder.editButton.setOnClickListener {
            onEditClickListener?.invoke(category)
        }
        holder.deleteButton.setOnClickListener {
            onDeleteClickListener?.invoke(category)
        }

    }

    override fun getItemCount(): Int = filteredList.size
    fun removeCategory(category: Category) {
        val index = filteredList.indexOfFirst { it.category_id == category.category_id }
        if (index != -1) {
            filteredList.removeAt(index)
            categories.removeIf { it.category_id == category.category_id }
            notifyItemRemoved(index)
        }
    }

    fun updateData(newList: List<Category>) {
        categories.clear()
        categories.addAll(newList)
        filteredList = categories.toMutableList()
        notifyDataSetChanged()
    }
    // Filter logic
    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            categories.toMutableList()
        } else {
            categories.filter { it.category_name.contains(query, ignoreCase = true) }.toMutableList()
        }
        notifyDataSetChanged()
    }

}

