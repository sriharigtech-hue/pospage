package com.example.apitest.dataModel


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class InputField(
    @Json(name = "product_id")
    var product_id: Int? = null,
    @Json(name = "category_id")
    var categoryId: Int? = null,
    @Json(name = "sub_category_id")
    var sub_category_id: Int? = null,
    @Json(name = "sub_category_name")
    var subCategoryName: String? = null,
    @Json(name = "unit_id")
    var unit_id: String? = null,
    @Json(name = "unit_name")
    var unit_name: String? = null,
    @Json(name = "password")
    var password: String? = null,
    @Json(name = "order_id")
    var order_id: String? = null,
    @Json(name = "status")
    var status: String? = null

)
