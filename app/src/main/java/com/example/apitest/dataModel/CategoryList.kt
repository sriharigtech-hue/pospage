package com.example.apitest.dataModel


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class CategoryList(
    @Json(name = "category_id")
    var categoryId: Int? = null,
    @Json(name = "category_image")
    var categoryImage: String? = null,
    @Json(name = "category_name")
    var categoryName: String? = null,
    @Json(name = "sub_category_status")
    var subCategoryStatus: String? = null,
    @Json(name = "category_status")
    var categoryStatus: String? = null
)