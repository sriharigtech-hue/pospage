package com.example.apitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.POSFeatures

class MainFeaturesAdapter(private val list: List<POSFeatures>) :
    RecyclerView.Adapter<MainFeaturesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTitle: TextView = itemView.findViewById(R.id.plan_name)
        val subList: RecyclerView = itemView.findViewById(R.id.posSubfeaturelist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plan_features, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.txtTitle.text = item.title ?: ""

        holder.subList.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.subList.adapter = SubAdapter(item.features ?: emptyList())
    }

    override fun getItemCount(): Int = list.size
}