package com.example.apitest.cartScreen

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.CartItem
import kotlin.math.max

class CartAdapter(
    private val items: MutableList<CartItem>,
    private val onItemUpdated: (CartItem) -> Unit,
    private val onItemDeleted: (CartItem) -> Unit,
    private val discountType: String? = null
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.product_name)
        val qty: TextView = itemView.findViewById(R.id.txt_quantity)
        val price: TextView = itemView.findViewById(R.id.sub_total_product_amount)
        val offerPrice: TextView = itemView.findViewById(R.id.offer_product_amount)
        val btnIncrease: TextView = itemView.findViewById(R.id.btn_increase)
        val btnDecrease: TextView = itemView.findViewById(R.id.btn_decrease)
        val delete: View = itemView.findViewById(R.id.delete)
        val addToBag: View = itemView.findViewById(R.id.add_to_bag)
        val quantityLayout: View = itemView.findViewById(R.id.quantity_layout)
        val discountLayout: View? = itemView.findViewById(R.id.product_name_layout)
        val btnAddDiscount: View? = itemView.findViewById(R.id.add_discount)
        val txtOffer: TextView? = itemView.findViewById(R.id.product_offer)
        val btnRemoveDiscount: TextView? = itemView.findViewById(R.id.removeCoupon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_product, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context
        val totalOriginal = item.originalPrice.takeIf { it > 0 } ?: item.price
        val total = totalOriginal * item.quantity

        holder.name.text = item.name
        holder.qty.text = if (item.quantity % 1.0 == 0.0)
            item.quantity.toInt().toString()
        else item.quantity.toString()

        // ---- Show prices (same line) ----
        if (item.discountValue != null && item.discountType != null) {
            val discountValue = item.discountValue!!
            val discountedTotal = if (item.discountType == "PERCENTAGE")
                total - (total * discountValue / 100)
            else total - discountValue

            // Original price with strikethrough
            holder.price.text = "₹%.2f".format(total)
            holder.price.paintFlags = holder.price.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.price.setTextColor(context.getColor(R.color.red))

            // Show discounted price in same line
            holder.offerPrice.visibility = View.VISIBLE
            holder.offerPrice.text = " ₹%.2f".format(discountedTotal)
            holder.offerPrice.setTextColor(context.getColor(R.color.green4CAF50))

        } else {
            holder.price.text = "₹%.2f".format(total)
            holder.price.paintFlags = holder.price.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.price.setTextColor(context.getColor(R.color._586374))
            holder.offerPrice.visibility = View.GONE
        }

        holder.quantityLayout.visibility = View.VISIBLE
        holder.addToBag.visibility = View.GONE



        // ---- ✅ Discount UI control visibility ----
        if (discountType == "1") {
            // 🔸 Hide the ENTIRE discount layout (Add + Offer + Remove)
            holder.discountLayout?.visibility = View.GONE
        } else if (discountType == "2") {
            // 🔸 Show layout for product-wise discount
            holder.discountLayout?.visibility = View.VISIBLE
            holder.btnAddDiscount?.visibility = View.VISIBLE
        }


        if (item.discountValue != null) {
            holder.txtOffer?.visibility = View.VISIBLE
            holder.btnRemoveDiscount?.visibility = View.VISIBLE
            holder.btnAddDiscount?.visibility = View.GONE
            holder.txtOffer?.text =
                if (item.discountType == "PERCENTAGE")
                    "${item.discountValue?.toInt()}% OFF"
                else "₹${item.discountValue?.toInt()} OFF"
        } else {
            holder.txtOffer?.visibility = View.GONE
            holder.btnRemoveDiscount?.visibility = View.GONE
            holder.btnAddDiscount?.visibility = View.VISIBLE
        }

        // ---- Add Discount Dialog ----
        holder.btnAddDiscount?.setOnClickListener { showDiscountDialog(holder.itemView, item) }

        // ---- Remove Discount ----
        holder.btnRemoveDiscount?.setOnClickListener {
            item.discountValue = null
            item.discountType = null
            holder.price.paintFlags = holder.price.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.offerPrice.visibility = View.GONE
            holder.txtOffer?.visibility = View.GONE
            holder.btnRemoveDiscount?.visibility = View.GONE
            holder.btnAddDiscount?.visibility = View.VISIBLE
            onItemUpdated(item)
            notifyItemChanged(position)
        }

        // ---- Quantity control ----
        val stock = item.stockCount ?: Double.MAX_VALUE
        holder.btnIncrease.setOnClickListener {
            if (item.quantity < stock) {
                item.quantity++
                notifyItemChanged(position)
                onItemUpdated(item)
            } else {
                Toast.makeText(context, "Stock limit reached (${stock.toInt()})", Toast.LENGTH_SHORT).show()
            }
        }

        holder.btnDecrease.setOnClickListener {
            if (item.quantity > 1) {
                item.quantity = max(1.0, item.quantity - 1.0)
                notifyItemChanged(position)
                onItemUpdated(item)
            }
        }

        // ---- Delete ----
        holder.delete.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                val deletedItem = items[pos]
                items.removeAt(pos)
                notifyItemRemoved(pos)
                onItemDeleted(deletedItem)
            }
        }
    }

    private fun showDiscountDialog(view: View, item: CartItem) {
        val context = view.context
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_discount, null)
        val dialog = android.app.AlertDialog.Builder(context, R.style.CustomAlertDialog)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val btnFlat = dialogView.findViewById<TextView>(R.id.flat)
        val btnPercentage = dialogView.findViewById<TextView>(R.id.percentage)
        val edtDiscount = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.discountValue)
        val btnEnter = dialogView.findViewById<TextView>(R.id.enter)
        val btnCancel = dialogView.findViewById<androidx.appcompat.widget.AppCompatImageView>(R.id.cancel)
        val hintLayout = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.hint)

        var isPercentage = false
        btnFlat.setBackgroundResource(R.drawable.gradient_bg)
        btnFlat.setTextColor(context.getColor(R.color.white))
        hintLayout.hint = "Enter Flat Discount Amount *"

        btnFlat.setOnClickListener {
            isPercentage = false
            btnFlat.setBackgroundResource(R.drawable.gradient_bg)
            btnFlat.setTextColor(context.getColor(R.color.white))
            btnPercentage.setBackgroundColor(context.getColor(android.R.color.transparent))
            btnPercentage.setTextColor(context.getColor(R.color.colorPrimary))
            hintLayout.hint = "Enter Flat Discount Amount *"
        }

        btnPercentage.setOnClickListener {
            isPercentage = true
            btnPercentage.setBackgroundResource(R.drawable.gradient_bg)
            btnPercentage.setTextColor(context.getColor(R.color.white))
            btnFlat.setBackgroundColor(context.getColor(android.R.color.transparent))
            btnFlat.setTextColor(context.getColor(R.color.colorPrimary))
            hintLayout.hint = "Enter Percentage Discount *"
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnEnter.setOnClickListener {
            val value = edtDiscount.text.toString().toDoubleOrNull() ?: 0.0
            if (value <= 0) {
                Toast.makeText(context, "Enter valid discount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            item.originalPrice = if (item.originalPrice == 0.0) item.price else item.originalPrice
            item.discountValue = value
            item.discountType = if (isPercentage) "PERCENTAGE" else "FLAT"

            onItemUpdated(item)
            notifyItemChanged(items.indexOf(item))
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun getItemCount(): Int = items.size
}
