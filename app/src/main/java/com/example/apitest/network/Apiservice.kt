package com.example.apitest.network


import com.example.apitest.dataModel.AddCategoryOutput
import com.example.apitest.dataModel.CategoryInput
import com.example.apitest.dataModel.CategoryListOutput

import com.example.apitest.dataModel.StatusResponse
import com.example.apitest.dataModel.StatusUpdateInput
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body


import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface ApiService {
    @Headers("Accept:application/json; charset=UTF-8")
    @POST("stock_category")
    fun stockCategoryApi(
        @Header("Authorization") jwtToken: String,
        @Body input: CategoryInput?,
    ): Call<CategoryListOutput>


    @POST("add_category")
    fun addCategoryApi(
        @Header("Authorization") jwtToken: String,
        @Body input: CategoryInput?,
//        @Part("status") status: RequestBody?,
    ): Call<AddCategoryOutput>

    @Multipart
    @POST("edit_category")
    fun uploadEditCategory(
        @Header("Authorization") jwtToken: String,
        @Part("category_id") category_id: RequestBody?,
        @Part("category_name") category_name: RequestBody?,
        @Part("status") status: RequestBody?,
    ): Call<StatusResponse>


    @Headers("Accept:application/json; charset=UTF-8")
    @POST("delete_category")
    fun deleteCategory(
        @Header("Authorization") jwtToken: String,
        @Body statusUpdateInput: StatusUpdateInput?
    ): Call<StatusResponse>

}
