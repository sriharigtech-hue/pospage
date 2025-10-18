package com.example.apitest.helperClass

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.apitest.dataModel.NewProductPrice

object CartManager {

    val cartMap = mutableMapOf<String, Double>()
    val allProductsMap = mutableMapOf<String, NewProductPrice>()
    val customProducts = mutableListOf<CustomProduct>()

    // Use a Long timestamp as an event token so observers always receive updates.
    val cartChangedLiveData = MutableLiveData<Long>() // -> posts System.currentTimeMillis()

    data class CustomProduct(
        val id: String,
        val name: String,
        val variation: String,
        var quantity: Double,
        val price: Double,
        val tax: Double,
        val mrp: Double,
        val wholesalePrice: Double,
        val unit: String
    )

    private fun notifyCartChanged() {
        cartChangedLiveData.postValue(System.currentTimeMillis())
    }

    fun addCustomProduct(product: CustomProduct) {
        customProducts.add(product)
        notifyCartChanged()
    }

    fun getAllCustomProducts(): List<CustomProduct> = customProducts

    fun getDistinctItemsCount(): Int {
        val validCart = cartMap.filter { it.value > 0.0 }
        return validCart.size + customProducts.count { it.quantity > 0.0 }
    }

    fun getTotalQuantity(): Double {
        val normalQty = cartMap.values.sum()
        val customQty = customProducts.sumOf { it.quantity }
        return normalQty + customQty
    }

    fun getTotalAmount(): Double {
        var total = 0.0
        cartMap.forEach { (key, qty) ->
            val price = allProductsMap[key]?.productPrice?.toDoubleOrNull() ?: 0.0
            total += price * qty
        }
        total += customProducts.sumOf { it.price * it.quantity }
        return total
    }

    fun removeProduct(productKey: String) {
        cartMap.remove(productKey)
        allProductsMap[productKey]?.let {
            it.selectedQuantity = 0
            it.selectedQuantityDecimal = 0.0
        }
        allProductsMap.remove(productKey)
        Log.d("CartManager", "Removed product $productKey. cartMap=${cartMap.keys}")
        notifyCartChanged()
    }

    fun removeCustomProduct(id: String) {
        customProducts.removeAll { it.id == id }
        Log.d("CartManager", "Removed custom product $id. customProducts=${customProducts.map { it.id }}")
        notifyCartChanged()
    }

    fun clearCart() {
        cartMap.clear()
        allProductsMap.clear()
        customProducts.clear()
        notifyCartChanged()
    }
}
