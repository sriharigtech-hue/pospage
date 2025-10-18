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

    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZGE0YjJmNjlmZGJkNjMwMDMyNGE3MWNkZWRhMDI2ZWI2YTIwMGM4NWIyNTI2MTNjOTZhZGIyMDA2MTE3YjMxMGI0MTFjYjczNzNmZmNlZDAiLCJpYXQiOjE3NjAzMjkzNTkuNDc3MTA1LCJuYmYiOjE3NjAzMjkzNTkuNDc3MTA4LCJleHAiOjE3OTE4NjUzNTkuNDcyNjI1LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.C3ySWdDX7BRHm4qzwWFZZofL_DEx3C2Qjy7iEUWxy9GdrL8OJS7m7Kk_Oe4HtFaT7DvPMEWE_c9kIC8RalMXflXTvPGKkfsw7yxdVxZOKSE20UNZiSbScAdvx3RxAz-XoHK4wJr7wepspLad5y5KCv4RyPXAJl8sIjFELfiCMoxt1CiYGp5_GhsOjbMeSLWSBoDwd3H4MLNvUyU2KN2zhvQaRRUh4T-L11mZgmd_8A8kWZbp_bO6AK-3hGHFGd7VaT2Xqoi4asmn0ABlxusVYWG6hw9UhnU-_uxOVFQLAHog-WKfbahCwfkssXtK07wMpk-ZGHfRn7ujbkrMAX5gNgNkcNQZPRMkUSrokHylEJXKC7UOAgUiK8fy32bIlmFuMQE9hTuuQjHWJ8hdEqtPaXVIcc1oXURtZhCWTp2APH9RE4_L41NYStog_bVMdXwRO_a6QEg_ex0moqxwtRZKivnIF4DKm6WLj45X0FLj-F7HTlZ-eoc9j3w_dVaVyhhxEKUiTyQSJ_AwVKMTbAUmxvWY3OnoIAmu4WYrbC4T4tA2cWoB9yXKna8Yfbil_vC46tLZweGF7RRZR2MPT16q-iCzKG73JqAMphV4NO7b-bMk6mhvgz8TR0_YUewsPg2CVvgdvEmnV4DE4znhnwiLMniN0kPGzF5pindkKTVNDb8"
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
