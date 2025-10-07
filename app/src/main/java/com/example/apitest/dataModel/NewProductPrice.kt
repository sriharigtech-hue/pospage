package com.example.apitest.dataModel


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class NewProductPrice(
    @Json(name = "mrp_price")
    var mrpPrice: String? = null,
    @Json(name = "whole_sale_price")
    var wholeSalePrice: String? = null,
    @Json(name = "product_tax")
    var productTax: String? = null,
    @Json(name = "product_price")
    var productPrice: String? = null,
    @Json(name = "product_price_id")
    var productPriceId: Int? = null,
    @Json(name = "product_unit")
    var productUnit: String? = null,
    @Json(name = "product_variation")
    var productVariation: String? = null,
    @Json(name = "stock_count")
    var stockCount: Int? = null,
    @Json(name = "low_stock_alert")
    var low_stock_alert: Int? = null,
    @Json(name = "stock_update_status")
    var stock_update_status: Int? = null,
    @Json(name = "unit_id")
    var unitId: String? = null,
    @Json(name = "unit_name")
    var unitName: String? = null,

// **New field to track quantity per variation**
    var selectedQuantity: Int = 0
)