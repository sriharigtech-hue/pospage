package com.example.apitest.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.Category
import com.example.apitest.dataModel.CategoryStatusUpdateInput
import com.example.apitest.dataModel.StatusResponse
import com.example.apitest.network.ApiClient
import com.github.angads25.toggle.widget.LabeledSwitch
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.moshi.Moshi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoryAdapter(private val categories: MutableList<Category>) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZGE0YjJmNjlmZGJkNjMwMDMyNGE3MWNkZWRhMDI2ZWI2YTIwMGM4NWIyNTI2MTNjOTZhZGIyMDA2MTE3YjMxMGI0MTFjYjczNzNmZmNlZDAiLCJpYXQiOjE3NjAzMjkzNTkuNDc3MTA1LCJuYmYiOjE3NjAzMjkzNTkuNDc3MTA4LCJleHAiOjE3OTE4NjUzNTkuNDcyNjI1LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.C3ySWdDX7BRHm4qzwWFZZofL_DEx3C2Qjy7iEUWxy9GdrL8OJS7m7Kk_Oe4HtFaT7DvPMEWE_c9kIC8RalMXflXTvPGKkfsw7yxdVxZOKSE20UNZiSbScAdvx3RxAz-XoHK4wJr7wepspLad5y5KCv4RyPXAJl8sIjFELfiCMoxt1CiYGp5_GhsOjbMeSLWSBoDwd3H4MLNvUyU2KN2zhvQaRRUh4T-L11mZgmd_8A8kWZbp_bO6AK-3hGHFGd7VaT2Xqoi4asmn0ABlxusVYWG6hw9UhnU-_uxOVFQLAHog-WKfbahCwfkssXtK07wMpk-ZGHfRn7ujbkrMAX5gNgNkcNQZPRMkUSrokHylEJXKC7UOAgUiK8fy32bIlmFuMQE9hTuuQjHWJ8hdEqtPaXVIcc1oXURtZhCWTp2APH9RE4_L41NYStog_bVMdXwRO_a6QEg_ex0moqxwtRZKivnIF4DKm6WLj45X0FLj-F7HTlZ-eoc9j3w_dVaVyhhxEKUiTyQSJ_AwVKMTbAUmxvWY3OnoIAmu4WYrbC4T4tA2cWoB9yXKna8Yfbil_vC46tLZweGF7RRZR2MPT16q-iCzKG73JqAMphV4NO7b-bMk6mhvgz8TR0_YUewsPg2CVvgdvEmnV4DE4znhnwiLMniN0kPGzF5pindkKTVNDb8"


    private var onEditClickListener: ((Category) -> Unit)? = null
    private var onDeleteClickListener: ((Category) -> Unit)? = null
    private var filteredList: MutableList<Category> = categories.toMutableList()

    fun setOnEditClickListener(listener: (Category) -> Unit) {
        onEditClickListener = listener
    }

    fun setOnDeleteClickListener(listener: (Category) -> Unit) {
        onDeleteClickListener = listener
    }

    fun updateCategory(updatedCategory: Category) {
        val index = categories.indexOfFirst { it.category_id == updatedCategory.category_id }
        if (index != -1) categories[index] = updatedCategory

        val fIndex = filteredList.indexOfFirst { it.category_id == updatedCategory.category_id }
        if (fIndex != -1) {
            filteredList[fIndex] = updatedCategory
            notifyItemChanged(fIndex)
        } else {
            filteredList = categories.toMutableList()
            notifyDataSetChanged()
        }
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvName)
        val editButton: ShapeableImageView = itemView.findViewById(R.id.EditOption)
        val deleteButton: ShapeableImageView = itemView.findViewById(R.id.deleteProduct)
        val statusSwitch: LabeledSwitch = itemView.findViewById(R.id.CpOn_off_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = filteredList[holder.bindingAdapterPosition]
        holder.name.text = category.category_name

        //  prevent double-trigger
        holder.statusSwitch.setOnToggledListener(null)

        // Set correct initial state
        holder.statusSwitch.isOn = category.category_status == 1

        // Re-attach listener
        holder.statusSwitch.setOnToggledListener { _, isOn ->
            val newStatus = if (isOn) 1 else 0
            val input = CategoryStatusUpdateInput(
                category_id = category.category_id,
                category_status = newStatus,
                status = 1
            )

            val jsonAdapter = Moshi.Builder().build().adapter(CategoryStatusUpdateInput::class.java)
            Log.d("CategoryAdapter", "JSON: ${jsonAdapter.toJson(input)}")
            Log.d("CategoryAdapter", "Input: $input")

            ApiClient.instance.categoryStatusUpdate(jwtToken, input)
                .enqueue(object : Callback<StatusResponse> {
                    override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                        Log.d("CategoryAdapter", "Response: ${response.body()}")

                        if (response.isSuccessful && response.body()?.status == true) {
                            category.category_status = newStatus
                            notifyItemChanged(holder.bindingAdapterPosition)
                            Toast.makeText(holder.itemView.context, "Status updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            holder.statusSwitch.isOn = !isOn
                            Toast.makeText(holder.itemView.context,
                                response.body()?.message ?: "Update failed",
                                Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                        holder.statusSwitch.isOn = !isOn
                        Toast.makeText(holder.itemView.context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // Edit & delete listeners
        holder.editButton.setOnClickListener { onEditClickListener?.invoke(category) }
        holder.deleteButton.setOnClickListener { onDeleteClickListener?.invoke(category) }
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

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            categories.toMutableList()
        } else {
            categories.filter { it.category_name.contains(query, ignoreCase = true) }
                .toMutableList()
        }
        notifyDataSetChanged()
    }
}
