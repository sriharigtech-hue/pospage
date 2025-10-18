package com.example.apitest.helperClass

import android.content.Intent
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.example.apitest.MainActivity
import com.example.apitest.POSActivity
import com.example.apitest.R
import com.example.apitest.StockActivity
import com.example.apitest.UserAccess

open class NavigationActivity : AppCompatActivity() {

    var currentTab: String? = null
/*    object UserAccess {
        var isStockAllowed = true
    }*/

    protected fun setupBottomNavigation(tab: String) {
        currentTab = tab

        val inventoryBtn = findViewById<LinearLayout>(R.id.inventory_button)
        val stockBtn = findViewById<LinearLayout>(R.id.web_button)
        val posBtn = findViewById<RelativeLayout>(R.id.sale_but)

        val inventoryIcon = inventoryBtn.findViewById<AppCompatImageView>(R.id.inventory_icon)
        val stockIcon = stockBtn.findViewById<AppCompatImageView>(R.id.stock_icon)
        val posIcon = posBtn.findViewById<AppCompatImageView>(R.id.sale_icon)

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

    }
}
