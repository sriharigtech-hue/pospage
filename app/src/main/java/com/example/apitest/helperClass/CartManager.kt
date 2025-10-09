package com.example.apitest.helperClass

import com.example.apitest.dataModel.NewProductPrice

object CartManager {
    val cartMap = mutableMapOf<String, Int>()           // key = productId_variationId, value = quantity
    val allProductsMap = mutableMapOf<String, NewProductPrice>() // all variation objects

    fun getDistinctItemsCount(): Int {
        return cartMap.size // now each variation counts separately
    }

    fun getTotalQuantity(): Int {
        return cartMap.values.sum() // sum of all variation quantities
    }

    fun getTotalAmount(): Double {
        var total = 0.0
        cartMap.forEach { (key, qty) ->
            val price = allProductsMap[key]?.productPrice?.toDoubleOrNull() ?: 0.0
            total += price * qty
        }
        return total
    }
}

