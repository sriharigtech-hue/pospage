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

    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiOTMyZGFhM2IyNGZmM2Q0ZTY5NmQwYTdlODdhNmY1Y2ViOTA1Y2VhMzc0NWU5NmFlMTBhZWEwZjY2MDkxZDZiMWI1MjIwYjIwYmU4N2EyNDEiLCJpYXQiOjE3NTg3OTI2NDEuMjExMDEsIm5iZiI6MTc1ODc5MjY0MS4yMTEwMTMsImV4cCI6MTc5MDMyODY0MS4yMDQ5NjYsInN1YiI6IjYiLCJzY29wZXMiOltdfQ.lcZz0mc3u-to55t0p23p5g_Z1NQHM23K6xT8vaRAr7X9qHsMbyN9OEq-KuHhZGaqikcY-7ak26uM3_mtoLpLfq6jivhUPFC06JL7ID3HRHOUsWY05CqjIwPpOfjop127eWyz1CYmBmZx3mKsSuE2rvNK91OsROryf1Dz-Px0SnVJ1uDJGK9y1zsJ_Mi8wY7WIH_E3cBusI7uSgNHTQq7dh6GurCYymPxnvvR8cdMQ4Om9SnmfqX9f3GCUncHXBVfxYCuH6ElLsjq74Z9ZXRGBLdwdM4BJWiyv4jzKfU80LmErPo7XT90DPzqa40T0pRblACQsaSGLn64TBoKvxlsO4HjJV8nBg3az5PrCkDsj8QTwgPLzJtP7WcT3pvcCI6O3O8OKlL2lR2-csFHNzCHetHaT1fmOLnVWuc5YIjqhYFEOjp8IKVhzxmcocxsd3R8bcvrjR8NcutOX5H7zmfD-GX17f64RT2c0zqSRdVpRxFZYlNycxd3rI591w9ImgZSkeGQN4Eg8us8oqmlRqfF5mO7QZXi_OsjJgnMdovqP1NB1IxHuTHQIyfESkQ3DoA_KYBF4_8DXhyjvE2D5_SQfZitUpjSwfWqZ_ghgVoOdLJokz4TBJQ9j_ec4jK3uf3nCwS_6Evx9zwbZxmin2CpnIrg4lFRmMpO6YgLfYKPqoI"
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
