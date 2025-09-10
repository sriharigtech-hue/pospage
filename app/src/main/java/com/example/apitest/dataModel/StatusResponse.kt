

@file:Suppress("DEPRECATION")

package com.example.apitest.dataModel


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StatusResponse(
    @Json(name = "message")
    var message: String? = null,
    @Json(name = "status")
    var status: Boolean? = null,
    @Json(name = "bill_no")
    var bill_no: String? = null,
    @Json(name = "pdf")
    var pdf: String? = null,
    @Json(name = "order_id")
    var order_id: Int? = null

)