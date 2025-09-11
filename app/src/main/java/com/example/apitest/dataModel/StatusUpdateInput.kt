package com.example.apitest.dataModel


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class StatusUpdateInput(



    @Json(name = "category_id")
    var category_id: Int,

//    @Json(name = "category_status")
//    var categoryStatus: String? = null,







)