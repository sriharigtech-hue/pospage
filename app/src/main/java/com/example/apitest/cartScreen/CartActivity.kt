package com.example.apitest.cartScreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import kotlin.text.toDoubleOrNull
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.example.apitest.R
import com.example.apitest.dataModel.CartItem
import com.example.apitest.dataModel.Input
import com.example.apitest.dataModel.ProfileOutput
import com.example.apitest.dataModel.UserDetails
import com.example.apitest.helperClass.NavigationActivity
import com.example.apitest.network.ApiClient
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartActivity : NavigationActivity() {

    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZTRjY2JlNDBmYWFiZmE5YTg1YmVkMDdjOTcyMzAzNWY3MTljNzQ4M2Q3YzIzMjg3M2E5ZjZmN2QyMjQ1Y2E3MjVhNjVlMTQ5ZGRiZjBkYjgiLCJpYXQiOjE3NjE2MzM3OTIuNDI5OTk5LCJuYmYiOjE3NjE2MzM3OTIuNDMwMDAzLCJleHAiOjE3OTMxNjk3OTIuNDIyNjY5LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.SutcB4SMDdEzgfDZ1lnzGDjY4gf1aSwNJMGFnKF3RXWGbB4MhP6R8wRLCa2wtCMZ1egUQc1LxPMq2S9P-hfCeh4stj4W-zlWf0xOfvswvWVos14aP5RBTjbZ8Rc3PrmAwiFvnzu7PpRtqy8hGF_h1nt2dJrZpT4k0CIuvpUK8afLBKYfpr0i1NOx1awb2Z6Uo5YfJRQrB7y4wrs8TEsnbuAIdC4JG6-9Oi_TPwZ42SGatw4UEUvm09I3SjKGjZRDCOMJZLbSc1O4C6B53aK9hNQKCjnwsSXWc37h-cA6lKg-DSfm6K1usg0yHeAsE-2uya2_b8_TNq3LN4Mb04S820FmpnP0RqtDoPeoqBUSTacd0bSINimYpNv8NyiaO0D6k0J3HzaNd5MmwORyHpTNnVEaM8l0O5iyI-UIa3bPaOBnltamiAydE2EmUA9pfRQy3HWd6yZnxfuITM0THdj73ju1Qh3D0WVP7aUFR-3XUbp5qVflBZkiBe0klClG94ubWNFMX6vebixLQ21KsDvEDLj2Xy0hoLY-g6sm33l14NwbSiUgZ0VQI_3WbOzSpUvU0sprU7ozX7y7-nwjIXjshOQ5ymktZUMCN7LyCbvgX-qcGyxbS8C9JN9BhmvhagIBatDpPZw75arXlksHzxKnytbLG3BVxFHXxkp9jSBqW2s"

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private val cartList = mutableListOf<CartItem>()
    private var profileOutput: ProfileOutput? = null

    private lateinit var selectionBG: LinearLayout // sales/expense layout
    private lateinit var addItemButton: AppCompatTextView
    private lateinit var paymentStatusLayout: LinearLayout
    private lateinit var subTotalText: AppCompatTextView
    private lateinit var discountText: AppCompatTextView
    private lateinit var totalText: AppCompatTextView
    private lateinit var removeCouponText: AppCompatTextView

    // Flags
    private var taxEnabled = false
    private var mrpEnabled = false
    private var wholeEnabled = false
    private var unitEnabled = false
    private var customProductEnabled = false


    //  added layouts for tax
    private lateinit var taxLayout: RelativeLayout
    private lateinit var sgstTaxLayout: RelativeLayout
    private lateinit var cgstTaxLayout: RelativeLayout
    private lateinit var totalAmountLayout: RelativeLayout

    private lateinit var addDiscountLayout: AppCompatTextView
    private lateinit var acLayout: RelativeLayout
    private lateinit var acAmountLayout: RelativeLayout
    private lateinit var billingAmtText: AppCompatTextView
    private lateinit var balanceBillText: AppCompatTextView
    private lateinit var unpaidBtn: AppCompatTextView

    private var currentSubTotal = 0.0
    private var currentAcCharge = 0.0
    private var currentDiscount = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        setupBottomNavigation("cart")
        subTotalText = findViewById(R.id.sub_total)
        discountText = findViewById(R.id.discount)
        totalText = findViewById(R.id.total_bill)
        taxLayout = findViewById(R.id.taxLayout)
        sgstTaxLayout = findViewById(R.id.sgstTaxLayout)
        cgstTaxLayout = findViewById(R.id.cgstTaxLayout)
        totalAmountLayout = findViewById(R.id.total_bill_layout)
        addDiscountLayout = findViewById(R.id.add_discount) // your layout for Add Discount section
        discountText = findViewById(R.id.discount)
        acLayout = findViewById(R.id.ac_layout)
        acLayout = findViewById(R.id.ac_layout)
        acAmountLayout = findViewById(R.id.ac_amount_layout)
        acAmountLayout.visibility = View.GONE  // hide by default
        billingAmtText = findViewById(R.id.billing_amt)
        balanceBillText = findViewById(R.id.balance_bill)
        unpaidBtn = findViewById(R.id.unpaid)

        addDiscountLayout.visibility = View.GONE // default hidden
        acLayout.visibility = View.GONE // default hidden
        val paidBtn = findViewById<AppCompatTextView>(R.id.paid)
        val unpaidBtn = findViewById<AppCompatTextView>(R.id.unpaid)
        val billingAmtText = findViewById<AppCompatTextView>(R.id.billing_amt)
        val balanceBillText = findViewById<AppCompatTextView>(R.id.balance_bill)

        val backButton: RelativeLayout = findViewById(R.id.backButton)
        backButton.setOnClickListener { finishWithResult() }

        selectionBG = findViewById(R.id.selectionBG)
        addItemButton = findViewById(R.id.addItem)
        addItemButton.visibility = View.GONE
        paymentStatusLayout = findViewById(R.id.paymentStatusLayout)
        paymentStatusLayout.visibility = View.GONE // default hidden

        cartRecyclerView = findViewById(R.id.cart_list)
        cartRecyclerView.layoutManager = LinearLayoutManager(this)
        removeCouponText = findViewById(R.id.removeCoupon)
        removeCouponText.visibility = View.GONE  // default hidden

        removeCouponText.setOnClickListener {
            removeDiscount()
        }

        val receivedItems = intent.getParcelableArrayListExtra<CartItem>("cart_items")
        if (!receivedItems.isNullOrEmpty()) cartList.addAll(receivedItems)
        billingAmtText.text = "0.00"
        balanceBillText.text = String.format("%.2f", getCurrentBillingAmount())
        unpaidBtn.setOnClickListener {
            val billingAmount = getCurrentBillingAmount()
            billingAmtText.text = "0.00"
            balanceBillText.text = String.format("%.2f", billingAmount)
            Toast.makeText(this, "Marked as UNPAID", Toast.LENGTH_SHORT).show()
        }
// 🔹 Handle PAID click
        paidBtn.setOnClickListener {
            val billingAmount = getCurrentBillingAmount()
            billingAmtText.text = String.format("%.2f", billingAmount)
            balanceBillText.text = "0.00"
            Toast.makeText(this, "Marked as PAID", Toast.LENGTH_SHORT).show()
        }


        cartAdapter = CartAdapter(
            cartList,
            onItemUpdated = {  updateSubTotal()},
            onItemDeleted = { updateSubTotal() },
            discountType = "0"
        )
        cartRecyclerView.adapter = cartAdapter

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

        loadUserProfile()
        updateSubTotal()

    }
    // 🔹 Function to calculate total billing amount dynamically
    fun getCurrentBillingAmount(): Double {
        return currentSubTotal + currentAcCharge - currentDiscount
    }




    //  Fetch Profile API
    private fun loadUserProfile() {
        val input = Input(status = "1")

        ApiClient.instance.getUserDetails(jwtToken, input)
            ?.enqueue(object : Callback<ProfileOutput?> {
                override fun onResponse(call: Call<ProfileOutput?>, response: Response<ProfileOutput?>) {
                    if (response.isSuccessful && response.body() != null) {
                        profileOutput = response.body()
                        val userDetails = profileOutput?.userDetails
                        handleUserDetailsResponse(userDetails)
                    } else {
                        Toast.makeText(this@CartActivity, "Failed to fetch profile", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ProfileOutput?>, t: Throwable) {
                    Toast.makeText(this@CartActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    //  Apply profile flags & setup custom button
    private fun handleUserDetailsResponse(userDetails: UserDetails?) {
        selectionBG.visibility = if (userDetails?.estimation_bill_status == "0") View.GONE else View.VISIBLE
        paymentStatusLayout.visibility = if (userDetails?.advance_payment_status == "1") View.VISIBLE else View.GONE

        customProductEnabled = userDetails?.custom_product_status == "1"
        taxEnabled = userDetails?.product_tax_status == "1"
        mrpEnabled = userDetails?.mrp_price_status == "1"
        wholeEnabled = userDetails?.whole_sale_price_status == "1"
        unitEnabled = userDetails?.unit_status == "1"

        val discountType = userDetails?.discount_type ?: "0"

        // --- Discount type handling ---
        if (discountType == "1") {
            //  Total discount → show Add Discount button on cart screen
            addDiscountLayout.visibility = View.VISIBLE
            addDiscountLayout.setOnClickListener {
                showCartDiscountDialog()
            }
        } else {
            //  Product-wise or none → hide total Add Discount button
            addDiscountLayout.visibility = View.GONE
        }

        // --- Update adapter with correct discount type ---
        cartAdapter = CartAdapter(
            cartList,
            onItemUpdated = { updateSubTotal() },
            onItemDeleted = { updateSubTotal() },
            discountType = discountType
        )
        cartRecyclerView.adapter = cartAdapter

        // --- Tax section handling ---
        if (userDetails?.tax_status == "1") {
            taxLayout.visibility = View.VISIBLE
            sgstTaxLayout.visibility = View.VISIBLE
            cgstTaxLayout.visibility = View.VISIBLE
            totalAmountLayout.visibility = View.GONE
        } else {
            taxLayout.visibility = View.GONE
            sgstTaxLayout.visibility = View.GONE
            cgstTaxLayout.visibility = View.GONE
            totalAmountLayout.visibility = View.VISIBLE
        }

        // --- Custom product handling ---
        if (customProductEnabled) {
            addItemButton.visibility = View.VISIBLE
            addItemButton.setOnClickListener { showCustomItemDialog() }
        } else {
            addItemButton.visibility = View.GONE
        }
        //--- ac / non ac ----
        if (userDetails?.ac_status == "1") {
            acLayout.visibility = View.VISIBLE

            val acButton = findViewById<RelativeLayout>(R.id.ac)
            val nonAcButton = findViewById<RelativeLayout>(R.id.non_ac)
            val acTotalText = findViewById<AppCompatTextView>(R.id.ac_total)

            // Default selection → Non-AC
            var selectedType = "NON_AC"
            highlightAcSelection(acButton, nonAcButton, selectedType)

            nonAcButton.setOnClickListener {
                selectedType = "NON_AC"
                highlightAcSelection(acButton, nonAcButton, selectedType)
                updateAcTotalInBill(0.0)
                acTotalText.text = "0.00"
                acAmountLayout.visibility = View.GONE   // hide when Non-AC selected
            }


            acButton.setOnClickListener {
                // 🔹 Don't immediately select AC — open dialog first
                showAcDialog(acTotalText, acAmountLayout) { acEntered ->
                    if (acEntered) {
                        // ✅ If value entered, then select AC
                        selectedType = "AC"
                        highlightAcSelection(acButton, nonAcButton, selectedType)
                    } else {
                        // ❌ If dialog canceled or invalid, keep NON-AC selected
                        selectedType = "NON_AC"
                        highlightAcSelection(acButton, nonAcButton, selectedType)
                    }
                }
            }


        } else {
            acLayout.visibility = View.GONE
        }


    }
    // 🔹 Toggle background highlight for AC / Non-AC selection
    private fun highlightAcSelection(
        acButton: RelativeLayout,
        nonAcButton: RelativeLayout,
        selectedType: String
    ) {
        if (selectedType == "AC") {
            acButton.setBackgroundResource(R.drawable.light_blue)
            nonAcButton.setBackgroundResource(R.drawable.button_left)
        } else {
            nonAcButton.setBackgroundResource(R.drawable.light_blue)
            acButton.setBackgroundResource(R.drawable.button_left)
        }
    }



    //  Custom Item Dialog (same logic as POSActivity)
    private fun showCustomItemDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_custom_box, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val nameEdit = dialogView.findViewById<EditText>(R.id.name)
        val qtyEdit = dialogView.findViewById<EditText>(R.id.qty)
        val priceEdit = dialogView.findViewById<EditText>(R.id.price)
        val unitSpinner = dialogView.findViewById<Spinner>(R.id.unitSpinner)

        val priceTaxLayout = dialogView.findViewById<View>(R.id.priceTaxLayout)
        val mrpLayout = dialogView.findViewById<View>(R.id.mrpPriceLayout)
        val wholeLayout = dialogView.findViewById<View>(R.id.productWholeSalePriceLayout)
        val unitLayout = dialogView.findViewById<View>(R.id.unitSpinnerLayout)
        val unitTitleLayout = dialogView.findViewById<View>(R.id.unitSpinnerTitleLayout)

        priceTaxLayout.visibility = if (taxEnabled) View.VISIBLE else View.GONE
        mrpLayout.visibility = if (mrpEnabled) View.VISIBLE else View.GONE
        wholeLayout.visibility = if (wholeEnabled) View.VISIBLE else View.GONE
        unitLayout.visibility = if (unitEnabled) View.VISIBLE else View.GONE
        unitTitleLayout.visibility = if (unitEnabled) View.VISIBLE else View.GONE

        if (unitEnabled) fetchUnits(unitSpinner)

        val yesBtn = dialogView.findViewById<AppCompatTextView>(R.id.yes_btn)
        val noBtn = dialogView.findViewById<AppCompatTextView>(R.id.no_btn)

        noBtn.setOnClickListener { dialog.dismiss() }

        yesBtn.setOnClickListener {
            val name = nameEdit.text.toString().trim()
            val qty = qtyEdit.text.toString().trim().toDoubleOrNull()
            val price = priceEdit.text.toString().trim().toDoubleOrNull()

            if (name.isEmpty() || qty == null || price == null) {
                Toast.makeText(this, "Please fill valid fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newItem = CartItem(
                productId = null,
                name = name,
                quantity = qty,
                price = price,
                isCustom = true
            )
            cartList.add(newItem)
            cartAdapter.notifyItemInserted(cartList.size - 1)
            updateSubTotal()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun fetchUnits(spinner: Spinner) {
        val input = Input(status = "1")
        ApiClient.instance.unitApi(jwtToken, input)
            .enqueue(object : Callback<com.example.apitest.dataModel.UnitOutput> {
                override fun onResponse(call: Call<com.example.apitest.dataModel.UnitOutput>, response: Response<com.example.apitest.dataModel.UnitOutput>) {
                    val units = response.body()?.unitList ?: emptyList()
                    val adapter = ArrayAdapter(
                        this@CartActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        units.map { it.unitName ?: "N/A" }
                    )
                    spinner.adapter = adapter
                }

                override fun onFailure(call: Call<com.example.apitest.dataModel.UnitOutput>, t: Throwable) {}
            })
    }

    // 🔹 Send updated cart back
    private fun finishWithResult() {
        val resultIntent = Intent()
        resultIntent.putParcelableArrayListExtra("updated_cart_items", ArrayList(cartList))
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }


    private fun updateSubTotal() {
        var total = 0.0
        var totalDiscount = 0.0

        for (item in cartList) {
            val price = item.price ?: 0.0
            val qty = item.quantity ?: 0.0
            val itemTotal = price * qty
            total += itemTotal

            if (item.discountValue != null && item.discountType != null) {
                val discount = if (item.discountType == "%") {
                    (itemTotal * item.discountValue!!) / 100
                } else {
                    item.discountValue!!
                }
                totalDiscount += discount
            }
        }

        val subTotal = total
        val discountAmount = totalDiscount
//        val taxableAmount = subTotal - discountAmount

        // 🔹 Get dynamic shop tax from profile
//        val shopTaxPercent = profileOutput?.userDetails?.shop_tax?.toString()?.toDoubleOrNull() ?: 0.0
//        val halfTaxPercent = shopTaxPercent / 2

        // 🔹 Calculate tax amounts
//        val totalTaxAmount = (taxableAmount * shopTaxPercent) / 100 //(eg:product price : 500 , discount : 0 , tax : 25% , then totalTaxAmount = 125)
//        val sgstAmount = (taxableAmount * halfTaxPercent) / 100
//        val cgstAmount = (taxableAmount * halfTaxPercent) / 100
//        val grandTotal = taxableAmount + totalTaxAmount

        // 🔹 Update base UI
        subTotalText.text = String.format("%.2f", subTotal)
        discountText.text = String.format("%.2f", discountAmount)
        totalText.text = String.format("%.2f", subTotal - discountAmount)
        currentSubTotal = subTotal
        currentDiscount = discountAmount
        updateBillingAmount()
        toggleTotalBillLayoutVisibility()



        // 🔹 Update tax-related text views if exist
//        findViewById<AppCompatTextView?>(R.id.tax_total)?.text = String.format("%.2f", totalTaxAmount)
//        findViewById<AppCompatTextView?>(R.id.sgstTax_total)?.text = String.format("%.2f", sgstAmount)
//        findViewById<AppCompatTextView?>(R.id.cgstTax_total)?.text = String.format("%.2f", cgstAmount)

        // 🔹 Update tax labels dynamically
//        findViewById<AppCompatTextView?>(R.id.taxTextView)?.text = "Total Tax (${shopTaxPercent.toInt()}%)"
//        findViewById<AppCompatTextView?>(R.id.sgstTaxTextView)?.text = "SGST (${halfTaxPercent.toInt()}%)"
//        findViewById<AppCompatTextView?>(R.id.cgstTaxTextView)?.text = "CGST (${halfTaxPercent.toInt()}%)"

        // 🔹 Show/Hide tax layout dynamically if tax = 0.00
//        if (shopTaxPercent == 0.0) {
//            taxLayout.visibility = View.GONE
//            sgstTaxLayout.visibility = View.GONE
//            cgstTaxLayout.visibility = View.GONE
//            totalAmountLayout.visibility = View.VISIBLE
//        } else {
//            taxLayout.visibility = View.VISIBLE
//            sgstTaxLayout.visibility = View.VISIBLE
//            cgstTaxLayout.visibility = View.VISIBLE
//            totalAmountLayout.visibility = View.GONE
//        }
    }

    //  Show Total Discount Dialog (for discountType = "1")
    private fun showCartDiscountDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_discount, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val btnFlat = dialogView.findViewById<TextView>(R.id.flat)
        val btnPercentage = dialogView.findViewById<TextView>(R.id.percentage)
        val edtDiscount = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.discountValue)
        val btnEnter = dialogView.findViewById<TextView>(R.id.enter)
        val btnCancel = dialogView.findViewById<androidx.appcompat.widget.AppCompatImageView>(R.id.cancel)
        val hintLayout = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.hint)

        var isPercentage = false
        btnFlat.setBackgroundResource(R.drawable.gradient_bg)
        btnFlat.setTextColor(getColor(R.color.white))
        hintLayout.hint = "Enter Flat Discount Amount *"

        btnFlat.setOnClickListener {
            isPercentage = false
            btnFlat.setBackgroundResource(R.drawable.gradient_bg)
            btnFlat.setTextColor(getColor(R.color.white))
            btnPercentage.setBackgroundColor(getColor(android.R.color.transparent))
            btnPercentage.setTextColor(getColor(R.color.colorPrimary))
            hintLayout.hint = "Enter Flat Discount Amount *"
        }

        btnPercentage.setOnClickListener {
            isPercentage = true
            btnPercentage.setBackgroundResource(R.drawable.gradient_bg)
            btnPercentage.setTextColor(getColor(R.color.white))
            btnFlat.setBackgroundColor(getColor(android.R.color.transparent))
            btnFlat.setTextColor(getColor(R.color.colorPrimary))
            hintLayout.hint = "Enter Percentage Discount *"
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnEnter.setOnClickListener {
            val discountValue = edtDiscount.text.toString().toDoubleOrNull()
            if (discountValue == null || discountValue <= 0) {
                Toast.makeText(this, "Enter valid discount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            applyTotalDiscount(discountValue, isPercentage)
            dialog.dismiss()
        }

        dialog.show()
    }

//  Apply total discount logic (with validation fix)
private fun applyTotalDiscount(value: Double, isPercentage: Boolean) {
    var total = 0.0
    for (item in cartList) {
        total += (item.price ?: 0.0) * (item.quantity ?: 0.0)
    }

    // Calculate discount
    var discountAmount = if (isPercentage) (total * value / 100) else value
    if (discountAmount > total) {
        discountAmount = total
        Toast.makeText(this, "Discount cannot exceed subtotal", Toast.LENGTH_SHORT).show()
    }

    val newTotal = total - discountAmount

    // ✅ Update display
    discountText.text = String.format("%.2f", discountAmount)
    totalText.text = String.format("%.2f", newTotal)

    // ✅ Update stored values
    currentDiscount = discountAmount
    currentSubTotal = total

    // ✅ Always update Billing Amount
    updateBillingAmount()

    removeCouponText.visibility = View.VISIBLE
    toggleTotalBillLayoutVisibility()

}


    private fun removeDiscount() {
        var total = 0.0
        for (item in cartList) {
            total += (item.price ?: 0.0) * (item.quantity ?: 0.0)
        }

        discountText.text = "0.00"
        totalText.text = String.format("%.2f", total)
        currentDiscount = 0.0
        updateBillingAmount()

        removeCouponText.visibility = View.GONE
        toggleTotalBillLayoutVisibility()


        Toast.makeText(this, "Discount removed", Toast.LENGTH_SHORT).show()
    }
    // 🔹 Show AC Dialog
// 🔹 Show AC Dialog
    private fun showAcDialog(acTotalText: AppCompatTextView, acAmountLayout: RelativeLayout,   onResult: (Boolean) -> Unit)
    {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_qty, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val submitBtn = dialogView.findViewById<TextView>(R.id.enter)
        val inputLayout = dialogView.findViewById<TextInputLayout>(R.id.hint)
        val valueEditText = dialogView.findViewById<TextInputEditText>(R.id.value)
        val cancelBtn = dialogView.findViewById<AppCompatImageView>(R.id.cancel)

        inputLayout.hint = "Enter AC Service Charge *"


        cancelBtn.setOnClickListener {
            dialog.dismiss()
            onResult(false)
        }


        submitBtn.setOnClickListener {
            val enteredValue = valueEditText.text?.toString()?.trim()
            if (enteredValue.isNullOrEmpty()) {
                Toast.makeText(this, "Please enter a valid value", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val acValue = enteredValue.toDoubleOrNull()
            if (acValue == null || acValue < 0) {
                Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //  Update AC total TextView
            val acTotalText = findViewById<AppCompatTextView>(R.id.ac_total)
            acTotalText.text = String.format("%.2f", acValue)
            updateAcTotalInBill(acValue)
            acAmountLayout.visibility = View.VISIBLE
            dialog.dismiss()
            onResult(true)
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }


    // Keep last AC value (if needed later)
    private var lastAcValue = 0.0

    private fun updateAcTotalInBill(acValue: Double) {
        // ✅ Update displayed AC total
        val acTotalText = findViewById<AppCompatTextView>(R.id.ac_total)
        acTotalText.text = String.format("%.2f", acValue)

        // ✅ Save for billing formula
        currentAcCharge = acValue
        lastAcValue = acValue

        // ✅ Refresh final billing total
        updateBillingAmount()
    }

    // 🔹 Final Billing Calculation
    private fun updateBillingAmount() {
        val billingTotal = currentSubTotal + currentAcCharge - currentDiscount
        billingAmtText.text = String.format("%.2f", billingTotal)
    }
    // ✅ Show total_bill_layout only if any product or total discount applied
    private fun toggleTotalBillLayoutVisibility() {
        val totalBillLayout = findViewById<RelativeLayout>(R.id.total_bill_layout)
        var hasDiscount = false

        // Check if any product has discount
        for (item in cartList) {
            val itemDiscount = item.discountValue ?: 0.0
            if (itemDiscount > 0) {
                hasDiscount = true
                break
            }
        }

        // Check if total discount applied
        if (currentDiscount > 0.0) {
            hasDiscount = true
        }

        // Show/Hide layout
        totalBillLayout.visibility = if (hasDiscount) View.VISIBLE else View.GONE
    }


    override fun onBackPressed() {
        finishWithResult()
    }
}
