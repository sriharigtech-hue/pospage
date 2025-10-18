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
    val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZGE0YjJmNjlmZGJkNjMwMDMyNGE3MWNkZWRhMDI2ZWI2YTIwMGM4NWIyNTI2MTNjOTZhZGIyMDA2MTE3YjMxMGI0MTFjYjczNzNmZmNlZDAiLCJpYXQiOjE3NjAzMjkzNTkuNDc3MTA1LCJuYmYiOjE3NjAzMjkzNTkuNDc3MTA4LCJleHAiOjE3OTE4NjUzNTkuNDcyNjI1LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.C3ySWdDX7BRHm4qzwWFZZofL_DEx3C2Qjy7iEUWxy9GdrL8OJS7m7Kk_Oe4HtFaT7DvPMEWE_c9kIC8RalMXflXTvPGKkfsw7yxdVxZOKSE20UNZiSbScAdvx3RxAz-XoHK4wJr7wepspLad5y5KCv4RyPXAJl8sIjFELfiCMoxt1CiYGp5_GhsOjbMeSLWSBoDwd3H4MLNvUyU2KN2zhvQaRRUh4T-L11mZgmd_8A8kWZbp_bO6AK-3hGHFGd7VaT2Xqoi4asmn0ABlxusVYWG6hw9UhnU-_uxOVFQLAHog-WKfbahCwfkssXtK07wMpk-ZGHfRn7ujbkrMAX5gNgNkcNQZPRMkUSrokHylEJXKC7UOAgUiK8fy32bIlmFuMQE9hTuuQjHWJ8hdEqtPaXVIcc1oXURtZhCWTp2APH9RE4_L41NYStog_bVMdXwRO_a6QEg_ex0moqxwtRZKivnIF4DKm6WLj45X0FLj-F7HTlZ-eoc9j3w_dVaVyhhxEKUiTyQSJ_AwVKMTbAUmxvWY3OnoIAmu4WYrbC4T4tA2cWoB9yXKna8Yfbil_vC46tLZweGF7RRZR2MPT16q-iCzKG73JqAMphV4NO7b-bMk6mhvgz8TR0_YUewsPg2CVvgdvEmnV4DE4znhnwiLMniN0kPGzF5pindkKTVNDb8"


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
