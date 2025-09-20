package com.example.apitest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.apitest.fragment.CategoryFragment
import com.example.apitest.fragment.ItemsFragment
import com.example.apitest.fragment.SubCategoryFragment
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var tabItems: TextView
    private lateinit var tabSubCategory: TextView
    private lateinit var tabCategory: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabItems = findViewById(R.id.tabItems)
        tabSubCategory = findViewById(R.id.tabSubCategory)
        tabCategory = findViewById(R.id.tabCategory)

        // Default fragment
        replaceFragment(ItemsFragment())

        tabItems.setOnClickListener {
            replaceFragment(ItemsFragment())
            selectTab(tabItems)
        }
        tabSubCategory.setOnClickListener {
            replaceFragment(SubCategoryFragment())
            selectTab(tabSubCategory)
        }
        tabCategory.setOnClickListener {
            replaceFragment(CategoryFragment())
            selectTab(tabCategory)
        }
    }


    private fun selectTab(selected: TextView) {
        tabItems.setBackgroundResource(0)
        tabSubCategory.setBackgroundResource(0)
        tabCategory.setBackgroundResource(0)

        selected.setBackgroundResource(R.drawable.tab_selected_bg)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
