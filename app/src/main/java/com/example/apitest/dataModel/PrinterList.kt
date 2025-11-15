package com.example.apitest.dataModel


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class PrinterList(
    @Json(name = "id")
    var id: Int? = null,
    @Json(name = "printer_size")
    var printerSize: String? = null
)