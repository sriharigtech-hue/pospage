package com.example.apitest.dataModel

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class ReportList(
    @Json(name = "grand_total")
    var amount: String? = null,
    @Json(name = "bill_type")
    var bill_type: String? = null,
    @Json(name = "date")
    var date: String? = null,
    @Json(name = "order_id")
    var id: Int? = null,
    @Json(name = "order_string")
    var orderNumber: String? = null,
    @Json(name = "payment_mode")
    var paymentMode: String? = null,
    @Json(name = "booking_time")
    var time: String? = null,
    @Json(name = "emp_name")
    var emp_name: String? = null,
    @Json(name = "notes")
    var notes: String? = null
)