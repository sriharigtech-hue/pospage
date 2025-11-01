package com.example.apitest.dataModel


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class EmpList(
    @Json(name = "emp_id")
    var empId: Int? = null,
    @Json(name = "emp_name")
    var empName: String? = null
)