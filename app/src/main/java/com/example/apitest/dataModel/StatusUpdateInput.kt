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

//Sub-category
    @Json(name = "subcategory_id")
    var subcategory_id: String? = null,
    @Json(name = "sub_category_id")
    var sub_category_id: String? = null,
    @Json(name = "subcategory_status")
    var subcategory_status: String? = null,
    @Json(name = "product_id")
    var product_id: String? = null,
    @Json(name = "expense_id")
    var expense_id: String? = null,
    @Json(name = "expense_status")
    var expense_status: String? = null,
    @Json(name = "stock_id")
    var stock_id: String? = null,
    @Json(name = "product_variation_id")
    var product_variation_id: String? = null,
    @Json(name = "stock_quantity")
    var stock_quantity: String? = null,
    @Json(name = "operation")
    var operation: String? = null,
    @Json(name = "product_status")
    var product_status: String? = null,
    @Json(name = "status")
    var status: String? = null



)