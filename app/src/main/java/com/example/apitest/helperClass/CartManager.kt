package com.example.apitest.helperClass

import com.example.apitest.dataModel.NewProductPrice

object CartManager {
    val cartMap: MutableMap<String, Int> = mutableMapOf()
    val allProductsMap: MutableMap<String, NewProductPrice> = mutableMapOf()

    // Total quantity (sum of all)
    fun getTotalQuantity(): Int = cartMap.values.sum()

    // Distinct products count (for badge)
    fun getDistinctItemsCount(): Int = cartMap.size

    fun getTotalAmount(): Double = cartMap.entries.sumOf { (key, qty) ->
        val price = allProductsMap[key]?.productPrice?.toDoubleOrNull() ?: 0.0
        price * qty
    }

    fun clearCart() {
        cartMap.clear()
        allProductsMap.clear()
    }
}
