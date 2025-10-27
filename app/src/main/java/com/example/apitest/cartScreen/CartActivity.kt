package com.example.apitest.cartScreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.CartItem
import com.example.apitest.helperClass.NavigationActivity
import com.google.android.material.textfield.TextInputLayout
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.core.view.isVisible

class CartActivity : NavigationActivity() {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private val cartList = mutableListOf<CartItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        setupBottomNavigation("cart")

        // 🔹 Back button click
        val backButton: RelativeLayout = findViewById(R.id.backButton)
        backButton.setOnClickListener { finishWithResult() }

        // 🔹 Recycler setup
        cartRecyclerView = findViewById(R.id.cart_list)
        cartRecyclerView.layoutManager = LinearLayoutManager(this)

        // 🔹 Receive items from POSActivity
        val receivedItems = intent.getParcelableArrayListExtra<CartItem>("cart_items")
        if (!receivedItems.isNullOrEmpty()) cartList.addAll(receivedItems)

        // 🔹 Setup adapter
        cartAdapter = CartAdapter(
            cartList,
            onItemUpdated = { /* local update */ },
            onItemDeleted = { /* local delete */ }
        )
        cartRecyclerView.adapter = cartAdapter

        // 🔹 Add Customer section toggle
        val addCustomer = findViewById<AppCompatTextView>(R.id.add_customer)
        val mobileLayout = findViewById<TextInputLayout>(R.id.mobileLayout)
        val nameLayout = findViewById<TextInputLayout>(R.id.nameLayout)
        val addressLayout = findViewById<TextInputLayout>(R.id.addressLayout)
        val locationRecyclerView = findViewById<RecyclerView>(R.id.locationRecyclerView)

        var isVisible = false

        addCustomer.setOnClickListener {
            TransitionManager.beginDelayedTransition(findViewById(android.R.id.content), AutoTransition())
            isVisible = !isVisible

            mobileLayout.isVisible = isVisible
            nameLayout.isVisible = isVisible
            addressLayout.isVisible = isVisible
            locationRecyclerView.isVisible = isVisible

            addCustomer.text = if (isVisible) "Hide" else "Add"
        }
    }

    // 🔹 Send cart back to POSActivity when leaving
    private fun finishWithResult() {
        val resultIntent = Intent()
        resultIntent.putParcelableArrayListExtra("updated_cart_items", ArrayList(cartList))
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onBackPressed() {
        finishWithResult()
    }
}
