package com.example.apitest.dataModel


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class LowStockProductData(
    @Json(name = "id")
    var id: Int? = null,
    @Json(name = "seq_no")
    var seq_no: String? = null,
    @Json(name = "product_id")
    var productId: Int? = null,
    @Json(name = "product_image")
    var productImage: String? = null,
    @Json(name = "product_name")
    var productName: String? = null,
    @Json(name = "product_status")
    var productStatus: Int? = null,
    @Json(name = "product_variation_id")
    var productVariationId: Int? = null,
    @Json(name = "product_variation_name")
    var productVariationName: String? = null,
    @Json(name = "stock_count")
    var stockCount: Int? = null,
    @Json(name = "low_stock_alert")
    var low_stock_alert: Int? = null,
    @Json(name = "stock_update_status")
    var stock_update_status: Int? = null
)