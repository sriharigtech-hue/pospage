package com.example.apitest.dataModel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    val productId: String?,
    val name: String,
    var quantity: Double,
    var price: Double,
    val isCustom: Boolean = false,
    val stockCount: Double? = null, // ✅ Added field
    var originalPrice: Double = 0.0,
    var discountValue: Double? = null,
    var discountType: String? = null,
    var discountedPrice: Double? = null,
    val variationName: String? = null,
    var discount: Double = 0.0 // percent-based discount


) : Parcelable
