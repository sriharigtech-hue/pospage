package com.example.apitest.dataModel


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class FeaturesList(
    @Json(name = "sub_title")
    var sub_title: String? = null,
    @Json(name = "description")
    var description: String? = null,
    @Json(name = "name")
    var name: String? = null,
    @Json(name = "key")
    var key: String? = null,
    @Json(name = "status")
    var status: Int? = null,
)