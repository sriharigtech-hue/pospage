package com.example.apitest.cartScreen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.CartItem

class CartAdapter(
    private val items: MutableList<CartItem>,
    private val onItemUpdated: (CartItem) -> Unit,
    private val onItemDeleted: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.product_name)
        val qty: TextView = itemView.findViewById(R.id.txt_quantity)
        val price: TextView = itemView.findViewById(R.id.sub_total_product_amount)
        val btnIncrease: TextView = itemView.findViewById(R.id.btn_increase)
        val btnDecrease: TextView = itemView.findViewById(R.id.btn_decrease)
        val delete: View = itemView.findViewById(R.id.delete)
        val addToBag: View = itemView.findViewById(R.id.add_to_bag)
        val quantityLayout: View = itemView.findViewById(R.id.quantity_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_product, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]

        holder.name.text = item.name
        holder.qty.text = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString()
        holder.price.text = "₹%.2f".format(item.price * item.quantity)

        // Show quantity layout, hide add button
        holder.quantityLayout.visibility = View.VISIBLE
        holder.addToBag.visibility = View.GONE

        // Increase quantity
        holder.btnIncrease.setOnClickListener {
            item.quantity += 1
            holder.qty.text = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString()
            holder.price.text = "₹%.2f".format(item.price * item.quantity)
            onItemUpdated(item)
        }

        // Decrease quantity
        holder.btnDecrease.setOnClickListener {
            if (item.quantity > 1) {
                item.quantity -= 1
                holder.qty.text = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString()
                holder.price.text = "₹%.2f".format(item.price * item.quantity)
                onItemUpdated(item)
            }
        }

        // Delete item
        holder.delete.setOnClickListener {
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, items.size)
            onItemDeleted(item)
        }
    }

    override fun getItemCount(): Int = items.size
}
