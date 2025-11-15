package com.example.apitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.FeaturesList

class SubAdapter(private val list: List<FeaturesList>) :
    RecyclerView.Adapter<SubAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subTitle: TextView = itemView.findViewById(R.id.plan_name)
        val description: TextView = itemView.findViewById(R.id.plan_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plan_sub_features, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.subTitle.text = item.sub_title ?: ""
        holder.description.text = item.description ?: ""
    }

    override fun getItemCount(): Int = list.size
}
