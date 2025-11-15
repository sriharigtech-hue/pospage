package com.example.apitest.helperClass

import android.content.Intent
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.example.apitest.ActivityReport
import com.example.apitest.MainActivity
import com.example.apitest.POSActivity
import com.example.apitest.R
import com.example.apitest.StockActivity
import com.example.apitest.ActivityAccount
import com.example.apitest.UserAccess
import com.example.apitest.dataModel.DashboardOutput
import com.example.apitest.dataModel.Input
import com.example.apitest.dataModel.InputField
import com.example.apitest.dataModel.StatusResponse
import com.example.apitest.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class NavigationActivity : AppCompatActivity() {

    var currentTab: String? = null
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZTRjY2JlNDBmYWFiZmE5YTg1YmVkMDdjOTcyMzAzNWY3MTljNzQ4M2Q3YzIzMjg3M2E5ZjZmN2QyMjQ1Y2E3MjVhNjVlMTQ5ZGRiZjBkYjgiLCJpYXQiOjE3NjE2MzM3OTIuNDI5OTk5LCJuYmYiOjE3NjE2MzM3OTIuNDMwMDAzLCJleHAiOjE3OTMxNjk3OTIuNDIyNjY5LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.SutcB4SMDdEzgfDZ1lnzGDjY4gf1aSwNJMGFnKF3RXWGbB4MhP6R8wRLCa2wtCMZ1egUQc1LxPMq2S9P-hfCeh4stj4W-zlWf0xOfvswvWVos14aP5RBTjbZ8Rc3PrmAwiFvnzu7PpRtqy8hGF_h1nt2dJrZpT4k0CIuvpUK8afLBKYfpr0i1NOx1awb2Z6Uo5YfJRQrB7y4wrs8TEsnbuAIdC4JG6-9Oi_TPwZ42SGatw4UEUvm09I3SjKGjZRDCOMJZLbSc1O4C6B53aK9hNQKCjnwsSXWc37h-cA6lKg-DSfm6K1usg0yHeAsE-2uya2_b8_TNq3LN4Mb04S820FmpnP0RqtDoPeoqBUSTacd0bSINimYpNv8NyiaO0D6k0J3HzaNd5MmwORyHpTNnVEaM8l0O5iyI-UIa3bPaOBnltamiAydE2EmUA9pfRQy3HWd6yZnxfuITM0THdj73ju1Qh3D0WVP7aUFR-3XUbp5qVflBZkiBe0klClG94ubWNFMX6vebixLQ21KsDvEDLj2Xy0hoLY-g6sm33l14NwbSiUgZ0VQI_3WbOzSpUvU0sprU7ozX7y7-nwjIXjshOQ5ymktZUMCN7LyCbvgX-qcGyxbS8C9JN9BhmvhagIBatDpPZw75arXlksHzxKnytbLG3BVxFHXxkp9jSBqW2s"


    protected fun setupBottomNavigation(tab: String) {
        currentTab = tab

        val inventoryBtn = findViewById<LinearLayout>(R.id.inventory_button)
        val stockBtn = findViewById<LinearLayout>(R.id.web_button)
        val posBtn = findViewById<RelativeLayout>(R.id.sale_but)
        val homeBtn = findViewById<LinearLayout>(R.id.home_button) // 👈 new line
        val profileBtn = findViewById<LinearLayout>(R.id.profile_button)

        val inventoryIcon = inventoryBtn.findViewById<AppCompatImageView>(R.id.inventory_icon)
        val stockIcon = stockBtn.findViewById<AppCompatImageView>(R.id.stock_icon)
        val posIcon = posBtn.findViewById<AppCompatImageView>(R.id.sale_icon)
        val profileIcon = profileBtn.findViewById<AppCompatImageView>(R.id.profile_icon)

        // Reset all icons to default gray
        fun resetIcons() {
            inventoryIcon?.setColorFilter(getColor(R.color.grey9599AB))
            stockIcon?.setColorFilter(getColor(R.color.grey9599AB))
            posIcon?.setColorFilter(getColor(R.color.grey9599AB))
        }
        resetIcons()

        // Highlight current tab
        when (currentTab) {
            "inventory" -> inventoryIcon?.setColorFilter(getColor(R.color.colorAccent))
            "stock" -> stockIcon?.setColorFilter(getColor(R.color.colorAccent))
            "pos" -> posIcon?.setColorFilter(getColor(R.color.colorAccent))
            "report" -> homeBtn.findViewById<AppCompatImageView>(R.id.homereport)
                ?.setColorFilter(getColor(R.color.colorAccent))
            "profile" -> profileIcon?.setColorFilter(getColor(R.color.colorAccent))

        }

        // Only navigate if user clicks a tab different from current
        inventoryBtn.setOnClickListener {
            if (currentTab != "inventory") {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(intent)
            }
        }



        stockBtn.setOnClickListener {
            if (!UserAccess.isStockAllowed) {
                // Stock access is restricted
                Toast.makeText(this, "Access restricted", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentTab != "stock") {
                startActivity(Intent(this, StockActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                })
            }
        }

        posBtn.setOnClickListener {
            if (currentTab != "pos") {
                val intent = Intent(this, POSActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(intent)
            }
        }
        // ✅ New Report tab
        homeBtn.setOnClickListener {
            if (currentTab != "report") {
                showReportPasswordDialog()
            }
        }

        profileBtn.setOnClickListener {
            startActivity(Intent(this, ActivityAccount::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }

    }



    private fun showReportPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_password_report, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val passwordInput =
            dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.value)
        val submitBtn =
            dialogView.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.enter)
        val cancelBtn =
            dialogView.findViewById<androidx.appcompat.widget.AppCompatImageView>(R.id.cancel)

        cancelBtn.setOnClickListener { dialog.dismiss() }

        submitBtn.setOnClickListener {
            val enteredPassword = passwordInput.text?.toString()?.trim()

            if (enteredPassword.isNullOrEmpty()) {
                passwordInput.error = "Please enter password"
                return@setOnClickListener
            }

            // ✅ Prepare input
            val input = InputField(
                password = enteredPassword,
                status = "1"
            )


            ApiClient.instance.checkReportPassword(jwtToken, input)
                ?.enqueue(object : Callback<StatusResponse?> {
                    override fun onResponse(
                        call: Call<StatusResponse?>,
                        response: Response<StatusResponse?>,
                    ) {
                        if (response.isSuccessful && response.body()?.status == true) {
                            dialog.dismiss()
                            Toast.makeText(
                                this@NavigationActivity,
                                "Access Granted",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(
                                Intent(
                                    this@NavigationActivity,
                                    ActivityReport::class.java
                                ).apply {
                                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                })
                        } else {
                            Toast.makeText(
                                this@NavigationActivity,
                                "Incorrect password",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(p0: Call<StatusResponse?>, t: Throwable) {
                        Toast.makeText(
                            this@NavigationActivity,
                            "Network error: ${t.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }

        dialog.show()
    }




}
