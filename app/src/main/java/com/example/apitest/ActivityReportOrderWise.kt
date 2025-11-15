package com.example.apitest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import com.example.apitest.helperClass.DialogCustomReport
import com.example.apitest.helperClass.NavigationActivity

class ActivityReportOrderWise : NavigationActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_order_wise)
        setupBottomNavigation("report")
        findViewById<View>(R.id.backButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 🟢 Get values passed from ActivityReport
        val todayCount = intent.getStringExtra("today_orders_count") ?: "0"
        val yesterdayCount = intent.getStringExtra("yesterday_orders_count") ?: "0"
        val weekCount = intent.getStringExtra("week_orders_count") ?: "0"
        val monthCount = intent.getStringExtra("month_orders_count") ?: "0"

        // 🟢 Show them in the corresponding TextViews (already in XML)
        findViewById<TextView>(R.id.saleAmount).text = todayCount
        findViewById<TextView>(R.id.YestSaleAmount).text = yesterdayCount
        findViewById<TextView>(R.id.weekSaleAmount).text = weekCount
        findViewById<TextView>(R.id.monthSaleAmount).text = monthCount



        findViewById<View>(R.id.productWiseReport).setOnClickListener {
            val intent = Intent(this, ActivityProductReport::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.dateMonthWiseReport).setOnClickListener {
            val intent = Intent(this, ActivityMonthDateReport::class.java)
            startActivity(intent)
        }
        findViewById<View>(R.id.customizedReport).setOnClickListener {
            DialogCustomReport.showCustomizedReportDialog(this)
        }



        //  When clicking first layout (Sales Wise Report), go back to ActivityReport
        findViewById<View>(R.id.orderWiseReport).setOnClickListener {
            val intent = Intent(this, ActivityReport::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish() // optional to prevent multiple stack entries
        }
    }
}
