package com.example.apitest.dataModel


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class LowStockProductOutput(
    @Json(name = "data")
    var `data`: List<LowStockProductData>? = listOf(),
    @Json(name = "message")
    var message: String? = null,
    @Json(name = "status")
    var status: Boolean? = false
)