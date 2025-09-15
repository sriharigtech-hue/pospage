package com.example.apitest.dataModel


import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
@Keep
@Parcelize
data class SubCategoryDetails(
    @Json(name = "subcategory_id") var subcategoryId: Int? = null,
    @Json(name = "category_id") var categoryId: Int? = null,
    @Json(name = "subcategory_name") var subcategoryName: String? = null,
    @Json(name = "subcategory_image") var subcategoryImage: String? = null,
    @Json(name = "subcategory_status") var subcategoryStatus: Int? = null,
    @Json(name = "category_status") var categoryStatus: String? = null,
    @Json(name = "seq_no") var seqNo: String? = null,
    @Json(name = "category_name") var categoryName: String? = null
):Parcelable
