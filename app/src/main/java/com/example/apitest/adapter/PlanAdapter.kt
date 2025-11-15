package com.example.apitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.PlanDetails

class PlanAdapter(
    private var planList: ArrayList<PlanDetails>,
    private val onClick: (PlanDetails) -> Unit
) : RecyclerView.Adapter<PlanAdapter.PlanViewHolder>() {

    var activePlanPosition = -1
    var userSelectedPosition = -1

    inner class PlanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout: View = view.findViewById(R.id.layout)

        val planName: AppCompatTextView = view.findViewById(R.id.plan_name)
        val planPrice: AppCompatTextView = view.findViewById(R.id.plan_price)
        val forDetails: AppCompatTextView = view.findViewById(R.id.for_details)

        val description: AppCompatTextView = view.findViewById(R.id.description)
        val categoryCounts: AppCompatTextView = view.findViewById(R.id.categoryCounts)
        val itemCounts: AppCompatTextView = view.findViewById(R.id.itemCounts)
        val orderCounts: AppCompatTextView = view.findViewById(R.id.orderCounts)

        val expiryDate: AppCompatTextView = view.findViewById(R.id.expiryDate)
        val daysCount: AppCompatTextView = view.findViewById(R.id.daysCount)

        val description1: AppCompatTextView = view.findViewById(R.id.description1)
        val categoryCounts1: AppCompatTextView = view.findViewById(R.id.categoryCounts1)
        val itemCounts1: AppCompatTextView = view.findViewById(R.id.itemCounts1)
        val orderCounts1: AppCompatTextView = view.findViewById(R.id.orderCounts1)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plan_details, parent, false)
        return PlanViewHolder(view)
    }


    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val plan = planList[position]

        val isActive = (position == activePlanPosition)
        val isUserSelected = (position == userSelectedPosition)

        if (isActive || isUserSelected) {
            holder.layout.setBackgroundResource(R.drawable.gradient_btn)
            setTextColor(holder, R.color.white)
        } else {
            holder.layout.setBackgroundResource(R.color.white)
            setDefaultTextColor(holder)
        }

        holder.layout.setOnClickListener {
            userSelectedPosition = holder.adapterPosition
            notifyDataSetChanged()
            onClick(planList[userSelectedPosition])
        }

        holder.planName.text = plan.name ?: "-"
        holder.planPrice.text = "₹ ${plan.ginex_six_month_prize ?: 0}"
        holder.forDetails.text = plan.validity_content ?: "-"
        holder.description.text = plan.description ?: "-"
        holder.categoryCounts.text = plan.category_count?.toString() ?: "-"
        holder.itemCounts.text = plan.product_count?.toString() ?: "-"
        holder.orderCounts.text = plan.order_count?.toString() ?: "-"

        // Show expiry date only for active plan
        val expiryLayout = holder.itemView.findViewById<View>(R.id.planExpiryLayout)
        if (isActive) {
            expiryLayout.visibility = View.VISIBLE
            holder.expiryDate.text = "Expiry: ${plan.expiry_date ?: "-"}"
            holder.daysCount.text = "${plan.days_count ?: 0} Days Left"
        } else {
            expiryLayout.visibility = View.GONE
        }
    }


    override fun getItemCount(): Int = planList.size


    fun updateList(newList: List<PlanDetails>) {
        planList.clear()
        planList.addAll(newList)
        notifyDataSetChanged()
    }


    private fun setTextColor(holder: PlanViewHolder, colorRes: Int) {
        val c = ContextCompat.getColor(holder.itemView.context, colorRes)
        holder.planName.setTextColor(c)
        holder.planPrice.setTextColor(c)
        holder.forDetails.setTextColor(c)
        holder.description.setTextColor(c)
        holder.categoryCounts.setTextColor(c)
        holder.itemCounts.setTextColor(c)
        holder.orderCounts.setTextColor(c)
        holder.expiryDate.setTextColor(c)
        holder.daysCount.setTextColor(c)
        holder.description1.setTextColor(c)
        holder.categoryCounts1.setTextColor(c)
        holder.itemCounts1.setTextColor(c)
        holder.orderCounts1.setTextColor(c)
    }

    private fun setDefaultTextColor(holder: PlanViewHolder) {
        val ctx = holder.itemView.context
        holder.planName.setTextColor(ContextCompat.getColor(ctx, R.color.black))
        holder.planPrice.setTextColor(ContextCompat.getColor(ctx, R.color.black))
        holder.forDetails.setTextColor(ContextCompat.getColor(ctx, R.color.gray494949))
        holder.description.setTextColor(ContextCompat.getColor(ctx, R.color.black))
        holder.categoryCounts.setTextColor(ContextCompat.getColor(ctx, R.color.black))
        holder.itemCounts.setTextColor(ContextCompat.getColor(ctx, R.color.black))
        holder.orderCounts.setTextColor(ContextCompat.getColor(ctx, R.color.black))
        holder.expiryDate.setTextColor(ContextCompat.getColor(ctx, R.color.black))
        holder.daysCount.setTextColor(ContextCompat.getColor(ctx, R.color.gray494949))
        holder.description1.setTextColor(ContextCompat.getColor(ctx, R.color.gray494949))
        holder.categoryCounts1.setTextColor(ContextCompat.getColor(ctx, R.color.gray494949))
        holder.itemCounts1.setTextColor(ContextCompat.getColor(ctx, R.color.gray494949))
        holder.orderCounts1.setTextColor(ContextCompat.getColor(ctx, R.color.gray494949))
    }
}
