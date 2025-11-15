package com.example.apitest.dataModel


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class PlanDetails(
    @Json(name = "plan_type")
    var plan_type: String? = null,
    @Json(name = "validity_content")
    var validity_content: String? = null,
    @Json(name = "name")
    var name: String? = null,
    @Json(name = "expiry_date")
    var expiry_date: String? = null,
    @Json(name = "days_count")
    var days_count: Int? = null,
    @Json(name = "category_count")
    var category_count: Int? = null,
    @Json(name = "product_count")
    var product_count: Int? = null,
    @Json(name = "order_count")
    var order_count: Int? = null,
    @Json(name = "reseller_six_month_prize")
    var reseller_six_month_prize: Float? = null,
    @Json(name = "ginex_six_month_prize")
    var ginex_six_month_prize: Float? = null,
    @Json(name = "reseller_twelve_month_prize")
    var reseller_twelve_month_prize: Float? = null,
    @Json(name = "ginex_twelve_month_prize")
    var ginex_twelve_month_prize: Float? = null,
    @Json(name = "description")
    var description: String? = null,
    @Json(name = "reseller_status")
    var reseller_status: String? = null,
    @Json(name = "id")
    var id: Int? = null,
    @Json(name = "features")
    var features: List<FeaturesList>? = null
)