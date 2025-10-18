package com.example.apitest.cartScreen

import android.os.Bundle
import android.content.Context
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.dataModel.NewProductPrice
import com.example.apitest.helperClass.CartManager
import com.example.apitest.helperClass.NavigationActivity

class CartActivity : NavigationActivity() {

    private lateinit var cartList: RecyclerView
    private lateinit var adapter: CartAdapter
    private var quantityStatus: String = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        setupBottomNavigation("cart")
        quantityStatus = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getString("quantity_status", "0") ?: "0"

        cartList = findViewById(R.id.cart_list)
        cartList.layoutManager = LinearLayoutManager(this)
        loadCart()

        val backButton: RelativeLayout = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish() // closes CartActivity and returns to POSActivity
        }

        CartManager.cartChangedLiveData.observe(this) { isChanged ->
            if (isChanged != null) {
                refreshCart()
            }
        }

        setupBottomNavigation("cart")

    }


    private fun loadCart() {
        val normalProducts = filterCartProducts(CartManager.allProductsMap.values)
        val customProducts = CartManager.getAllCustomProducts().toMutableList()

        adapter = CartAdapter(normalProducts, customProducts, {
            // This is called on quantity change (safe, doesn't cause duplication)
            updateCartTotals()
        }, quantityStatus)

        cartList.adapter = adapter
    }


    override fun onResume() {
        super.onResume()
        refreshCart()
    }


    private fun updateCartTotals() {
        // Implement logic here to update your total amount TextView or item count TextView.
        // For example: totalAmountTextView.text = "₹%.2f".format(CartManager.getTotalAmount())
    }

    private fun refreshCart() {
        val normalProducts = filterCartProducts(CartManager.allProductsMap.values)
        val customProducts = CartManager.getAllCustomProducts().toMutableList()
        customProducts.removeAll { it.quantity <= 0.0 }
        adapter.updateData(normalProducts, customProducts)
        updateCartTotals()
    }

    private fun filterCartProducts(products: Collection<NewProductPrice>): MutableList<NewProductPrice> {
        val isFractional = quantityStatus == "1"
        return products
            .distinctBy { "${it.productId}_${it.productPriceId}" } // ✅ Prevent duplicate key products
            .filter { product ->
                val qty = if (isFractional) product.selectedQuantityDecimal else product.selectedQuantity.toDouble()
                qty > 0.0
            }
            .toMutableList()
    }

}