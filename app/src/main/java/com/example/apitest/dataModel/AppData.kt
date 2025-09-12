package com.example.apitest.dataModel

import java.io.Serializable

/// One category item
data class Category(
    val category_id: Int,
    val category_name: String,
    val category_image: String?,
    val category_status: Int,
    val sub_category_status: String?,
    val seq_no: String?
)

// Response for stock_category API
data class CategoryListOutput(
    val status: Boolean,
    val message: String?,
    val data: List<Category>? // list of categories
)

// Response for add_category API
data class AddCategoryOutput(
    val status: Boolean,
    val message: String?,
    val data: String? // empty string in this API
)

data class EditCategoryResponse(
    val data: String?,
    val status: Boolean,
    val message: String
)



