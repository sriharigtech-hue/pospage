package com.example.apitest.dataModel


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class NewProductList(
    @Json(name = "category_id")
    var categoryId: Int? = null,
    @Json(name = "product_id")
    var productId: Int? = null,
    @Json(name = "seq_no")
    var seq_no: String? = null,
    @Json(name = "product_image")
    var productImage: String? = null,
    @Json(name = "product_name")
    var productName: String? = null,
    @Json(name = "product_price")
    var productPrice: List<NewProductPrice>? = listOf(),
    @Json(name = "product_status")
    var productStatus: Int? = null,
    @Json(name = "sub_categoryid")
    var subCategoryid: Int? = null,
    @Json(name = "stock_status")
    var stock_status: String? = null,
    @Json(name = "stock_count")
    var stockCount: Int? = null,
    @Json(name = "low_stock_alert")
    var low_stock_alert: Int? = null,
    @Json(name = "stock_update_status")
    var stock_update_status: Int? = null,
    @Json(name = "unit_id")
    var unitId: Int? = null,
    @Json(name = "unit_name")
    var unitName: String? = null,

    // 🟢 New field to track quantity selected by user , i added this
    var selectedQuantity: Int = 0
)