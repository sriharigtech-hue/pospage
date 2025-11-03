package com.example.apitest.dataModel


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class SaleReport(
    @Json(name = "total_sales_amount")
    var total_sales_amount: String? = null,
    @Json(name = "total_orders")
    var total_orders: String? = null,
    @Json(name = "total_upi_sales_amount")
    var total_upi_sales_amount: String? = null,
    @Json(name = "total_cash_sales_amount")
    var total_cash_sales_amount: String? = null,
    @Json(name = "total_upi_orders")
    var total_upi_orders: String? = null,
    @Json(name = "total_cash_orders")
    var total_cash_orders: String? = null,
    @Json(name = "total_cash_upi_orders")
    var total_cash_upi_orders: String? = null,

    @Json(name = "total_expense_amount")
    var total_expense_amount: String? = null,
    @Json(name = "total_upi_expense_amount")
    var total_upi_expense_amount: String? = null,
    @Json(name = "total_cash_expense_amount")
    var total_cash_expense_amount: String? = null,
    @Json(name = "total_expense")
    var total_expense: String? = null,
    @Json(name = "total_upi_expense")
    var total_upi_expense: String? = null,
    @Json(name = "total_cash_expense")
    var total_cash_expense: String? = null
)