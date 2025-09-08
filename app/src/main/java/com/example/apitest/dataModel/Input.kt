// CategoryInput.kt
package com.example.apitest.dataModel

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CategoryInput(
    @Json(name = "status")
    var status: String? = null
)
