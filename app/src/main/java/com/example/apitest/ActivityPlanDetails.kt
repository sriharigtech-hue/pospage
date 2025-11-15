package com.example.apitest

import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apitest.adapter.MainFeaturesAdapter
import com.example.apitest.adapter.PlanAdapter
import com.example.apitest.dataModel.Input
import com.example.apitest.dataModel.POSFeatures
import com.example.apitest.dataModel.PlanDetails
import com.example.apitest.dataModel.PlanDetailsOutput
import com.example.apitest.dataModel.PosFeaturesDetailsOutput
import com.example.apitest.dataModel.ShopPlanDetailsOutput
import com.example.apitest.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActivityPlanDetails : AppCompatActivity() {
    private val jwtToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI1IiwianRpIjoiZTRjY2JlNDBmYWFiZmE5YTg1YmVkMDdjOTcyMzAzNWY3MTljNzQ4M2Q3YzIzMjg3M2E5ZjZmN2QyMjQ1Y2E3MjVhNjVlMTQ5ZGRiZjBkYjgiLCJpYXQiOjE3NjE2MzM3OTIuNDI5OTk5LCJuYmYiOjE3NjE2MzM3OTIuNDMwMDAzLCJleHAiOjE3OTMxNjk3OTIuNDIyNjY5LCJzdWIiOiI2Iiwic2NvcGVzIjpbXX0.SutcB4SMDdEzgfDZ1lnzGDjY4gf1aSwNJMGFnKF3RXWGbB4MhP6R8wRLCa2wtCMZ1egUQc1LxPMq2S9P-hfCeh4stj4W-zlWf0xOfvswvWVos14aP5RBTjbZ8Rc3PrmAwiFvnzu7PpRtqy8hGF_h1nt2dJrZpT4k0CIuvpUK8afLBKYfpr0i1NOx1awb2Z6Uo5YfJRQrB7y4wrs8TEsnbuAIdC4JG6-9Oi_TPwZ42SGatw4UEUvm09I3SjKGjZRDCOMJZLbSc1O4C6B53aK9hNQKCjnwsSXWc37h-cA6lKg-DSfm6K1usg0yHeAsE-2uya2_b8_TNq3LN4Mb04S820FmpnP0RqtDoPeoqBUSTacd0bSINimYpNv8NyiaO0D6k0J3HzaNd5MmwORyHpTNnVEaM8l0O5iyI-UIa3bPaOBnltamiAydE2EmUA9pfRQy3HWd6yZnxfuITM0THdj73ju1Qh3D0WVP7aUFR-3XUbp5qVflBZkiBe0klClG94ubWNFMX6vebixLQ21KsDvEDLj2Xy0hoLY-g6sm33l14NwbSiUgZ0VQI_3WbOzSpUvU0sprU7ozX7y7-nwjIXjshOQ5ymktZUMCN7LyCbvgX-qcGyxbS8C9JN9BhmvhagIBatDpPZw75arXlksHzxKnytbLG3BVxFHXxkp9jSBqW2s"
    private var activePlanId: Int? = null
    private var plansLoaded = false
    private var activePlanLoaded = false

    private lateinit var recyclerView: RecyclerView
    private lateinit var posFeatureRecycler: RecyclerView

    private lateinit var oneYearButton: RelativeLayout
    private lateinit var sixMonthsButton: RelativeLayout

    private var allPlans = ArrayList<PlanDetails>()
    private var posFeatureList = ArrayList<POSFeatures>()

    private lateinit var planAdapter: PlanAdapter

    // 1 = 1 Year selected, 6 = 6 Months selected
    private var selectedButton = 1

    private var activePlanPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plan_details)

        recyclerView = findViewById(R.id.list_radio)
        recyclerView.layoutManager = LinearLayoutManager(this)
        posFeatureRecycler = findViewById(R.id.posfeaturelist)
        posFeatureRecycler.layoutManager = LinearLayoutManager(this)

        oneYearButton = findViewById(R.id.english) // 1 Year
        sixMonthsButton = findViewById(R.id.others) // 6 Months

        // prepare adapter with empty list first
        planAdapter = PlanAdapter(ArrayList()) { plan ->
            Toast.makeText(this@ActivityPlanDetails, plan.name ?: "-", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = planAdapter

        // initial UI (1 Year selected by default)
        updateButtonUI()

        oneYearButton.setOnClickListener {
            if (selectedButton != 1) {
                selectedButton = 1
                updateButtonUI()

            }
        }

        sixMonthsButton.setOnClickListener {
            if (selectedButton != 6) {
                selectedButton = 6
                updateButtonUI()

            }
        }

        loadPlans()

        loadPOSFeatures()
    }

    private fun updateButtonUI() {
        val oneYearText = oneYearButton.findViewById<AppCompatTextView>(R.id.english1)
        val sixMonthsText = sixMonthsButton.findViewById<AppCompatTextView>(R.id.others1)

        if (selectedButton == 1) {
            oneYearButton.setBackgroundResource(R.drawable.gradient_btn)
            oneYearText.setTextColor(ContextCompat.getColor(this, R.color.white))

            sixMonthsButton.setBackgroundResource(R.color.white)
            sixMonthsText.setTextColor(ContextCompat.getColor(this, R.color.textBlack))
        } else {
            sixMonthsButton.setBackgroundResource(R.drawable.gradient_btn)
            sixMonthsText.setTextColor(ContextCompat.getColor(this, R.color.white))

            oneYearButton.setBackgroundResource(R.color.white)
            oneYearText.setTextColor(ContextCompat.getColor(this, R.color.textBlack))
        }
    }


//
//    private fun showAppropriateContent() {
//
//        // ALWAYS show full data for both 1 Year and 6 Months
//
//        posFeatureRecycler.adapter = MainFeaturesAdapter(posFeatureList)
//    }
    private fun tryReorderPlans() {
        if (!plansLoaded || !activePlanLoaded) return

        val active = allPlans.find { it.id == activePlanId }

        if (active != null) {
            allPlans.remove(active)
            allPlans.add(0, active)
            activePlanPosition = 0
        }

        planAdapter.updateList(allPlans)
        planAdapter.activePlanPosition = activePlanPosition
        planAdapter.notifyDataSetChanged()

    }





    private fun loadPlans() {
        val input = Input(status = "1")

        ApiClient.instance.getPlansAPI(jwtToken, input)
            ?.enqueue(object : Callback<PlanDetailsOutput?> {
                override fun onResponse(
                    call: Call<PlanDetailsOutput?>,
                    response: Response<PlanDetailsOutput?>
                ) {
                    if (response.isSuccessful && response.body()?.status == true) {
                        allPlans = ArrayList(response.body()?.loginDetails ?: emptyList())
                        plansLoaded = true
                        planAdapter.updateList(allPlans)
                        loadActivePlan()
                    }
                }

                override fun onFailure(call: Call<PlanDetailsOutput?>, t: Throwable) {}
            })
    }




    private fun loadPOSFeatures() {
        ApiClient.instance.posFeaturesAPI()
            ?.enqueue(object : Callback<PosFeaturesDetailsOutput?> {
                override fun onResponse(
                    call: Call<PosFeaturesDetailsOutput?>,
                    response: Response<PosFeaturesDetailsOutput?>
                ) {
                    if (response.isSuccessful) {
                        posFeatureList = ArrayList(response.body()?.loginDetails ?: emptyList())

                        posFeatureRecycler.adapter = MainFeaturesAdapter(posFeatureList)


                    }
                }

                override fun onFailure(call: Call<PosFeaturesDetailsOutput?>, t: Throwable) {
                    Toast.makeText(this@ActivityPlanDetails, "POS Feature Load Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadActivePlan() {

        val input = Input(status = "1")   // REQUIRED

        ApiClient.instance.getShopSubscriptionDetailsAPI(jwtToken, input)
            ?.enqueue(object : Callback<ShopPlanDetailsOutput?> {
                override fun onResponse(
                    call: Call<ShopPlanDetailsOutput?>,
                    response: Response<ShopPlanDetailsOutput?>
                ) {
                    if (response.isSuccessful && response.body()?.status == true) {


                        activePlanId = response.body()?.loginDetails?.id
                        activePlanLoaded = true

                        reorderActivePlanOnTop()
                    }
                }

                override fun onFailure(call: Call<ShopPlanDetailsOutput?>, t: Throwable) {}
            })
    }



    private fun reorderActivePlanOnTop() {
        if (!plansLoaded || !activePlanLoaded) return
        val active = allPlans.find { it.id == activePlanId }
        if (active != null) {
            allPlans.remove(active)
            allPlans.add(0, active)
            activePlanPosition = 0
        }
        planAdapter.activePlanPosition = activePlanPosition
        planAdapter.updateList(allPlans)
    }


}
