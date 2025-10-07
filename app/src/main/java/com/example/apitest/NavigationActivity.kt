package com.example.apitest

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView

open class NavigationActivity : AppCompatActivity() {

    private var currentTab: String? = null

    protected fun setupBottomNavigation(tab: String) {
        currentTab = tab

        val inventoryBtn = findViewById<LinearLayout>(R.id.inventory_button)
        val stockBtn = findViewById<LinearLayout>(R.id.web_button)
        val posBtn = findViewById<RelativeLayout>(R.id.sale_but)

        val inventoryIcon = inventoryBtn.findViewById<AppCompatImageView>(R.id.inventory_icon)
        val stockIcon = stockBtn.findViewById<AppCompatImageView>(R.id.stock_icon)
        val posIcon = posBtn.findViewById<AppCompatImageView>(R.id.sale_icon)

        fun resetIcons() {
            inventoryIcon?.setColorFilter(getColor(R.color.grey9599AB))
            stockIcon?.setColorFilter(getColor(R.color.grey9599AB))
            posIcon?.setColorFilter(getColor(R.color.grey9599AB))
        }

        resetIcons()

        when (currentTab) {
            "inventory" -> inventoryIcon?.setColorFilter(getColor(R.color.colorAccent))
            "stock" -> stockIcon?.setColorFilter(getColor(R.color.colorAccent))
            "pos" -> posIcon?.setColorFilter(getColor(R.color.colorAccent))
        }



        inventoryBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        stockBtn.setOnClickListener {
            startActivity(Intent(this, StockActivity::class.java))
            finish()
        }

        posBtn.setOnClickListener {
            startActivity(Intent(this, POSActivity::class.java))
            finish()
        }
    }
}


