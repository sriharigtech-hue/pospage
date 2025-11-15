package com.example.apitest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.apitest.dataModel.Input
import com.example.apitest.dataModel.ProfileOutput
import com.example.apitest.fragment.CategoryFragment
import com.example.apitest.fragment.ItemsFragment
import com.example.apitest.fragment.SubCategoryFragment
import com.example.apitest.fragment.UnitFragment
import com.example.apitest.helperClass.NavigationActivity
import com.example.apitest.network.ApiClient

class MainActivity : NavigationActivity() {

    private lateinit var tabItems: TextView
    private lateinit var tabSubCategory: TextView
    private lateinit var tabCategory: TextView
    private lateinit var tabUnit: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupBottomNavigation("inventory")

        tabItems = findViewById(R.id.tabItems)
        tabSubCategory = findViewById(R.id.tabSubCategory)
        tabCategory = findViewById(R.id.tabCategory)
        tabUnit = findViewById(R.id.tabUnit)

        tabUnit.visibility = View.GONE

        // Default fragment
        replaceFragment(ItemsFragment())

        tabItems.setOnClickListener {
            replaceFragment(ItemsFragment())
            selectTab(tabItems)
        }
        tabSubCategory.setOnClickListener {
            replaceFragment(SubCategoryFragment())
            selectTab(tabSubCategory)
        }
        tabCategory.setOnClickListener {
            replaceFragment(CategoryFragment())
            selectTab(tabCategory)
        }
        tabUnit.setOnClickListener {
            replaceFragment(UnitFragment())
            selectTab(tabUnit)
        }

        // POS button
        findViewById<RelativeLayout>(R.id.sale_but).setOnClickListener {
            startActivity(Intent(this, POSActivity::class.java))
        }

        // Stock button click → check API first
        findViewById<LinearLayout>(R.id.web_button).setOnClickListener {
            fetchUserProfileForStock()
        }

    }

    private fun fetchUserProfileForStock() {
        val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZTRjY2JlNDBmYWFiZmE5YTg1YmVkMDdjOTcyMzAzNWY3MTljNzQ4M2Q3YzIzMjg3M2E5ZjZmN2QyMjQ1Y2E3MjVhNjVlMTQ5ZGRiZjBkYjgiLCJpYXQiOjE3NjE2MzM3OTIuNDI5OTk5LCJuYmYiOjE3NjE2MzM3OTIuNDMwMDAzLCJleHAiOjE3OTMxNjk3OTIuNDIyNjY5LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.SutcB4SMDdEzgfDZ1lnzGDjY4gf1aSwNJMGFnKF3RXWGbB4MhP6R8wRLCa2wtCMZ1egUQc1LxPMq2S9P-hfCeh4stj4W-zlWf0xOfvswvWVos14aP5RBTjbZ8Rc3PrmAwiFvnzu7PpRtqy8hGF_h1nt2dJrZpT4k0CIuvpUK8afLBKYfpr0i1NOx1awb2Z6Uo5YfJRQrB7y4wrs8TEsnbuAIdC4JG6-9Oi_TPwZ42SGatw4UEUvm09I3SjKGjZRDCOMJZLbSc1O4C6B53aK9hNQKCjnwsSXWc37h-cA6lKg-DSfm6K1usg0yHeAsE-2uya2_b8_TNq3LN4Mb04S820FmpnP0RqtDoPeoqBUSTacd0bSINimYpNv8NyiaO0D6k0J3HzaNd5MmwORyHpTNnVEaM8l0O5iyI-UIa3bPaOBnltamiAydE2EmUA9pfRQy3HWd6yZnxfuITM0THdj73ju1Qh3D0WVP7aUFR-3XUbp5qVflBZkiBe0klClG94ubWNFMX6vebixLQ21KsDvEDLj2Xy0hoLY-g6sm33l14NwbSiUgZ0VQI_3WbOzSpUvU0sprU7ozX7y7-nwjIXjshOQ5ymktZUMCN7LyCbvgX-qcGyxbS8C9JN9BhmvhagIBatDpPZw75arXlksHzxKnytbLG3BVxFHXxkp9jSBqW2s"
        val input = Input(status = "1")

        ApiClient.instance.getUserDetails(jwtToken, input)?.enqueue(object :
            retrofit2.Callback<ProfileOutput?> {
            override fun onResponse(
                call: retrofit2.Call<ProfileOutput?>,
                response: retrofit2.Response<ProfileOutput?>
            ) {
                if (response.isSuccessful) {
                    val profile = response.body()

                    // ✅ Check stock status
                    val stockAllowed = profile?.userDetails?.stock_status == "1"
                    UserAccess.isStockAllowed = stockAllowed

                    if (stockAllowed) {
                        // Open Stock screen
                        startActivity(Intent(this@MainActivity, StockActivity::class.java))
                    } else {
                        // Show access restricted toast
                        Toast.makeText(this@MainActivity, "Access Restricted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Failed to fetch profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<ProfileOutput?>, t: Throwable) {
                Toast.makeText(this@MainActivity, "API error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun selectTab(selected: TextView) {
        tabItems.setBackgroundResource(0)
        tabSubCategory.setBackgroundResource(0)
        tabCategory.setBackgroundResource(0)
        tabUnit.setBackgroundResource(0)
        selected.setBackgroundResource(R.drawable.tab_selected_bg)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun setUnitTabVisibility(visible: Boolean) {
        tabUnit.visibility = if (visible) TextView.VISIBLE else TextView.GONE
    }

}
