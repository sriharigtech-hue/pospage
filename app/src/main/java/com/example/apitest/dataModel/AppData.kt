// CategoryOutput.kt
package com.example.apitest.dataModel

// One category item
data class Category(
    val category_id: Int,
    val category_name: String,
    val category_image: String,
    val category_status: Int,
    val sub_category_status: String,
    val seq_no: String
)

// Response wrapper
data class CategoryOutput(
    val data: List<Category>
)
