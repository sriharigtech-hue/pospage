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

    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiNmZiMTgzMGFlYzFkNzUzOTdiNDBjMmE3NWJhOWRmZWU4MTU3YWMxZmZlMTg2NGYxZWMwOTYyYzA1NjE1YThjNTlkZjg3MjVjNDlmNmJmZTYiLCJpYXQiOjE3NTg2MTM2MzguMzQyMTUzLCJuYmYiOjE3NTg2MTM2MzguMzQyMTU3LCJleHAiOjE3OTAxNDk2MzguMzM2ODI2LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.QiPgcG55zwKdr_U471IaYnBQkUk437w5vRrmjxWos1zRADMFKLuovtJ8IqoTzbkQ1GMWE0fr6c22cdBq9Eq4PlY71A8ocqua6rhGKTS5h7ziYjA8y_KNRSmvWeSfrYF59FCv_sl_Yi8mci_Gl6lzLV5yzf-gQOcmxQ0m4NifHTxCZZEOXloSS0V2KtiECV1MwATuDGLGv7QYAwte7XEIZiCFjTJUjGGKdcXHGdCPXnsnlSSzyYUCuljabM4Of3dnP6QV6YVEuRkAiSn7HvyqgNpi34ux4lsVXFMWy1qnbI-VI_fo2Vcf5uZa8B4KBYVNT7YkV2_KcxEsdZ-ZhRzGbm6erYvUuwWUTLj8DHRUHfH-s1sOO4j3u8SQeVV47OfXB6Wo-CghPzuMQBtTvoQe2_zgAV9QrCS-xsk-uJxHLxp_dao9igrzB6fskUGshJ70IKLatiF3IDXuyVLZNqOJUblUYpoyvKuj4dBa-BZUnS35m9jm5Rz5XCkeIMIm26VTvwiNcuTg0cAXhpe97yZ_Ir0FV7-8YNQCPTAtCrC4IA0r6cuLTkWjZWa-gokQKbMWble8R9VoP-OIm0zFbISJUBtI-B0cif3oxTwr7m12jubPutnFl_HZyREBOticvsTf6q48cKUxNSYeyOW3aMdbZ584yRv95UYKUou0k0LbezM"


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
