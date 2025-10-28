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

    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZTRjY2JlNDBmYWFiZmE5YTg1YmVkMDdjOTcyMzAzNWY3MTljNzQ4M2Q3YzIzMjg3M2E5ZjZmN2QyMjQ1Y2E3MjVhNjVlMTQ5ZGRiZjBkYjgiLCJpYXQiOjE3NjE2MzM3OTIuNDI5OTk5LCJuYmYiOjE3NjE2MzM3OTIuNDMwMDAzLCJleHAiOjE3OTMxNjk3OTIuNDIyNjY5LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.SutcB4SMDdEzgfDZ1lnzGDjY4gf1aSwNJMGFnKF3RXWGbB4MhP6R8wRLCa2wtCMZ1egUQc1LxPMq2S9P-hfCeh4stj4W-zlWf0xOfvswvWVos14aP5RBTjbZ8Rc3PrmAwiFvnzu7PpRtqy8hGF_h1nt2dJrZpT4k0CIuvpUK8afLBKYfpr0i1NOx1awb2Z6Uo5YfJRQrB7y4wrs8TEsnbuAIdC4JG6-9Oi_TPwZ42SGatw4UEUvm09I3SjKGjZRDCOMJZLbSc1O4C6B53aK9hNQKCjnwsSXWc37h-cA6lKg-DSfm6K1usg0yHeAsE-2uya2_b8_TNq3LN4Mb04S820FmpnP0RqtDoPeoqBUSTacd0bSINimYpNv8NyiaO0D6k0J3HzaNd5MmwORyHpTNnVEaM8l0O5iyI-UIa3bPaOBnltamiAydE2EmUA9pfRQy3HWd6yZnxfuITM0THdj73ju1Qh3D0WVP7aUFR-3XUbp5qVflBZkiBe0klClG94ubWNFMX6vebixLQ21KsDvEDLj2Xy0hoLY-g6sm33l14NwbSiUgZ0VQI_3WbOzSpUvU0sprU7ozX7y7-nwjIXjshOQ5ymktZUMCN7LyCbvgX-qcGyxbS8C9JN9BhmvhagIBatDpPZw75arXlksHzxKnytbLG3BVxFHXxkp9jSBqW2s"

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
