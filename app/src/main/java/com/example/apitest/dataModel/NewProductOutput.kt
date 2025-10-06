package com.example.apitest.dataModel


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class NewProductOutput(
    @Json(name = "count")
    var count: Boolean? = false,
    @Json(name = "data")
    var `data`: List<NewProductList>? = listOf(),
    @Json(name = "message")
    var message: String? = null,
    @Json(name = "status")
    var status: Boolean? = false
)