package com.example.apitest.dataModel


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class ProductInput(
    @Json(name = "category_id")
    var categoryId: String? = null,
    @Json(name = "page")
    var page: String? = null,
    @Json(name = "status")
    var status: String? = null,
    @Json(name = "sub_category_id")
    var subCategoryId: String? = null
)