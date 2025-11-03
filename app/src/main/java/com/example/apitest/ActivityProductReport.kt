package com.example.apitest

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.adapter.ProductReportAdapter
import com.example.apitest.dataModel.Input
import com.example.apitest.dataModel.ProductReportList
import com.example.apitest.dataModel.ProductReportOutput
import com.example.apitest.helperClass.NavigationActivity
import com.example.apitest.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActivityProductReport : NavigationActivity() {

    private lateinit var resultDateText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var noListError: LinearLayout
    private lateinit var resultLayout: LinearLayout

    private lateinit var adapter: ProductReportAdapter
    private var reportList: MutableList<ProductReportList> = mutableListOf()

    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZTRjY2JlNDBmYWFiZmE5YTg1YmVkMDdjOTcyMzAzNWY3MTljNzQ4M2Q3YzIzMjg3M2E5ZjZmN2QyMjQ1Y2E3MjVhNjVlMTQ5ZGRiZjBkYjgiLCJpYXQiOjE3NjE2MzM3OTIuNDI5OTk5LCJuYmYiOjE3NjE2MzM3OTIuNDMwMDAzLCJleHAiOjE3OTMxNjk3OTIuNDIyNjY5LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.SutcB4SMDdEzgfDZ1lnzGDjY4gf1aSwNJMGFnKF3RXWGbB4MhP6R8wRLCa2wtCMZ1egUQc1LxPMq2S9P-hfCeh4stj4W-zlWf0xOfvswvWVos14aP5RBTjbZ8Rc3PrmAwiFvnzu7PpRtqy8hGF_h1nt2dJrZpT4k0CIuvpUK8afLBKYfpr0i1NOx1awb2Z6Uo5YfJRQrB7y4wrs8TEsnbuAIdC4JG6-9Oi_TPwZ42SGatw4UEUvm09I3SjKGjZRDCOMJZLbSc1O4C6B53aK9hNQKCjnwsSXWc37h-cA6lKg-DSfm6K1usg0yHeAsE-2uya2_b8_TNq3LN4Mb04S820FmpnP0RqtDoPeoqBUSTacd0bSINimYpNv8NyiaO0D6k0J3HzaNd5MmwORyHpTNnVEaM8l0O5iyI-UIa3bPaOBnltamiAydE2EmUA9pfRQy3HWd6yZnxfuITM0THdj73ju1Qh3D0WVP7aUFR-3XUbp5qVflBZkiBe0klClG94ubWNFMX6vebixLQ21KsDvEDLj2Xy0hoLY-g6sm33l14NwbSiUgZ0VQI_3WbOzSpUvU0sprU7ozX7y7-nwjIXjshOQ5ymktZUMCN7LyCbvgX-qcGyxbS8C9JN9BhmvhagIBatDpPZw75arXlksHzxKnytbLG3BVxFHXxkp9jSBqW2s"
    private val reportOptions = listOf("Today", "Yesterday", "This Week", "This Month")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_report)
        setupBottomNavigation("report")

        // Back button
        findViewById<View>(R.id.backButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        resultDateText = findViewById(R.id.resultDate)
        recyclerView = findViewById(R.id.transactions)
        noListError = findViewById(R.id.no_list_error)
        resultLayout = findViewById(R.id.resultLayout)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProductReportAdapter(reportList)
        recyclerView.adapter = adapter

        setupSpinner()
    }

    private fun setupSpinner() {
        val spinner = findViewById<Spinner>(R.id.amount_spinner)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            reportOptions
        )
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = reportOptions[position]
                resultDateText.text = "Report - ($selected)"

                val reportDateValue = when (selected) {
                    "Today" -> "1"
                    "Yesterday" -> "2"
                    "This Week" -> "3"
                    "This Month" -> "4"
                    else -> "1"
                }

                fetchProductReport(reportDateValue)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun fetchProductReport(reportDate: String) {
        resultLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        noListError.visibility = View.GONE

        val input = Input(status = "1", report_date = reportDate)

        ApiClient.instance.productBasedReport(jwtToken, input)
            .enqueue(object : Callback<ProductReportOutput> {
                override fun onResponse(
                    call: Call<ProductReportOutput>,
                    response: Response<ProductReportOutput>
                ) {

                    if (response.isSuccessful && response.body()?.status == true) {
                        val dataList = response.body()?.data?.filterNotNull()
                        if (!dataList.isNullOrEmpty()) {
                            recyclerView.visibility = View.VISIBLE
                            noListError.visibility = View.GONE
                            reportList.clear()
                            reportList.addAll(dataList)
                            adapter.notifyDataSetChanged()
                        } else {
                            recyclerView.visibility = View.GONE
                            noListError.visibility = View.VISIBLE
                        }
                    } else {
                        recyclerView.visibility = View.GONE
                        noListError.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<ProductReportOutput>, t: Throwable) {
                    recyclerView.visibility = View.GONE
                    noListError.visibility = View.VISIBLE
                    Toast.makeText(this@ActivityProductReport, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
