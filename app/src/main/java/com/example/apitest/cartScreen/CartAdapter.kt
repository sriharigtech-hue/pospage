package com.example.apitest.cartScreen

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.NewProductPrice
import com.example.apitest.helperClass.CartManager
import com.makeramen.roundedimageview.RoundedImageView
import com.google.android.material.imageview.ShapeableImageView
import kotlin.math.max

class CartAdapter(
    private val normalProducts: MutableList<NewProductPrice>,
    private val customProducts: MutableList<CartManager.CustomProduct>,
    private val onTotalChanged: () -> Unit,
    private val quantityStatus: String = "0"

) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {
    private val isFractionalEnabled = quantityStatus == "1"
    private val defaultIncrement = if (isFractionalEnabled) 1.0 else 1.0

    init {
        Log.d("CartAdapter", "Initializing CartAdapter: normal=${normalProducts.size}, custom=${customProducts.size}")
    }


    fun updateData(newNormal: List<NewProductPrice>, newCustom: List<CartManager.CustomProduct>) {
        normalProducts.clear()
        normalProducts.addAll(newNormal)
        customProducts.clear()
        customProducts.addAll(newCustom)
        Log.d("CartAdapter", "updateData called: normal=${normalProducts.size}, custom=${customProducts.size}")
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_product, parent, false)
        return CartViewHolder(view)
    }

    override fun getItemCount(): Int = normalProducts.size + customProducts.size

