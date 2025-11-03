package com.example.apitest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apitest.dataModel.Dashboard
import com.example.apitest.dataModel.DashboardOutput
import com.example.apitest.dataModel.Input
import com.example.apitest.dataModel.ProfileOutput
import com.example.apitest.helperClass.NavigationActivity
import com.example.apitest.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ActivityReport : NavigationActivity() {
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZTRjY2JlNDBmYWFiZmE5YTg1YmVkMDdjOTcyMzAzNWY3MTljNzQ4M2Q3YzIzMjg3M2E5ZjZmN2QyMjQ1Y2E3MjVhNjVlMTQ5ZGRiZjBkYjgiLCJpYXQiOjE3NjE2MzM3OTIuNDI5OTk5LCJuYmYiOjE3NjE2MzM3OTIuNDMwMDAzLCJleHAiOjE3OTMxNjk3OTIuNDIyNjY5LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.SutcB4SMDdEzgfDZ1lnzGDjY4gf1aSwNJMGFnKF3RXWGbB4MhP6R8wRLCa2wtCMZ1egUQc1LxPMq2S9P-hfCeh4stj4W-zlWf0xOfvswvWVos14aP5RBTjbZ8Rc3PrmAwiFvnzu7PpRtqy8hGF_h1nt2dJrZpT4k0CIuvpUK8afLBKYfpr0i1NOx1awb2Z6Uo5YfJRQrB7y4wrs8TEsnbuAIdC4JG6-9Oi_TPwZ42SGatw4UEUvm09I3SjKGjZRDCOMJZLbSc1O4C6B53aK9hNQKCjnwsSXWc37h-cA6lKg-DSfm6K1usg0yHeAsE-2uya2_b8_TNq3LN4Mb04S820FmpnP0RqtDoPeoqBUSTacd0bSINimYpNv8NyiaO0D6k0J3HzaNd5MmwORyHpTNnVEaM8l0O5iyI-UIa3bPaOBnltamiAydE2EmUA9pfRQy3HWd6yZnxfuITM0THdj73ju1Qh3D0WVP7aUFR-3XUbp5qVflBZkiBe0klClG94ubWNFMX6vebixLQ21KsDvEDLj2Xy0hoLY-g6sm33l14NwbSiUgZ0VQI_3WbOzSpUvU0sprU7ozX7y7-nwjIXjshOQ5ymktZUMCN7LyCbvgX-qcGyxbS8C9JN9BhmvhagIBatDpPZw75arXlksHzxKnytbLG3BVxFHXxkp9jSBqW2s"
    private var dashboardData: Dashboard? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        setupBottomNavigation("report")

        // 👉 Navigate to Order Wise Report page when clicked
        findViewById<View>(R.id.orderWiseReport).setOnClickListener {
            dashboardData?.let { data ->
                val intent = Intent(this, ActivityReportOrderWise::class.java)
                intent.putExtra("today_orders_count", data.today_orders_count)
                intent.putExtra("yesterday_orders_count", data.yesterday_orders_count)
                intent.putExtra("week_orders_count", data.week_orders_count)
                intent.putExtra("month_orders_count", data.month_orders_count)
                startActivity(intent)
            } ?: Toast.makeText(this, "Please wait, data loading...", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.productWiseReport).setOnClickListener {
            val intent = Intent(this, ActivityProductReport::class.java)
            startActivity(intent)
        }
        findViewById<View>(R.id.dateMonthWiseReport).setOnClickListener {
            val intent = Intent(this, ActivityMonthDateReport::class.java)
            startActivity(intent)
        }


    }
    override fun onResume() {
        super.onResume()
        getDashboardData() // ✅ Refresh data every time user returns
    }

    private fun getDashboardData() {
        val input = Input(status = "1")

        findViewById<View>(R.id.loading_screen).visibility = View.VISIBLE
        ApiClient.instance.dashboard(jwtToken, input)
            .enqueue(object : Callback<DashboardOutput> {
                override fun onResponse(
                    call: Call<DashboardOutput>,
                    response: Response<DashboardOutput>
                ) {
                    findViewById<View>(R.id.loading_screen).visibility = View.GONE

                    if (response.isSuccessful && response.body()?.status == true) {
                        val data = response.body()?.dataList
                        dashboardData = data  // ✅ store data here for later use
                        updateDashboardUI(data)
                    } else {
                        Toast.makeText(
                            this@ActivityReport,
                            "Error: ${response.body()?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DashboardOutput>, t: Throwable) {
                    findViewById<View>(R.id.loading_screen).visibility = View.GONE
                    Toast.makeText(this@ActivityReport, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })

    }

    private fun updateDashboardUI(data: Dashboard?) {
        if (data == null) return

        // Sales summary
        findViewById<TextView>(R.id.saleAmount).text = "₹ ${data.todaySalesAmount ?: "0.00"}"
        findViewById<TextView>(R.id.YestSaleAmount).text = "₹ ${data.yesterday_sales_amount ?: "0.00"}"
        findViewById<TextView>(R.id.weekSaleAmount).text = "₹ ${data.week_sales_amount ?: "0.00"}"
        findViewById<TextView>(R.id.monthSaleAmount).text = "₹ ${data.month_sales_amount ?: "0.00"}"
        findViewById<TextView>(R.id.totalCashAmount).text = "₹ ${data.today_cash_sales_amount ?: "0.00"}"
        findViewById<TextView>(R.id.totalUpiAmount).text = "₹ ${data.today_upi_sales_amount ?: "0.00"}"

    }
}
