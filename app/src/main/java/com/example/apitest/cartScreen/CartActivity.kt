package com.example.apitest.cartScreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.CartItem
import com.example.apitest.helperClass.NavigationActivity

class CartActivity : NavigationActivity() {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private val cartList = mutableListOf<CartItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        setupBottomNavigation("cart")

        // Back button click
        val backButton: RelativeLayout = findViewById(R.id.backButton)
        backButton.setOnClickListener { finishWithResult() }

        // Setup recycler
        cartRecyclerView = findViewById(R.id.cart_list)
        cartRecyclerView.layoutManager = LinearLayoutManager(this)

        // Receive items from POSActivity
        val receivedItems = intent.getParcelableArrayListExtra<CartItem>("cart_items")
        if (!receivedItems.isNullOrEmpty()) cartList.addAll(receivedItems)

        // Setup adapter
        cartAdapter = CartAdapter(
            cartList,
            onItemUpdated = { /* just update list locally */ },
            onItemDeleted = { /* same here */ }
        )
        cartRecyclerView.adapter = cartAdapter
    }

    // When back is pressed or back button tapped, send data back to POSActivity
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
