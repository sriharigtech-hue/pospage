package com.example.apitest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.apitest.fragment.CategoryFragment
import com.example.apitest.fragment.ItemsFragment
import com.example.apitest.fragment.SubCategoryFragment
import android.widget.TextView
import android.widget.Toast
import com.example.apitest.dataModel.Input
import com.example.apitest.dataModel.ProfileOutput
import com.example.apitest.fragment.UnitFragment
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




        val posButton = findViewById<RelativeLayout>(R.id.sale_but)

        posButton.setOnClickListener {
            startActivity(Intent(this, POSActivity::class.java))
        }





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


        val webButton = findViewById<LinearLayout>(R.id.web_button)
        webButton.setOnClickListener {
            fetchUserProfileForStock() // call API when Stock tab clicked
        }


    }

    private fun fetchUserProfileForStock() {
        val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiOTMyZGFhM2IyNGZmM2Q0ZTY5NmQwYTdlODdhNmY1Y2ViOTA1Y2VhMzc0NWU5NmFlMTBhZWEwZjY2MDkxZDZiMWI1MjIwYjIwYmU4N2EyNDEiLCJpYXQiOjE3NTg3OTI2NDEuMjExMDEsIm5iZiI6MTc1ODc5MjY0MS4yMTEwMTMsImV4cCI6MTc5MDMyODY0MS4yMDQ5NjYsInN1YiI6IjYiLCJzY29wZXMiOltdfQ.lcZz0mc3u-to55t0p23p5g_Z1NQHM23K6xT8vaRAr7X9qHsMbyN9OEq-KuHhZGaqikcY-7ak26uM3_mtoLpLfq6jivhUPFC06JL7ID3HRHOUsWY05CqjIwPpOfjop127eWyz1CYmBmZx3mKsSuE2rvNK91OsROryf1Dz-Px0SnVJ1uDJGK9y1zsJ_Mi8wY7WIH_E3cBusI7uSgNHTQq7dh6GurCYymPxnvvR8cdMQ4Om9SnmfqX9f3GCUncHXBVfxYCuH6ElLsjq74Z9ZXRGBLdwdM4BJWiyv4jzKfU80LmErPo7XT90DPzqa40T0pRblACQsaSGLn64TBoKvxlsO4HjJV8nBg3az5PrCkDsj8QTwgPLzJtP7WcT3pvcCI6O3O8OKlL2lR2-csFHNzCHetHaT1fmOLnVWuc5YIjqhYFEOjp8IKVhzxmcocxsd3R8bcvrjR8NcutOX5H7zmfD-GX17f64RT2c0zqSRdVpRxFZYlNycxd3rI591w9ImgZSkeGQN4Eg8us8oqmlRqfF5mO7QZXi_OsjJgnMdovqP1NB1IxHuTHQIyfESkQ3DoA_KYBF4_8DXhyjvE2D5_SQfZitUpjSwfWqZ_ghgVoOdLJokz4TBJQ9j_ec4jK3uf3nCwS_6Evx9zwbZxmin2CpnIrg4lFRmMpO6YgLfYKPqoI"
        val input = Input( status = "1") // Add any required fields if needed

        val call = ApiClient.instance.getUserDetails(jwtToken, input)

        call?.enqueue(object : retrofit2.Callback<ProfileOutput?> {
            override fun onResponse(
                call: retrofit2.Call<ProfileOutput?>,
                response: retrofit2.Response<ProfileOutput?>
            ) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    val stockStatus = profile?.userDetails?.stock_status

                    if (stockStatus == "1") {
                        // Stock is available → open StockActivity
                        startActivity(Intent(this@MainActivity, StockActivity::class.java))
                    } else {
                        // Stock unavailable → show Toast
                        Toast.makeText(
                            this@MainActivity,
                            "Stock status is unavailable",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to fetch profile",
                        Toast.LENGTH_SHORT
                    ).show()
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
