package com.example.apitest

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.apitest.fragment.LowStockListFragment
import com.example.apitest.fragment.StockListFragment

class StockActivity : AppCompatActivity() {

    private lateinit var inventoryButton: LinearLayout
    private lateinit var stockButton: LinearLayout
    private lateinit var homeButton: LinearLayout
    private lateinit var profileButton: LinearLayout

    private lateinit var inventoryText: TextView
    private lateinit var stockText: TextView
    private lateinit var homeText: TextView
    private lateinit var profileText: TextView

    private lateinit var tabStockList: TextView
    private lateinit var tabLowStockList: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock)
        tabStockList = findViewById(R.id.tabStockList)
        tabLowStockList = findViewById(R.id.tabLowStockList)

        // Default fragment
        replaceFragment(StockListFragment())
        selectTab(tabStockList)

        tabStockList.setOnClickListener {
            replaceFragment(StockListFragment())
            selectTab(tabStockList)
        }

        tabLowStockList.setOnClickListener {
            replaceFragment(LowStockListFragment())
            selectTab(tabLowStockList)
        }




        // Bottom navigation buttons
        inventoryButton = findViewById(R.id.inventory_button)
        stockButton = findViewById(R.id.web_button)
        homeButton = findViewById(R.id.home_button)
        profileButton = findViewById(R.id.profile_button)

        // Grab TextViews inside each button
        inventoryText = inventoryButton.getChildAt(1) as TextView
        stockText = stockButton.getChildAt(1) as TextView
        homeText = homeButton.getChildAt(1) as TextView
        profileText = profileButton.getChildAt(1) as TextView

        // Highlight Stock tab
        highlightBottomNav(stockText)

        // Navigation click listeners
        inventoryButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
            finish()
        }

        stockButton.setOnClickListener {
            // Already in StockActivity → do nothing
        }

        homeButton.setOnClickListener {
            // Navigate to Home/Report
        }

        profileButton.setOnClickListener {
            // Navigate to Settings
        }
    }

    private fun highlightBottomNav(selected: TextView) {
        // Reset all tabs to grey
        val grey = resources.getColor(R.color.grey9599AB)
        val accent = resources.getColor(R.color.colorAccent)

        inventoryText.setTextColor(grey)
        stockText.setTextColor(grey)
        homeText.setTextColor(grey)
        profileText.setTextColor(grey)

        // Highlight selected
        selected.setTextColor(accent)
    }

    private fun selectTab(selected: TextView) {
        tabStockList.setBackgroundResource(0)
        tabLowStockList.setBackgroundResource(0)

        tabStockList.setTextColor(resources.getColor(R.color.black))
        tabLowStockList.setTextColor(resources.getColor(R.color.black))

        selected.setBackgroundResource(R.drawable.tab_selected_bg)
        selected.setTextColor(resources.getColor(R.color.blue))
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

}
