package com.example.apitest

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.apitest.helperClass.NavigationActivity
import com.example.apitest.fragment.LowStockListFragment
import com.example.apitest.fragment.StockListFragment

class StockActivity : NavigationActivity() {

    private lateinit var tabStockList: TextView
    private lateinit var tabLowStockList: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock)

        setupBottomNavigation("stock") // using navigation helper class to set up bottom navigation

        tabStockList = findViewById(R.id.tabStockList)
        tabLowStockList = findViewById(R.id.tabLowStockList)


        // Only show fragments if allowed
        if (UserAccess.isStockAllowed) {
            replaceFragment(StockListFragment(), "StockListFragment")
            selectTab(tabStockList)
        } else {
            Toast.makeText(this, "Access restricted", Toast.LENGTH_SHORT).show()
        }


        tabStockList.setOnClickListener {
            if (!UserAccess.isStockAllowed) {
                Toast.makeText(this, "Access restricted", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            replaceFragment(StockListFragment(), "StockListFragment")
            selectTab(tabStockList)
        }

        tabLowStockList.setOnClickListener {
            if (!UserAccess.isStockAllowed) {
                Toast.makeText(this, "Access restricted", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            replaceFragment(LowStockListFragment(), "LowStockListFragment")
            selectTab(tabLowStockList)
        }
    }

    private fun selectTab(selected: TextView) {
        tabStockList.setBackgroundResource(0)
        tabLowStockList.setBackgroundResource(0)

        tabStockList.setTextColor(resources.getColor(R.color.black))
        tabLowStockList.setTextColor(resources.getColor(R.color.black))

        selected.setBackgroundResource(R.drawable.tab_selected_bg)
        selected.setTextColor(resources.getColor(R.color.blue))
    }

    private fun replaceFragment(fragment: Fragment, tag: String? = null) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
        if (tag != null) {
            transaction.addToBackStack(tag) // optional: keeps back navigation
        }
        transaction.commit()
    }


}
