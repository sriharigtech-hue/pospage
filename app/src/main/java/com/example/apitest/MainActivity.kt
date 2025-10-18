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
        val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZGE0YjJmNjlmZGJkNjMwMDMyNGE3MWNkZWRhMDI2ZWI2YTIwMGM4NWIyNTI2MTNjOTZhZGIyMDA2MTE3YjMxMGI0MTFjYjczNzNmZmNlZDAiLCJpYXQiOjE3NjAzMjkzNTkuNDc3MTA1LCJuYmYiOjE3NjAzMjkzNTkuNDc3MTA4LCJleHAiOjE3OTE4NjUzNTkuNDcyNjI1LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.C3ySWdDX7BRHm4qzwWFZZofL_DEx3C2Qjy7iEUWxy9GdrL8OJS7m7Kk_Oe4HtFaT7DvPMEWE_c9kIC8RalMXflXTvPGKkfsw7yxdVxZOKSE20UNZiSbScAdvx3RxAz-XoHK4wJr7wepspLad5y5KCv4RyPXAJl8sIjFELfiCMoxt1CiYGp5_GhsOjbMeSLWSBoDwd3H4MLNvUyU2KN2zhvQaRRUh4T-L11mZgmd_8A8kWZbp_bO6AK-3hGHFGd7VaT2Xqoi4asmn0ABlxusVYWG6hw9UhnU-_uxOVFQLAHog-WKfbahCwfkssXtK07wMpk-ZGHfRn7ujbkrMAX5gNgNkcNQZPRMkUSrokHylEJXKC7UOAgUiK8fy32bIlmFuMQE9hTuuQjHWJ8hdEqtPaXVIcc1oXURtZhCWTp2APH9RE4_L41NYStog_bVMdXwRO_a6QEg_ex0moqxwtRZKivnIF4DKm6WLj45X0FLj-F7HTlZ-eoc9j3w_dVaVyhhxEKUiTyQSJ_AwVKMTbAUmxvWY3OnoIAmu4WYrbC4T4tA2cWoB9yXKna8Yfbil_vC46tLZweGF7RRZR2MPT16q-iCzKG73JqAMphV4NO7b-bMk6mhvgz8TR0_YUewsPg2CVvgdvEmnV4DE4znhnwiLMniN0kPGzF5pindkKTVNDb8"
        val input = Input(status = "1")

        ApiClient.instance.getUserDetails(jwtToken, input)?.enqueue(object :
            retrofit2.Callback<ProfileOutput?> {
            override fun onResponse(
                call: retrofit2.Call<ProfileOutput?>,
                response: retrofit2.Response<ProfileOutput?>
            ) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    UserAccess.isStockAllowed = profile?.userDetails?.stock_status == "1"

                    if (UserAccess.isStockAllowed) {
                        // Open StockActivity
                        startActivity(Intent(this@MainActivity, StockActivity::class.java))
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Access restricted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Failed to fetch profile", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: retrofit2.Call<ProfileOutput?>, t: Throwable) {
                Toast.makeText(
                    this@MainActivity,
                    "API error: ${t.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
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
