package com.example.apitest

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.bumptech.glide.Glide
import com.example.apitest.dataModel.Input
import com.example.apitest.dataModel.ProfileOutput
import com.example.apitest.dataModel.UserDetails
import com.example.apitest.helperClass.NavigationActivity
import android.content.Intent

import com.example.apitest.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActivityAccount : NavigationActivity() {
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZTRjY2JlNDBmYWFiZmE5YTg1YmVkMDdjOTcyMzAzNWY3MTljNzQ4M2Q3YzIzMjg3M2E5ZjZmN2QyMjQ1Y2E3MjVhNjVlMTQ5ZGRiZjBkYjgiLCJpYXQiOjE3NjE2MzM3OTIuNDI5OTk5LCJuYmYiOjE3NjE2MzM3OTIuNDMwMDAzLCJleHAiOjE3OTMxNjk3OTIuNDIyNjY5LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.SutcB4SMDdEzgfDZ1lnzGDjY4gf1aSwNJMGFnKF3RXWGbB4MhP6R8wRLCa2wtCMZ1egUQc1LxPMq2S9P-hfCeh4stj4W-zlWf0xOfvswvWVos14aP5RBTjbZ8Rc3PrmAwiFvnzu7PpRtqy8hGF_h1nt2dJrZpT4k0CIuvpUK8afLBKYfpr0i1NOx1awb2Z6Uo5YfJRQrB7y4wrs8TEsnbuAIdC4JG6-9Oi_TPwZ42SGatw4UEUvm09I3SjKGjZRDCOMJZLbSc1O4C6B53aK9hNQKCjnwsSXWc37h-cA6lKg-DSfm6K1usg0yHeAsE-2uya2_b8_TNq3LN4Mb04S820FmpnP0RqtDoPeoqBUSTacd0bSINimYpNv8NyiaO0D6k0J3HzaNd5MmwORyHpTNnVEaM8l0O5iyI-UIa3bPaOBnltamiAydE2EmUA9pfRQy3HWd6yZnxfuITM0THdj73ju1Qh3D0WVP7aUFR-3XUbp5qVflBZkiBe0klClG94ubWNFMX6vebixLQ21KsDvEDLj2Xy0hoLY-g6sm33l14NwbSiUgZ0VQI_3WbOzSpUvU0sprU7ozX7y7-nwjIXjshOQ5ymktZUMCN7LyCbvgX-qcGyxbS8C9JN9BhmvhagIBatDpPZw75arXlksHzxKnytbLG3BVxFHXxkp9jSBqW2s"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
// In your Activity (e.g., ActivityAccount.kt)
        val editProfileLayout = findViewById<LinearLayout>(R.id.editProfile)

        editProfileLayout.setOnClickListener {
            val intent = Intent(this, ActivityEditProfile::class.java)
            startActivity(intent)
        }

        val planDetails = findViewById<LinearLayout>(R.id.planDetails)

        planDetails.setOnClickListener {
            val intent = Intent(this, ActivityPlanDetails::class.java)
            startActivity(intent)
        }

        setupBottomNavigation("profile")
        loadUserProfile()
    }
    override fun onResume() {
        super.onResume()
        loadUserProfile() // ensure the latest API data is applied
    }

    private fun loadUserProfile() {
        val input = Input(status = "1")

        ApiClient.instance.getUserDetails(jwtToken, input)
            ?.enqueue(object : Callback<ProfileOutput?> {
                override fun onResponse(
                    call: Call<ProfileOutput?>,
                    response: Response<ProfileOutput?>
                ) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        val user = response.body()?.userDetails
                        bindProfileData(user)
                    } else {
                        Toast.makeText(
                            this@ActivityAccount,
                            "Failed to fetch profile",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ProfileOutput?>, t: Throwable) {
                    Toast.makeText(
                        this@ActivityAccount,
                        "Network error: ${t.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun bindProfileData(user: UserDetails?) {
        if (user == null) return

        findViewById<AppCompatTextView>(R.id.shopName).text = user.name ?: "-"
        findViewById<AppCompatTextView>(R.id.userName).text = user.userName ?: "-"

        val employeeLayout = findViewById<LinearLayout>(R.id.employeeNameLayout)
        val employeeNameView = findViewById<AppCompatTextView>(R.id.employeeName)
        employeeLayout.visibility = if (user.emp_name.isNullOrEmpty()) View.GONE else View.VISIBLE
        employeeNameView.text = user.emp_name ?: "-"

        findViewById<AppCompatTextView>(R.id.mobile).text = user.userPhoneNumber ?: "-"

        val wholeSaleLayout = findViewById<RelativeLayout>(R.id.wholeSaleSelectionLayout)
        wholeSaleLayout.visibility = if (user.whole_sale_price_status?.trim() == "1") View.VISIBLE else View.GONE
    }


}
