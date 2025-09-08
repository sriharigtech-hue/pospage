package com.example.apitest.network


import com.example.apitest.dataModel.CategoryInput
import com.example.apitest.dataModel.CategoryOutput
import retrofit2.Call
import retrofit2.http.Body


import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST


interface ApiService {
    @Headers("Accept:application/json; charset=UTF-8")
    @POST("stock_category")
    fun stockCategoryApi(
        @Header("Authorization") jwtToken: String,
        @Body input: CategoryInput?,
    ): Call<CategoryOutput>
}
