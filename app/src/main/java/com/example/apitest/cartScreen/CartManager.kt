package com.example.apitest.cartScreen

import com.example.apitest.dataModel.CartItem

object CartManager {
    val cartList = mutableListOf<CartItem>()

    fun addOrUpdate(item: CartItem) {
        if (item.isCustom) {
            val existing = cartList.find { it.name.equals(item.name, true) && it.isCustom }
            if (existing != null) existing.quantity = item.quantity
            else cartList.add(item)
        } else {
            val existing = cartList.find { it.productId == item.productId && !it.isCustom }
            if (existing != null) existing.quantity = item.quantity
            else cartList.add(item)
        }
    }

    fun remove(item: CartItem) {
        cartList.remove(item)
    }

    fun getTotalQty() = cartList.sumOf { it.quantity }
    fun getTotalAmount() = cartList.sumOf { it.quantity * it.price }
}
