package com.example.apitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.UnitList
import androidx.appcompat.widget.AppCompatTextView
import java.util.*

class UnitAdapter(
    private var unitList: MutableList<UnitList>,
    private val onEditClick: (UnitList) -> Unit,
    private val onDeleteClick: (UnitList) -> Unit
) : RecyclerView.Adapter<UnitAdapter.UnitViewHolder>() {



    private var fullList: List<UnitList> = ArrayList(unitList) // backup for filtering

    inner class UnitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val unitNameText: AppCompatTextView = itemView.findViewById(R.id.product_name)
        private val descriptionText: AppCompatTextView = itemView.findViewById(R.id.description)
        private val quantityText: AppCompatTextView = itemView.findViewById(R.id.quantity)
        private val editButton: View = itemView.findViewById(R.id.UnitEditOption)
        private val deleteButton: View = itemView.findViewById(R.id.UnitDeleteProduct)

        fun bind(item: UnitList) {
            unitNameText.text = item.unitName ?: ""
            descriptionText.visibility = View.GONE
            quantityText.visibility = View.GONE
            editButton.setOnClickListener { onEditClick(item) }
            deleteButton.setOnClickListener { onDeleteClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_unit, parent, false)
        return UnitViewHolder(view)
    }

    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        holder.bind(unitList[position])
    }

    override fun getItemCount(): Int = unitList.size

    // Filter function for SearchView
    fun filter(query: String) {
        val lowerCaseQuery = query.lowercase(Locale.getDefault())
        unitList = if (lowerCaseQuery.isEmpty()) {
            fullList.toMutableList()
        } else {
            fullList.filter {
                it.unitName?.lowercase(Locale.getDefault())?.contains(lowerCaseQuery) == true
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    // Update data if needed
    fun updateData(newList: List<UnitList>) {
        unitList = newList.toMutableList()
        fullList = ArrayList(newList)
        notifyDataSetChanged()
    }
}
