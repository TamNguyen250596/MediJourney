package com.example.medijourney.common.api

import com.example.medijourney.common.constants.Constants
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NutritionixApi {
    @Headers(
        "Content-Type: application/json",
        "x-app-id: ${Constants.NUTRITIONX_APP_ID}",
        "x-app-key: ${Constants.NUTRITIONX_APP_KEY}"
    )
    @POST("natural/nutrients")
    fun getFoodInfo(@Body request: Map<String, String>): Call<JsonElement>
}