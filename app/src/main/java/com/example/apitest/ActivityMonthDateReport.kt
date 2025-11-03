package com.example.apitest

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.apitest.helperClass.NavigationActivity
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Spinner
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.apitest.dataModel.Input
import com.example.apitest.dataModel.SaleReportOutput
import com.example.apitest.network.ApiClient

class ActivityMonthDateReport : NavigationActivity() {
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZTRjY2JlNDBmYWFiZmE5YTg1YmVkMDdjOTcyMzAzNWY3MTljNzQ4M2Q3YzIzMjg3M2E5ZjZmN2QyMjQ1Y2E3MjVhNjVlMTQ5ZGRiZjBkYjgiLCJpYXQiOjE3NjE2MzM3OTIuNDI5OTk5LCJuYmYiOjE3NjE2MzM3OTIuNDMwMDAzLCJleHAiOjE3OTMxNjk3OTIuNDIyNjY5LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.SutcB4SMDdEzgfDZ1lnzGDjY4gf1aSwNJMGFnKF3RXWGbB4MhP6R8wRLCa2wtCMZ1egUQc1LxPMq2S9P-hfCeh4stj4W-zlWf0xOfvswvWVos14aP5RBTjbZ8Rc3PrmAwiFvnzu7PpRtqy8hGF_h1nt2dJrZpT4k0CIuvpUK8afLBKYfpr0i1NOx1awb2Z6Uo5YfJRQrB7y4wrs8TEsnbuAIdC4JG6-9Oi_TPwZ42SGatw4UEUvm09I3SjKGjZRDCOMJZLbSc1O4C6B53aK9hNQKCjnwsSXWc37h-cA6lKg-DSfm6K1usg0yHeAsE-2uya2_b8_TNq3LN4Mb04S820FmpnP0RqtDoPeoqBUSTacd0bSINimYpNv8NyiaO0D6k0J3HzaNd5MmwORyHpTNnVEaM8l0O5iyI-UIa3bPaOBnltamiAydE2EmUA9pfRQy3HWd6yZnxfuITM0THdj73ju1Qh3D0WVP7aUFR-3XUbp5qVflBZkiBe0klClG94ubWNFMX6vebixLQ21KsDvEDLj2Xy0hoLY-g6sm33l14NwbSiUgZ0VQI_3WbOzSpUvU0sprU7ozX7y7-nwjIXjshOQ5ymktZUMCN7LyCbvgX-qcGyxbS8C9JN9BhmvhagIBatDpPZw75arXlksHzxKnytbLG3BVxFHXxkp9jSBqW2s"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_month_date_report)
        setupBottomNavigation("report")

        findViewById<View>(R.id.backButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // --- View references ---
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val monthRadio = findViewById<RadioButton>(R.id.monthRadio)
        val dateRadio = findViewById<RadioButton>(R.id.dateRadio)
        val spinnerLayout = findViewById<LinearLayout>(R.id.spinnerLayout)
        val fromDateLayout = findViewById<RelativeLayout>(R.id.fromDateLayout)
        val titleContent = findViewById<TextView>(R.id.titleContent)
        val monthSpinner = findViewById<Spinner>(R.id.amount_spinner)
        val fromDate = findViewById<TextView>(R.id.fromDate)
        val fromDateIcon = findViewById<View>(R.id.fromDateIcon)

        // TextViews for result
        val totalSalesAmount = findViewById<TextView>(R.id.totalSalesAmount)
        val totalCashAmount = findViewById<TextView>(R.id.totalCashAmount)
        val totalUpiAmount = findViewById<TextView>(R.id.totalUpiAmount)
        val totalOrders = findViewById<TextView>(R.id.totalOrders)
        val cashUpiOrders = findViewById<TextView>(R.id.cashUpiOrders)
        val cashOrders = findViewById<TextView>(R.id.cashOrders)
        val upiOrders = findViewById<TextView>(R.id.upiOrders)

        // --- List of months ---
        val months = listOf(
            "january", "february", "march", "april", "may", "june",
            "july", "august", "september", "october", "november", "december"
        )
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            months
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpinner.adapter = adapter

        monthSpinner.setSelection(0)
        monthRadio.isChecked = true
        // --- Default view ---
        spinnerLayout.visibility = View.VISIBLE
        fromDateLayout.visibility = View.GONE
        titleContent.text = "Select Month"

        // --- Radio button change logic ---
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.monthRadio -> {
                    spinnerLayout.visibility = View.VISIBLE
                    fromDateLayout.visibility = View.GONE
                    titleContent.text = "Select Month"
                }
                R.id.dateRadio -> {
                    spinnerLayout.visibility = View.GONE
                    fromDateLayout.visibility = View.VISIBLE
                    titleContent.text = "Select Date"
                }
            }
        }

        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val selectedMonth = months[position]
                    fetchSalesReport(selectedMonth,
                        totalSalesAmount,
                        totalCashAmount,
                        totalUpiAmount,
                        totalOrders,
                        cashUpiOrders,
                        cashOrders,
                        upiOrders
                    )
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        fromDateIcon.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH)
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

            val datePickerDialog = android.app.DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    fromDate.text = formattedDate
                },
                year, month, day
            )
            datePickerDialog.show()
        }
    }

    private fun fetchSalesReport(
        month: String,
        totalSalesAmount: TextView,
        totalCashAmount: TextView,
        totalUpiAmount: TextView,
        totalOrders: TextView,
        cashUpiOrders: TextView,
        cashOrders: TextView,
        upiOrders: TextView
    ) {
        val input = Input().apply {
            status = "1"          // ✅ required by API
            date = ""             // ✅ empty value as per API
            this.month = month.lowercase() // ✅ dynamic month from spinner
        }

        ApiClient.instance.salesBasedReport(jwtToken, input)
            .enqueue(object : Callback<SaleReportOutput> {
                override fun onResponse(call: Call<SaleReportOutput>, response: Response<SaleReportOutput>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == true && body.data != null) {
                            val data = body.data
                            totalSalesAmount.text = "₹ ${data?.total_sales_amount ?: "0.00"}"
                            totalCashAmount.text = "₹ ${data?.total_cash_sales_amount ?: "0.00"}"
                            totalUpiAmount.text = "₹ ${data?.total_upi_sales_amount ?: "0.00"}"
                            totalOrders.text = "${data?.total_orders ?: 0}"
                            cashUpiOrders.text = "${data?.total_cash_upi_orders ?: 0}"
                            cashOrders.text = "${data?.total_cash_orders ?: 0}"
                            upiOrders.text = "${data?.total_upi_orders ?: 0}"
                        } else {
                            Toast.makeText(this@ActivityMonthDateReport, body?.message ?: "No data found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@ActivityMonthDateReport, "Server error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SaleReportOutput>, t: Throwable) {
                    Toast.makeText(this@ActivityMonthDateReport, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

}