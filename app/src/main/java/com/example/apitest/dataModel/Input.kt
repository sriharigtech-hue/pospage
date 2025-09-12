@file:Suppress("DEPRECATION")

package com.example.apitest.dataModel


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Input(
    @Json(name = "status")
    var status: String? = null,
    @Json(name = "bill_no")
    var bill_no: String? = null,
    @Json(name = "mobile_no")
    var mobile_no: String? = null,
    @Json(name = "employee_id")
    var employee_id: String? = null,
    @Json(name = "category_id")
    var category_id: String? = null,
    @Json(name = "report_date")
    var report_date: String? = null,
    @Json(name = "from_date")
    var from_date: String? = null,
    @Json(name = "to_date")
    var to_date: String? = null,
    @Json(name = "order_id")
    var order_id: String? = null,
    @Json(name = "date")
    var date: String? = null,
    @Json(name = "month")
    var month: String? = null,
    @Json(name = "bill_number")
    var bill_number: String? = null,
    @Json(name = "page")
    var page: String? = null,
    @Json(name = "sub_category_id")
    var sub_category_id: String? = null

)