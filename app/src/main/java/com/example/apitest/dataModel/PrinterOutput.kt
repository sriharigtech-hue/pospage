package com.example.apitest.dataModel


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class PrinterOutput(
    @Json(name = "data")
    var `data`: List<PrinterList>? = listOf(),
    @Json(name = "message")
    var message: String? = "",
    @Json(name = "status")
    var status: Boolean? = false
)