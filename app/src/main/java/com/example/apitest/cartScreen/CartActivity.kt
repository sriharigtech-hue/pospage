package com.example.apitest.cartScreen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.apitest.R
import com.example.apitest.dataModel.Input
import com.example.apitest.dataModel.ProfileOutput
import com.example.apitest.network.SharedPrefManager
import com.example.apitest.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        callProfileApi()
    }

    private fun callProfileApi() {
        val jwtToken = SharedPrefManager.getBearerToken(this)
        val input = Input()

        ApiClient.instance.getUserDetails(jwtToken, input)?.enqueue(object : Callback<ProfileOutput?> {
            override fun onResponse(call: Call<ProfileOutput?>, response: Response<ProfileOutput?>) {
                // Success — you can ignore or handle if needed
            }

            override fun onFailure(call: Call<ProfileOutput?>, t: Throwable) {
                // Failure — you can ignore or handle if needed
            }
        })
    }
}
