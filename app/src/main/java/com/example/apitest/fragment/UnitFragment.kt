package com.example.apitest.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.R
import com.example.apitest.AddUnitActivity
import com.example.apitest.adapter.UnitAdapter
import com.example.apitest.dataModel.Input
import com.example.apitest.dataModel.StatusResponse
import com.example.apitest.dataModel.StatusUpdateInput
import com.example.apitest.dataModel.UnitList
import com.example.apitest.dataModel.UnitOutput
import com.example.apitest.network.ApiClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UnitFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UnitAdapter
    private lateinit var searchView: SearchView
    private var unitList: MutableList<UnitList> = mutableListOf()
    val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiOTMyZGFhM2IyNGZmM2Q0ZTY5NmQwYTdlODdhNmY1Y2ViOTA1Y2VhMzc0NWU5NmFlMTBhZWEwZjY2MDkxZDZiMWI1MjIwYjIwYmU4N2EyNDEiLCJpYXQiOjE3NTg3OTI2NDEuMjExMDEsIm5iZiI6MTc1ODc5MjY0MS4yMTEwMTMsImV4cCI6MTc5MDMyODY0MS4yMDQ5NjYsInN1YiI6IjYiLCJzY29wZXMiOltdfQ.lcZz0mc3u-to55t0p23p5g_Z1NQHM23K6xT8vaRAr7X9qHsMbyN9OEq-KuHhZGaqikcY-7ak26uM3_mtoLpLfq6jivhUPFC06JL7ID3HRHOUsWY05CqjIwPpOfjop127eWyz1CYmBmZx3mKsSuE2rvNK91OsROryf1Dz-Px0SnVJ1uDJGK9y1zsJ_Mi8wY7WIH_E3cBusI7uSgNHTQq7dh6GurCYymPxnvvR8cdMQ4Om9SnmfqX9f3GCUncHXBVfxYCuH6ElLsjq74Z9ZXRGBLdwdM4BJWiyv4jzKfU80LmErPo7XT90DPzqa40T0pRblACQsaSGLn64TBoKvxlsO4HjJV8nBg3az5PrCkDsj8QTwgPLzJtP7WcT3pvcCI6O3O8OKlL2lR2-csFHNzCHetHaT1fmOLnVWuc5YIjqhYFEOjp8IKVhzxmcocxsd3R8bcvrjR8NcutOX5H7zmfD-GX17f64RT2c0zqSRdVpRxFZYlNycxd3rI591w9ImgZSkeGQN4Eg8us8oqmlRqfF5mO7QZXi_OsjJgnMdovqP1NB1IxHuTHQIyfESkQ3DoA_KYBF4_8DXhyjvE2D5_SQfZitUpjSwfWqZ_ghgVoOdLJokz4TBJQ9j_ec4jK3uf3nCwS_6Evx9zwbZxmin2CpnIrg4lFRmMpO6YgLfYKPqoI"


    companion object {
        const val ADD_UNIT_REQUEST_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_unit, container, false)

        recyclerView = view.findViewById(R.id.unitRecyclerView)
        searchView = view.findViewById(R.id.searchView)


        adapter = UnitAdapter(unitList,
            onEditClick = { unit ->
                val intent = Intent(requireContext(), AddUnitActivity::class.java)
                intent.putExtra("edit_mode", true)
                intent.putExtra("unit_id", unit.unitId.toString())
                intent.putExtra("unit_name", unit.unitName)
                startActivityForResult(intent, ADD_UNIT_REQUEST_CODE)
            },
            onDeleteClick = { unit ->
                deleteUnit(unit.unitId.toString())
            }
        )





        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val addButton: FloatingActionButton = view.findViewById(R.id.btnAddUnit)
        addButton.setOnClickListener {
            val intent = Intent(requireContext(), AddUnitActivity::class.java)
            startActivityForResult(intent, ADD_UNIT_REQUEST_CODE)
        }




        setupSearch()
        fetchUnits()

        return view
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.filter(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })
    }

    private fun fetchUnits() {
        val input = Input(status = "1")

        ApiClient.instance.unitApi(jwtToken, input).enqueue(object : Callback<UnitOutput> {
            override fun onResponse(call: Call<UnitOutput>, response: Response<UnitOutput>) {
                if (response.isSuccessful) {
                    val units = response.body()?.unitList ?: emptyList()
                    unitList.clear()
                    unitList.addAll(units)
                    adapter.updateData(unitList)

                }
            }
            override fun onFailure(call: Call<UnitOutput>, t: Throwable) {


            }
        })
    }
    private fun deleteUnit(unitId: String) {
        val input = StatusUpdateInput(unitId = unitId, status = 0)

        ApiClient.instance.deleteUnit(jwtToken, input)
            .enqueue(object : Callback<StatusResponse> {
                override fun onResponse(call: Call<StatusResponse>, response: Response<StatusResponse>) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        Toast.makeText(requireContext(), "Unit deleted successfully", Toast.LENGTH_SHORT).show()
                        fetchUnits()
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete unit", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<StatusResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_UNIT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            fetchUnits() // Refresh the list after adding a new unit
        }
    }
}
