package com.example.apitest.dataModel


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class ProductReportList(
    @Json(name = "exp_name")
    var exp_name: String? = null,
    @Json(name = "product_name")
    var product_name: String? = null,
    @Json(name = "product_variation_name")
    var product_variation_name: String? = null,
    @Json(name = "sale_count")
    var sale_count: String? = null,
    @Json(name = "expense_count")
    var expense_count: String? = null,
    @Json(name = "amount")
    var amount: String? = null
)