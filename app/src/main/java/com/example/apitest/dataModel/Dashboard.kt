package com.example.apitest.dataModel

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class Dashboard(
    @Json(name = "today_sales_amount")
    var todaySalesAmount: String? = null,

    @Json(name = "yesterday_sales_amount")
    var yesterday_sales_amount: String? = null,

    @Json(name = "week_sales_amount")
    var week_sales_amount: String? = null,

    @Json(name = "month_sales_amount")
    var month_sales_amount: String? = null,

    @Json(name = "today_orders_count")
    var today_orders_count: String? = null,

    @Json(name = "yesterday_orders_count")
    var yesterday_orders_count: String? = null,

    @Json(name = "week_orders_count")
    var week_orders_count: String? = null,

    @Json(name = "month_orders_count")
    var month_orders_count: String? = null,

    @Json(name = "today_upi_sales_amount")
    var today_upi_sales_amount: String? = null,

    @Json(name = "today_cash_sales_amount")
    var today_cash_sales_amount: String? = null,

    @Json(name = "today_expense_amount")
    var today_expense_amount: String? = null,

    @Json(name = "today_cash_expense_amount")
    var today_cash_expense_amount: String? = null,

    @Json(name = "today_upi_expense_amount")
    var today_upi_expense_amount: String? = null,

    @Json(name = "yesterday_expense_amount")
    var yesterday_expense_amount: String? = null,

    @Json(name = "week_expense_amount")
    var week_expense_amount: String? = null,

    @Json(name = "month_expense_amount")
    var month_expense_amount: String? = null,

    @Json(name = "today_expense_count")
    var today_expense_count: String? = null,

    @Json(name = "yesterday_expense_count")
    var yesterday_expense_count: String? = null,

    @Json(name = "week_expense_count")
    var week_expense_count: String? = null,

    @Json(name = "month_expense_count")
    var month_expense_count: String? = null,
)