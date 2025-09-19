package com.example.apitest.dataModel


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class UnitList(
    @Json(name = "unit_id")
    var unitId: Int? = null,
    @Json(name = "unit_name")
    var unitName: String? = null,
    @Json(name = "unit_status")
    var unitStatus: String? = null
)