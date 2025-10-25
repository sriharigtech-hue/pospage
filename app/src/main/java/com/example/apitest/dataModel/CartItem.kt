package com.example.apitest.dataModel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    val productId: String?,
    val name: String,
    var quantity: Double,
    val price: Double,
    val isCustom: Boolean = false
) : Parcelable
