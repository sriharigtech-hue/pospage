package com.example.apitest.network


import com.example.apitest.dataModel.CategoryInput
import com.example.apitest.dataModel.CategoryListOutput
import com.example.apitest.dataModel.Input
import com.example.apitest.dataModel.InputField

import com.example.apitest.dataModel.StatusResponse
import com.example.apitest.dataModel.StatusUpdateInput
import com.example.apitest.dataModel.SubCategoryOutput
import com.example.apitest.dataModel.CategoryOutput

import okhttp3.MultipartBody
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


    @Multipart
    @POST("add_category")
    fun uploadCategory(
        @Header("Authorization") jwtToken: String,
        @Part category_image: MultipartBody.Part?,
        @Part("category_name") type: RequestBody?,
        @Part("status") status: RequestBody?,
    ): Call<StatusResponse>

    @Multipart
    @POST("edit_category")
    fun uploadEditCategory(
        @Header("Authorization") jwtToken: String,
        @Part category_image: MultipartBody.Part?,
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



    //sub_category api
    @Headers("Accept:application/json; charset=UTF-8")
    @POST("all_sub_category")
    fun getAllSubCategoryApi(
        @Header("Authorization") jwtToken: String,
        @Body input: Input?
    ): Call<SubCategoryOutput?>?

    @Headers("Accept:application/json; charset=UTF-8")
    @POST("add_sub_category")
    fun addSubCategory(
        @Header("Authorization") jwtToken: String,
        @Body dashboardInput: InputField?
    ): Call<StatusResponse?>?

    @Headers("Accept:application/json; charset=UTF-8")
    @POST("edit_sub_category")
    fun editSubCategory(
        @Header("Authorization") jwtToken: String,
        @Body dashboardInput: InputField?
    ): Call<StatusResponse?>?

    @Headers("Accept:application/json; charset=UTF-8")
    @POST("delete_sub_category")
    fun deleteSubCategory(
        @Header("Authorization") jwtToken: String,
        @Body statusUpdateInput: StatusUpdateInput?
    ): Call<StatusResponse>

//Item api


    @Headers("Accept:application/json; charset=UTF-8")
    @POST("items_get_all_category")
    fun itemsGetAllCategory(
        @Header("Authorization") jwtToken: String,
        @Body input: Input?
    ): Call<CategoryOutput>







}
