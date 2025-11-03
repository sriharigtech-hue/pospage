package com.example.apitest.dataModel


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class DashboardOutput(
    @Json(name = "message")
    var message: String? = null,

    @Json(name = "status")
    var status: Boolean? = null,

    @Json(name = "data")
    var dataList: Dashboard? = null,
)