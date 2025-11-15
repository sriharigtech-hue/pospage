package com.example.apitest.dataModel


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class POSFeatures(
    @Json(name = "intro_content_1")
    var intro_content_1: String? = null,
    @Json(name = "intro_content_2")
    var intro_content_2: String? = null,
    @Json(name = "title")
    var title: String? = null,
    @Json(name = "features")
    var features: List<FeaturesList>? = null
)