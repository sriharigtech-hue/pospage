    package com.example.apitest

    import android.app.DatePickerDialog
    import android.content.Intent
    import android.os.Bundle
    import android.view.View
    import android.widget.*
    import androidx.recyclerview.widget.RecyclerView
    import android.widget.LinearLayout
    import com.example.apitest.helperClass.NavigationActivity
    import com.example.apitest.dataModel.Input
    import com.example.apitest.dataModel.SaleReportOutput
    import com.example.apitest.network.ApiClient
    import retrofit2.Call
    import retrofit2.Callback
    import retrofit2.Response
    import androidx.recyclerview.widget.LinearLayoutManager
    import com.example.apitest.dataModel.ReportOutput
    import com.example.apitest.adapter.OrderListReportAdapter
    import com.example.apitest.helperClass.DialogCustomReport
    import java.util.*

    class ActivityMonthDateReport : NavigationActivity() {

        private val jwtToken =
            "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZTRjY2JlNDBmYWFiZmE5YTg1YmVkMDdjOTcyMzAzNWY3MTljNzQ4M2Q3YzIzMjg3M2E5ZjZmN2QyMjQ1Y2E3MjVhNjVlMTQ5ZGRiZjBkYjgiLCJpYXQiOjE3NjE2MzM3OTIuNDI5OTk5LCJuYmYiOjE3NjE2MzM3OTIuNDMwMDAzLCJleHAiOjE3OTMxNjk3OTIuNDIyNjY5LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.SutcB4SMDdEzgfDZ1lnzGDjY4gf1aSwNJMGFnKF3RXWGbB4MhP6R8wRLCa2wtCMZ1egUQc1LxPMq2S9P-hfCeh4stj4W-zlWf0xOfvswvWVos14aP5RBTjbZ8Rc3PrmAwiFvnzu7PpRtqy8hGF_h1nt2dJrZpT4k0CIuvpUK8afLBKYfpr0i1NOx1awb2Z6Uo5YfJRQrB7y4wrs8TEsnbuAIdC4JG6-9Oi_TPwZ42SGatw4UEUvm09I3SjKGjZRDCOMJZLbSc1O4C6B53aK9hNQKCjnwsSXWc37h-cA6lKg-DSfm6K1usg0yHeAsE-2uya2_b8_TNq3LN4Mb04S820FmpnP0RqtDoPeoqBUSTacd0bSINimYpNv8NyiaO0D6k0J3HzaNd5MmwORyHpTNnVEaM8l0O5iyI-UIa3bPaOBnltamiAydE2EmUA9pfRQy3HWd6yZnxfuITM0THdj73ju1Qh3D0WVP7aUFR-3XUbp5qVflBZkiBe0klClG94ubWNFMX6vebixLQ21KsDvEDLj2Xy0hoLY-g6sm33l14NwbSiUgZ0VQI_3WbOzSpUvU0sprU7ozX7y7-nwjIXjshOQ5ymktZUMCN7LyCbvgX-qcGyxbS8C9JN9BhmvhagIBatDpPZw75arXlksHzxKnytbLG3BVxFHXxkp9jSBqW2s"

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_month_date_report)
            setupBottomNavigation("report")

            findViewById<View>(R.id.backButton).setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }


            findViewById<View>(R.id.dateMonthWiseReport).setOnClickListener {
                val intent = Intent(this, ActivityProductReport::class.java)
                startActivity(intent)
            }
            findViewById<View>(R.id.orderWiseReport).setOnClickListener {
                val intent = Intent(this, ActivityReport::class.java)
                startActivity(intent)
            }
            findViewById<View>(R.id.productWiseReport).setOnClickListener {
                val intent = Intent(this, ActivityReportOrderWise::class.java)
                startActivity(intent)
            }
            findViewById<View>(R.id.customizedReport).setOnClickListener {
                DialogCustomReport.showCustomizedReportDialog(this)
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
            val noListError = findViewById<LinearLayout>(R.id.no_list_error)
            val resultLayout = findViewById<RelativeLayout>(R.id.resultLayout)
            val resultLayout1 = findViewById<LinearLayout>(R.id.resultLayout1)
            val transactions = findViewById<RecyclerView>(R.id.transactions)
            transactions.layoutManager = LinearLayoutManager(this)

            // --- TextViews for API result ---
            val totalSalesAmount = findViewById<TextView>(R.id.totalSalesAmount)
            val totalCashAmount = findViewById<TextView>(R.id.totalCashAmount)
            val totalUpiAmount = findViewById<TextView>(R.id.totalUpiAmount)
            val totalOrders = findViewById<TextView>(R.id.totalOrders)
            val cashUpiOrders = findViewById<TextView>(R.id.cashUpiOrders)
            val cashOrders = findViewById<TextView>(R.id.cashOrders)
            val upiOrders = findViewById<TextView>(R.id.upiOrders)

            // --- Spinner: Months ---
            val months = listOf(
                "Select Month",
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

            // --- Default view ---
            monthRadio.isChecked = true
            spinnerLayout.visibility = View.VISIBLE
            fromDateLayout.visibility = View.GONE
            titleContent.text = "Select Month"

            // --- Radio button switch logic ---
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.monthRadio -> {
                        spinnerLayout.visibility = View.VISIBLE
                        fromDateLayout.visibility = View.GONE
                        titleContent.text = "Select Month"
                        noListError.visibility = View.GONE
                        resultLayout.visibility = View.GONE
                        resultLayout1.visibility = View.GONE
                        findViewById<RelativeLayout>(R.id.reportListLayout).visibility = View.VISIBLE
                        transactions.adapter = OrderListReportAdapter(emptyList()) // Clear old list
                    }
                    R.id.dateRadio -> {
                        spinnerLayout.visibility = View.GONE
                        fromDateLayout.visibility = View.VISIBLE
                        titleContent.text = "Select Date"
                        noListError.visibility = View.GONE
                        resultLayout.visibility = View.GONE
                        resultLayout1.visibility = View.GONE
                        transactions.adapter = OrderListReportAdapter(emptyList()) // Clear old list

                        // ✅ Show today's date automatically
                        val today = getCurrentDate()
                        fromDate.text = today

                        // ✅ Immediately fetch today’s report
                        fetchSalesReport(
                            type = "date",
                            value = today,
                            totalSalesAmount,
                            totalCashAmount,
                            totalUpiAmount,
                            totalOrders,
                            cashUpiOrders,
                            cashOrders,
                            upiOrders,
                            noListError,
                            resultLayout,
                            resultLayout1
                        )
                    }
                }
            }


            // --- Month selection event ---
            monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position > 0) {
                        val selectedMonth = months[position]
                        fetchSalesReport(
                            type = "month",
                            value = selectedMonth,
                            totalSalesAmount,
                            totalCashAmount,
                            totalUpiAmount,
                            totalOrders,
                            cashUpiOrders,
                            cashOrders,
                            upiOrders,
                            noListError,
                            resultLayout,
                            resultLayout1
                        )
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            // --- Date picker event ---
            fromDateIcon.setOnClickListener {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(
                    this,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                        fromDate.text = formattedDate

                        // Fetch date-based report
                        fetchSalesReport(
                            type = "date",
                            value = formattedDate,
                            totalSalesAmount,
                            totalCashAmount,
                            totalUpiAmount,
                            totalOrders,
                            cashUpiOrders,
                            cashOrders,
                            upiOrders,
                            noListError,
                            resultLayout,
                            resultLayout1
                        )
                    },
                    year, month, day
                )
                // ✅ Prevent future date selection (tomorrow and beyond)
                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
                datePickerDialog.show()
            }
        }

        // --- Common function for both month and date-based reports ---
        private fun fetchSalesReport(
            type: String,
            value: String,
            totalSalesAmount: TextView,
            totalCashAmount: TextView,
            totalUpiAmount: TextView,
            totalOrders: TextView,
            cashUpiOrders: TextView,
            cashOrders: TextView,
            upiOrders: TextView,
            noListError: LinearLayout,
            reportLayout: RelativeLayout,
            resultLayout1: LinearLayout
        ) {

            val input = Input().apply {
                status = "1"
                if (type == "month") {
                    month = value.lowercase()
                    date = ""
                } else {
                    date = value
                    month = ""
                }
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



                                noListError.visibility = View.GONE
                                reportLayout.visibility = View.VISIBLE
                                resultLayout1.visibility = View.VISIBLE
                                fetchOrderList(value, resultLayout1, findViewById(R.id.transactions))
                                findViewById<RelativeLayout>(R.id.reportListLayout).visibility =
                                    if (type == "date") View.GONE else View.VISIBLE

                            } else {
                                noListError.visibility = View.VISIBLE
                                reportLayout.visibility = View.GONE
                                resultLayout1.visibility = View.GONE

                                resetReportValues(totalSalesAmount, totalCashAmount, totalUpiAmount, totalOrders, cashUpiOrders, cashOrders, upiOrders)

                                // ✅ Show reportListLayout only when type == "date"
                                if (type == "date") {
                                    findViewById<RelativeLayout>(R.id.reportListLayout).visibility = View.VISIBLE
                                }
                            }
                        } else {
                            noListError.visibility = View.VISIBLE
                            reportLayout.visibility = View.GONE
                            resultLayout1.visibility = View.GONE
                            resetReportValues(totalSalesAmount, totalCashAmount, totalUpiAmount, totalOrders, cashUpiOrders, cashOrders, upiOrders)
                            if (type == "date") {
                                findViewById<RelativeLayout>(R.id.reportListLayout).visibility = View.VISIBLE
                            }
                        }
                    }

                    override fun onFailure(call: Call<SaleReportOutput>, t: Throwable) {
                        noListError.visibility = View.VISIBLE
                        reportLayout.visibility = View.GONE
                        resultLayout1.visibility = View.GONE
                        resetReportValues(totalSalesAmount, totalCashAmount, totalUpiAmount, totalOrders, cashUpiOrders, cashOrders, upiOrders)
                        if (type == "date") {
                            findViewById<RelativeLayout>(R.id.reportListLayout).visibility = View.VISIBLE
                        }
                    }
                })
        }

        private fun resetReportValues(
            totalSalesAmount: TextView,
            totalCashAmount: TextView,
            totalUpiAmount: TextView,
            totalOrders: TextView,
            cashUpiOrders: TextView,
            cashOrders: TextView,
            upiOrders: TextView
        ) {
            totalSalesAmount.text = "₹ 0.00"
            totalCashAmount.text = "₹ 0.00"
            totalUpiAmount.text = "₹ 0.00"
            totalOrders.text = "0"
            cashUpiOrders.text = "0"
            cashOrders.text = "0"
            upiOrders.text = "0"
        }


        private fun getCurrentDate(): String {
            val calendar = java.util.Calendar.getInstance()
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH) + 1
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            return String.format("%04d-%02d-%02d", year, month, day)
        }

        private fun fetchOrderList(date: String, resultLayout1: LinearLayout, transactions: RecyclerView) {

            val input = Input().apply {
                this.date = date
                this.status = "1"
            }

            ApiClient.instance.reportApi(jwtToken, input)
                .enqueue(object : Callback<ReportOutput> {
                    override fun onResponse(call: Call<ReportOutput>, response: Response<ReportOutput>) {
                        if (response.isSuccessful && response.body()?.status == true) {

                            val list = response.body()!!.data?.filterNotNull() ?: emptyList()

                            if (list.isNotEmpty()) {
                                resultLayout1.visibility = View.VISIBLE
                                transactions.adapter = OrderListReportAdapter(list)
                            } else {
                                resultLayout1.visibility = View.GONE
                            }

                        } else {
                            resultLayout1.visibility = View.GONE
                        }
                    }

                    override fun onFailure(call: Call<ReportOutput>, t: Throwable) {
                        resultLayout1.visibility = View.GONE
                    }
                })
        }



    }
