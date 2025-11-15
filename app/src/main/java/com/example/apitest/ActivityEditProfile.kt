package com.example.apitest

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import androidx.appcompat.app.AppCompatActivity
import com.example.apitest.dataModel.Input
import com.example.apitest.network.ApiClient
import com.example.apitest.dataModel.PrinterList
import com.example.apitest.dataModel.PrinterOutput
import com.example.apitest.dataModel.ProfileOutput
import com.github.angads25.toggle.widget.LabeledSwitch
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.provider.MediaStore
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.AdapterView
import com.example.apitest.dataModel.StatusResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActivityEditProfile : AppCompatActivity() {
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZTRjY2JlNDBmYWFiZmE5YTg1YmVkMDdjOTcyMzAzNWY3MTljNzQ4M2Q3YzIzMjg3M2E5ZjZmN2QyMjQ1Y2E3MjVhNjVlMTQ5ZGRiZjBkYjgiLCJpYXQiOjE3NjE2MzM3OTIuNDI5OTk5LCJuYmYiOjE3NjE2MzM3OTIuNDMwMDAzLCJleHAiOjE3OTMxNjk3OTIuNDIyNjY5LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.SutcB4SMDdEzgfDZ1lnzGDjY4gf1aSwNJMGFnKF3RXWGbB4MhP6R8wRLCa2wtCMZ1egUQc1LxPMq2S9P-hfCeh4stj4W-zlWf0xOfvswvWVos14aP5RBTjbZ8Rc3PrmAwiFvnzu7PpRtqy8hGF_h1nt2dJrZpT4k0CIuvpUK8afLBKYfpr0i1NOx1awb2Z6Uo5YfJRQrB7y4wrs8TEsnbuAIdC4JG6-9Oi_TPwZ42SGatw4UEUvm09I3SjKGjZRDCOMJZLbSc1O4C6B53aK9hNQKCjnwsSXWc37h-cA6lKg-DSfm6K1usg0yHeAsE-2uya2_b8_TNq3LN4Mb04S820FmpnP0RqtDoPeoqBUSTacd0bSINimYpNv8NyiaO0D6k0J3HzaNd5MmwORyHpTNnVEaM8l0O5iyI-UIa3bPaOBnltamiAydE2EmUA9pfRQy3HWd6yZnxfuITM0THdj73ju1Qh3D0WVP7aUFR-3XUbp5qVflBZkiBe0klClG94ubWNFMX6vebixLQ21KsDvEDLj2Xy0hoLY-g6sm33l14NwbSiUgZ0VQI_3WbOzSpUvU0sprU7ozX7y7-nwjIXjshOQ5ymktZUMCN7LyCbvgX-qcGyxbS8C9JN9BhmvhagIBatDpPZw75arXlksHzxKnytbLG3BVxFHXxkp9jSBqW2s"
    private lateinit var shopImage: com.makeramen.roundedimageview.RoundedImageView
    private lateinit var printerSpinner: Spinner
    private lateinit var nameField: TextInputEditText
    private lateinit var inchargeNameField: TextInputEditText
    private lateinit var emailField: TextInputEditText
    private lateinit var mobileField: TextInputEditText
    private lateinit var resellerCodeField: TextInputEditText
    private lateinit var addressField: TextInputEditText
    private lateinit var billPrefixField: TextInputEditText
    private lateinit var footerText1Field: TextInputEditText
    private lateinit var footerText2Field: TextInputEditText
    // Language toggle
    private lateinit var englishLayout: RelativeLayout
    private lateinit var otherLayout: RelativeLayout
    private lateinit var iconEnglish: ShapeableImageView
    private lateinit var iconOther: ShapeableImageView

    // Tax switch
    private lateinit var taxSwitch: LabeledSwitch

    // Discount type toggle
    private lateinit var total: RelativeLayout
    private lateinit var individual: RelativeLayout
    private lateinit var taxTotal: RelativeLayout
    private lateinit var taxIndividual: RelativeLayout
    private lateinit var taxicon01: ShapeableImageView
    private lateinit var taxicon11: ShapeableImageView

    // Tax value
    private lateinit var taxValueLayout: View
    private lateinit var taxValue: TextInputEditText
    private lateinit var taxTypeLayout: View
    private val PICK_IMAGE_REQUEST = 1001
    private var selectedImageUri: Uri? = null
    private lateinit var billTitleField: TextInputEditText
    private lateinit var printerItemSpinner: Spinner


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        printerSpinner = findViewById(R.id.printerSpinner)
        val printerConnection1Layout = findViewById<RelativeLayout>(R.id.printerConnection1Layout)

        nameField = findViewById(R.id.name)
        inchargeNameField = findViewById(R.id.inchargename)
        emailField = findViewById(R.id.emailAddress)
        mobileField = findViewById(R.id.mobileNumber)
        resellerCodeField = findViewById(R.id.resellerCode)
        addressField = findViewById(R.id.address)
        billTitleField = findViewById(R.id.billTitle)
        billPrefixField = findViewById(R.id.prefix)
        footerText1Field = findViewById(R.id.footerText1)
        footerText2Field = findViewById(R.id.footerText2)
        printerItemSpinner = findViewById(R.id.printerItemSpinner)


        // Language
        englishLayout = findViewById(R.id.english)
        otherLayout = findViewById(R.id.others)
        iconEnglish = findViewById(R.id.icon01)
        iconOther = findViewById(R.id.icon11)

        // Tax switch
        taxSwitch = findViewById(R.id.on_off_button)

        // Discount type
        total = findViewById(R.id.total)
        individual = findViewById(R.id.individual)


        // Tax type
        taxTotal = findViewById(R.id.taxTotal)
        taxIndividual = findViewById(R.id.taxIndividual)


        taxTypeLayout = findViewById(R.id.tax_layout)



        taxicon01 = findViewById(R.id.taxicon01)
        taxicon11 = findViewById(R.id.taxicon11)

        // Tax value
        taxValueLayout = findViewById(R.id.taxValueLayout)
        taxValue = findViewById(R.id.taxValue)

        taxSwitch.setOnToggledListener { _, isOn ->
            if (isOn) {
                taxValueLayout.visibility = View.VISIBLE
                taxTypeLayout.visibility = View.VISIBLE
                printerConnection1Layout.visibility = View.VISIBLE   //  show full tax type block
            } else {
                taxValueLayout.visibility = View.GONE
                taxTypeLayout.visibility = View.GONE
                printerConnection1Layout.visibility = View.GONE       //  hide full tax type block
            }
        }



        englishLayout.setOnClickListener { setShopLanguage("0") }
        otherLayout.setOnClickListener { setShopLanguage("1") }

        total.setOnClickListener { setDiscountType("1") }
        individual.setOnClickListener { setDiscountType("2") }

        taxTotal.setOnClickListener { setTaxType("total") }
        taxIndividual.setOnClickListener { setTaxType("individual") }


        shopImage = findViewById(R.id.shopImage)

        val editImageButton = findViewById<View>(R.id.editImage)
        editImageButton.setOnClickListener {
            openGallery() // open gallery first
        }


        setupPrinterItemSpinner()
        loadProfileData()
        loadPrinterSizes()
    }



    // -------------------- LANGUAGE TYPE --------------------

    private fun setShopLanguage(selected: String) {
        if (selected == "0") {
            // English selected
            englishLayout.setBackgroundResource(R.drawable.button_left)
            otherLayout.setBackgroundResource(R.drawable.button_right)

            // Apply tint colors
            englishLayout.backgroundTintList =
                getColorStateList(R.color.white)
            otherLayout.backgroundTintList =
                getColorStateList(R.color.bed7d6d6)

        } else {
            // Other selected
            englishLayout.setBackgroundResource(R.drawable.button_right)
            otherLayout.setBackgroundResource(R.drawable.button_left)

            // Apply tint colors
            englishLayout.backgroundTintList =
                getColorStateList(R.color.bed7d6d6)
            otherLayout.backgroundTintList =
                getColorStateList(R.color.white)

        }
    }

    private fun setTaxType(type: String) {
        if (type == "total") {
            //  Total tax selected
            taxTotal.setBackgroundResource(R.drawable.button_left)
            taxIndividual.setBackgroundResource(R.drawable.button_right)
            taxTotal.backgroundTintList = getColorStateList(R.color.white)
            taxIndividual.backgroundTintList = getColorStateList(R.color.bed7d6d6)

            // 👇 Show tax value only for total type
            taxValueLayout.visibility = View.VISIBLE

        } else {

            //  Individual tax selected
            taxTotal.setBackgroundResource(R.drawable.button_right)
            taxIndividual.setBackgroundResource(R.drawable.button_left)
            taxTotal.backgroundTintList = getColorStateList(R.color.bed7d6d6)
            taxIndividual.backgroundTintList = getColorStateList(R.color.white)

            //  Hide tax value for individual type
            taxValueLayout.visibility = View.GONE
        }

    }


    // -------------------- DISCOUNT TYPE --------------------
    private fun setDiscountType(discountType: String) {
        if (discountType == "1") {
            //  Total selected
            total.setBackgroundResource(R.drawable.button_left)
            individual.setBackgroundResource(R.drawable.button_right)

            total.backgroundTintList = getColorStateList(R.color.white)
            individual.backgroundTintList = getColorStateList(R.color.bed7d6d6)


        } else {
            //  Individual selected
            total.setBackgroundResource(R.drawable.button_right)
            individual.setBackgroundResource(R.drawable.button_left)

            total.backgroundTintList = getColorStateList(R.color.bed7d6d6)
            individual.backgroundTintList = getColorStateList(R.color.white)

        }
    }


    // -------------------- TAX SWITCH --------------------
    private fun setTaxStatus(shopTax: String?) {
        val taxAmount = shopTax?.toDoubleOrNull() ?: 0.0
        val isOn = taxAmount > 0.0

        taxSwitch.isOn = isOn
        taxValueLayout.visibility = if (isOn) View.VISIBLE else View.GONE
        taxTypeLayout.visibility = if (isOn) View.VISIBLE else View.GONE

        if (isOn) {
            taxValue.setText(String.format("%.2f", taxAmount))
        } else {
            taxValue.text?.clear()
        }
    }



    private fun loadPrinterSizes() {
        ApiClient.instance.getPrinterSize()?.enqueue(object : Callback<PrinterOutput?> {
            override fun onResponse(
                call: Call<PrinterOutput?>,
                response: Response<PrinterOutput?>
            ) {
                if (response.isSuccessful && response.body()?.status == true) {
                    val printerList: List<PrinterList> = response.body()?.data ?: emptyList()
                    setupSpinner(printerList)
                } else {
                    Toast.makeText(
                        this@ActivityEditProfile,
                        "Failed to load printer sizes",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<PrinterOutput?>, t: Throwable) {
                Toast.makeText(
                    this@ActivityEditProfile,
                    "Network error: ${t.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
    // -------------------- PROFILE DETAILS --------------------
    private fun loadProfileData() {
        val input = Input(status = "1")

        ApiClient.instance.getUserDetails(jwtToken, input)
            ?.enqueue(object : Callback<ProfileOutput?> {
                override fun onResponse(
                    call: Call<ProfileOutput?>,
                    response: Response<ProfileOutput?>
                ) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        val user = response.body()?.userDetails
                        user?.let {
                            if (!it.shop_image.isNullOrEmpty()) {
                                Glide.with(this@ActivityEditProfile)
                                    .load(it.shop_image)
                                    .placeholder(R.mipmap.ic_launcher_round) // default while loading
                                    .error(R.mipmap.ic_launcher_round)       // fallback if URL invalid
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(shopImage)
                            }

                            nameField.setText(it.name ?: "")
                            inchargeNameField.setText(it.inchargeName ?: "")
                            emailField.setText(it.userEmail ?: "")
                            mobileField.setText(it.userPhoneNumber ?: "")
                            resellerCodeField.setText(it.reseller_code?.takeLast(4) ?: "")
                            addressField.setText(it.address ?: "")
                            billTitleField.setText(it.customized_bill_title ?: "")
                            billPrefixField.setText(it.bill_number_prefix ?: "")
                            footerText1Field.setText(user.footer_text1 ?: "")
                            footerText2Field.setText(user.footer_text2 ?: "")

                            setShopLanguage(it.shop_language ?: "0")
                            setTaxStatus(it.shop_tax?.toString() ?: "0")
                            setDiscountType(it.discount_type ?: "1")
                        }
                    } else {
                        Toast.makeText(
                            this@ActivityEditProfile,
                            response.body()?.message ?: "Failed to load profile",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ProfileOutput?>, t: Throwable) {
                    Toast.makeText(
                        this@ActivityEditProfile,
                        "Network error: ${t.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun setupSpinner(printers: List<PrinterList>) {

        val printerNames = printers.map { it.printerSize ?: "-" }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            printerNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        printerSpinner.adapter = adapter
    }



    private fun showImageUploadDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_upload_shop_image, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val shopImageDialog = dialogView.findViewById<com.makeramen.roundedimageview.RoundedImageView>(R.id.shopImage)
        val saveButton = dialogView.findViewById<View>(R.id.save)
        val cancelButton = dialogView.findViewById<View>(R.id.cancel)

        // Display the selected image
        selectedImageUri?.let {
            Glide.with(this)
                .load(it)
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.ic_launcher_round)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(shopImageDialog)
        }

        // Cancel button closes dialog
        cancelButton.setOnClickListener { dialog.dismiss() }

        // Save button uploads image and closes dialog
        saveButton.setOnClickListener {
            if (selectedImageUri != null) {
                val part = prepareFilePart(selectedImageUri!!, "shop_image")

                if (part != null) {
                    uploadShopImage(part)
                    dialog.dismiss() // only once
                } else {
                    Toast.makeText(this, "Failed to prepare image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
            }
        }


        dialog.show()
    }


    private fun prepareFilePart(uri: Uri, paramName: String): MultipartBody.Part? {
        return try {
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }

            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, bos) // always PNG
            val bitmapData = bos.toByteArray()

            val requestFile = RequestBody.create("image/png".toMediaTypeOrNull(), bitmapData)
            MultipartBody.Part.createFormData(paramName, "shop_image.png", requestFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



    private fun uploadShopImage(imagePart: MultipartBody.Part) {
        ApiClient.instance.addShopImage(jwtToken, imagePart)
            .enqueue(object : Callback<StatusResponse> {
                override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        Toast.makeText(this@ActivityEditProfile, "Image uploaded successfully", Toast.LENGTH_SHORT).show()

                        // Update main activity image instantly
                        selectedImageUri?.let {
                            Glide.with(this@ActivityEditProfile)
                                .load(it)
                                .placeholder(R.mipmap.ic_launcher_round)
                                .into(shopImage)
                        }
                    } else {
                        Toast.makeText(this@ActivityEditProfile, response.body()?.message ?: "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                    Toast.makeText(this@ActivityEditProfile, "Network error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun getFileExtension(uri: Uri): String? {
        val mimeType = contentResolver.getType(uri)
        return if (mimeType == "image/png") "png" else null
    }



    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/png" // only PNG
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(Intent.createChooser(intent, "Select PNG Image"), PICK_IMAGE_REQUEST)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                val ext = getFileExtension(imageUri)
                if (ext != null) {
                    selectedImageUri = imageUri
                    showImageUploadDialog()
                } else {
                    Toast.makeText(this, "Please select a PNG or JPG image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun setupPrinterItemSpinner() {
        val itemSizes = arrayListOf("Normal", "Medium", "Large")

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            itemSizes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        printerItemSpinner.adapter = adapter

        printerItemSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


}