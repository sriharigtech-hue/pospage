package com.example.apitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.ReportList

class OrderListReportAdapter(private val list: List<ReportList>) :
    RecyclerView.Adapter<OrderListReportAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderId: TextView = view.findViewById(R.id.order_id)
        val bookingTime: TextView = view.findViewById(R.id.booking_date_time)
        val payment: TextView = view.findViewById(R.id.payment)
        val amount: TextView = view.findViewById(R.id.amount)
        val empName: TextView = view.findViewById(R.id.emp_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.orderId.text = item.orderNumber ?: "-"
        holder.bookingTime.text = item.time ?: "-"
        holder.payment.text = item.paymentMode ?: "-"
        holder.amount.text = "₹ ${item.amount ?: "0.00"}"
        holder.empName.text = item.emp_name ?: "-"
    }

    override fun getItemCount() = list.size
}
