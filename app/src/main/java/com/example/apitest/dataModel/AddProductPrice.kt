package com.example.apitest.dataModel

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class AddProductPrice(
    @Json(name = "product_variation_id")
    var product_variation_id: Int? = null,
    @Json(name = "product_variation")
    var product_variation: String? = null,
    @Json(name = "product_price")
    var product_price: String? = null,
    @Json(name = "stock_quantity")
    var stock_quantity: String? = null,
    @Json(name = "low_stock_alert")
    var low_stock_alert: String? = null,
    @Json(name = "mrp_price")
    var mrp_price: String? = null,
    @Json(name = "whole_sale_price")
    var whole_sale_price: String? = null,
    @Json(name = "product_tax")
    var product_tax: String? = null,
    @Json(name = "unit_id")
    var unit_id: String? = null,
    @Json(name = "unit_name")
    var unit_name: String? = null,
    @Json(name = "sku")
    var sku: String? = null,

    @Json(name = "unit")
    val unit: String? = null
)