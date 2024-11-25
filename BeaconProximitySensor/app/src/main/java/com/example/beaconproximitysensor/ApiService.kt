package com.example.beaconproximitysensor

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("postbeacon")  // Change this to your actual endpoint
    fun sendBeaconData(@Body beaconData: BeaconData): Call<Void>
}
