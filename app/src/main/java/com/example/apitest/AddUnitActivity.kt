package com.example.apitest

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apitest.dataModel.InputField
import com.example.apitest.dataModel.StatusResponse
import com.example.apitest.network.ApiClient
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.appcompat.widget.AppCompatTextView

class AddUnitActivity : AppCompatActivity() {

    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZTRjY2JlNDBmYWFiZmE5YTg1YmVkMDdjOTcyMzAzNWY3MTljNzQ4M2Q3YzIzMjg3M2E5ZjZmN2QyMjQ1Y2E3MjVhNjVlMTQ5ZGRiZjBkYjgiLCJpYXQiOjE3NjE2MzM3OTIuNDI5OTk5LCJuYmYiOjE3NjE2MzM3OTIuNDMwMDAzLCJleHAiOjE3OTMxNjk3OTIuNDIyNjY5LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.SutcB4SMDdEzgfDZ1lnzGDjY4gf1aSwNJMGFnKF3RXWGbB4MhP6R8wRLCa2wtCMZ1egUQc1LxPMq2S9P-hfCeh4stj4W-zlWf0xOfvswvWVos14aP5RBTjbZ8Rc3PrmAwiFvnzu7PpRtqy8hGF_h1nt2dJrZpT4k0CIuvpUK8afLBKYfpr0i1NOx1awb2Z6Uo5YfJRQrB7y4wrs8TEsnbuAIdC4JG6-9Oi_TPwZ42SGatw4UEUvm09I3SjKGjZRDCOMJZLbSc1O4C6B53aK9hNQKCjnwsSXWc37h-cA6lKg-DSfm6K1usg0yHeAsE-2uya2_b8_TNq3LN4Mb04S820FmpnP0RqtDoPeoqBUSTacd0bSINimYpNv8NyiaO0D6k0J3HzaNd5MmwORyHpTNnVEaM8l0O5iyI-UIa3bPaOBnltamiAydE2EmUA9pfRQy3HWd6yZnxfuITM0THdj73ju1Qh3D0WVP7aUFR-3XUbp5qVflBZkiBe0klClG94ubWNFMX6vebixLQ21KsDvEDLj2Xy0hoLY-g6sm33l14NwbSiUgZ0VQI_3WbOzSpUvU0sprU7ozX7y7-nwjIXjshOQ5ymktZUMCN7LyCbvgX-qcGyxbS8C9JN9BhmvhagIBatDpPZw75arXlksHzxKnytbLG3BVxFHXxkp9jSBqW2s"
    private lateinit var unitNameEditText: TextInputEditText
    private lateinit var saveButton: AppCompatTextView
    private lateinit var titleText: AppCompatTextView


    private var isEdit = false
    private var unitId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        unitNameEditText = findViewById(R.id.UnitName)
        saveButton = findViewById(R.id.saveButton)
        titleText = findViewById(R.id.titleU)
        // Check if this is edit mode
        isEdit = intent.getBooleanExtra("edit_mode", false) // match the key
        unitId = intent.getStringExtra("unit_id")           // match the key
        val unitName = intent.getStringExtra("unit_name")

        if (isEdit) {
            titleText.text = "Edit Unit"
            saveButton.text = "Update"
            unitNameEditText.setText(unitName ?: "")
        } else {
            titleText.text = "Add Unit"
            saveButton.text = "Save"
        }




        saveButton.setOnClickListener {
            val name = unitNameEditText.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Enter unit name", Toast.LENGTH_SHORT).show()
            } else {
                if (isEdit && !unitId.isNullOrEmpty()) {
                    // Call edit API
                    editUnitApi(unitId!!, name)
                } else {
                    // Call add API
                    addUnitApi(name)
                }
            }
        }
    }

    private fun addUnitApi(unitName: String) {
        val input = InputField(unit_name = unitName, status = "1")
        ApiClient.instance.addUnit(jwtToken, input)?.enqueue(object : Callback<StatusResponse?> {
            override fun onResponse(call: Call<StatusResponse?>, response: Response<StatusResponse?>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(this@AddUnitActivity, "Unit added successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@AddUnitActivity, "Failed to add unit", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StatusResponse?>, t: Throwable) {
                Toast.makeText(this@AddUnitActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun editUnitApi(unitId: String, unitName: String) {
        val input = InputField(unit_id = unitId, unit_name = unitName, status = "1")
        ApiClient.instance.editUnit(jwtToken, input)?.enqueue(object : Callback<StatusResponse?> {
            override fun onResponse(call: Call<StatusResponse?>, response: Response<StatusResponse?>) {
                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(this@AddUnitActivity, "Unit updated successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@AddUnitActivity, "Failed to update unit", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StatusResponse?>, t: Throwable) {
                Toast.makeText(this@AddUnitActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