//    override fun getItemId(position: Int): Long {
//        return if (position < normalProducts.size) {
//            val product = normalProducts[position]
//            val pId = product.productId?.toLong() ?: 0L
//            val pPriceId = product.productPriceId?.toLong() ?: 0L
//            return (pId * 100000L) + pPriceId
//        } else {
//
//            val customProductIndex = position - normalProducts.size
//            return CUSTOM_PRODUCT_ID_OFFSET + customProductIndex.toLong()
//        }
//    }


    private fun getCurrentQuantity(position: Int): Double {
        return if (position < normalProducts.size) {
            val p = normalProducts[position]
            if (isFractionalEnabled) p.selectedQuantityDecimal else p.selectedQuantity.toDouble()
        } else customProducts[position - normalProducts.size].quantity
    }

    private fun saveNewQuantity(position: Int, newQty: Double) {
        if (position < normalProducts.size) {
            val product = normalProducts[position]
            val key = "${product.productId}_${product.productPriceId}"
            if (isFractionalEnabled) {
                product.selectedQuantityDecimal = newQty
                product.selectedQuantity = newQty.toInt()
            } else {
                product.selectedQuantityDecimal = newQty
                product.selectedQuantity = newQty.toInt()
            }
            CartManager.cartMap[key] = newQty
            CartManager.allProductsMap[key] = product
            Log.d("CartAdapter", "Normal product updated: key=$key, qty=$newQty")
        } else {
            val custom = customProducts[position - normalProducts.size]
            custom.quantity = newQty
            Log.d("CartAdapter", "Custom product updated: id=${custom.id}, qty=$newQty")
        }
        CartManager.cartChangedLiveData.postValue(System.currentTimeMillis())
        onTotalChanged()
    }


    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val isNormal = position < normalProducts.size
        if (isNormal) {
            val product = normalProducts[position]
            val key = "${product.productId}_${product.productPriceId}"
            holder.bindNormal(product, isFractionalEnabled)
        } else {
            holder.bindCustom(customProducts[position - normalProducts.size])
        }


        val currentQty = getCurrentQuantity(position)

        holder.txtQuantity.text = if (isFractionalEnabled) "%.1f".format(currentQty) else currentQty.toInt().toString()
        holder.addToBag.visibility = if (currentQty <= 0.0) View.VISIBLE else View.GONE
        holder.quantityLayout.visibility = if (currentQty > 0.0) View.VISIBLE else View.GONE

        // --- Add to Bag click (set to 1.0) ---
        holder.addToBag.setOnClickListener {
//            // Check stock before adding
//            val stock = if (isNormalProduct) normalProducts[position].stockCount?.toDouble() ?: 0.0 else Double.MAX_VALUE
//
//            if (stock <= 0.0) {
//                Toast.makeText(holder.itemView.context, "Out of stock", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }

            saveNewQuantity(position, defaultIncrement)
            notifyItemChanged(position)
//            onTotalChanged()
        }

        // --- Increase quantity ---
        holder.btnIncrease.setOnClickListener {
//            val currentQty = getCurrentQuantity(position)
//            val newQty = currentQty + defaultIncrement
//
//            // 🌟 FIX: Add stock limit check for normal products
//            val stock = if (isNormalProduct) normalProducts[position].stockCount?.toDouble() ?: 0.0 else Double.MAX_VALUE
//
//            if (newQty <= stock) {
//                saveNewQuantity(position, newQty)
//                notifyItemChanged(position)
//                onTotalChanged()
//                Log.d("CartAdapter", "Increase clicked: position=$position, newQty=$newQty")
//
//            } else {
//                Toast.makeText(holder.itemView.context, "Stock limit reached", Toast.LENGTH_SHORT).show()
//            }
            saveNewQuantity(position, getCurrentQuantity(position) + defaultIncrement)
            notifyItemChanged(position)
        }

        // --- Decrease quantity ---
        holder.btnDecrease.setOnClickListener {
            val newQty = getCurrentQuantity(position) - defaultIncrement
            if (newQty <= 0.0) {
                if (isNormal) {
                    val product = normalProducts[position]
                    val key = "${product.productId}_${product.productPriceId}"
                    CartManager.removeProduct(key)
                    normalProducts.removeAt(position)
                } else {
                    val custom = customProducts[position - normalProducts.size]
                    CartManager.removeCustomProduct(custom.id)
                    customProducts.removeAt(position - normalProducts.size)
                }
                notifyItemRemoved(position)
            } else {
                saveNewQuantity(position, newQty)
                notifyItemChanged(position)
            }
        }



        // --- Delete product ---
        holder.delete.setOnClickListener {
            if (isNormal) {
                val product = normalProducts[position]
                CartManager.removeProduct("${product.productId}_${product.productPriceId}")
                normalProducts.removeAt(position)
            } else {
                val custom = customProducts[position - normalProducts.size]
                CartManager.removeCustomProduct(custom.id)
                customProducts.removeAt(position - normalProducts.size)
            }
            notifyItemRemoved(position)
        }
    }

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.product_name)
        val txtQuantity: TextView = itemView.findViewById(R.id.txt_quantity)
        val btnIncrease: TextView = itemView.findViewById(R.id.btn_increase)
        val btnDecrease: TextView = itemView.findViewById(R.id.btn_decrease)
        val delete: ShapeableImageView = itemView.findViewById(R.id.delete)
        val productAmount: TextView = itemView.findViewById(R.id.sub_total_product_amount)
        val productImage: RoundedImageView = itemView.findViewById(R.id.product_image)
        val addToBag: TextView = itemView.findViewById(R.id.add_to_bag)
        val quantityLayout: LinearLayout = itemView.findViewById(R.id.quantity_layout)


        fun bindNormal(product: NewProductPrice, isFractionalEnabled: Boolean) {
            val name = product.productName ?: ""
            val variation = product.productVariation ?: ""
            productName.text = if (variation.isNotEmpty()) "$name - $variation" else name


            val qty = if (isFractionalEnabled) product.selectedQuantityDecimal else product.selectedQuantity.toDouble()

            txtQuantity.text = if (isFractionalEnabled)
                "%.1f".format(qty)
            else
                qty.toInt().toString()

            val price = product.productPrice?.toDoubleOrNull() ?: 0.0
            productAmount.text = "₹%.2f".format(price * qty)
            productImage.visibility = View.GONE
        }

        fun bindCustom(product: CartManager.CustomProduct) {
            productName.text = product.name
            val qty = product.quantity

            txtQuantity.text = if (qty % 1.0 != 0.0) "%.1f".format(qty) else qty.toInt().toString()

            productAmount.text = "₹%.2f".format(product.price * qty)
            productImage.visibility = View.GONE
        }
    }
}