package com.zitsav.memoir.network

import com.zitsav.memoir.network.request.GeminiRequest
import com.zitsav.memoir.network.response.GeminiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("gemini/query")
    suspend fun generateContent(@Body request: GeminiRequest): Response<GeminiResponse>
}