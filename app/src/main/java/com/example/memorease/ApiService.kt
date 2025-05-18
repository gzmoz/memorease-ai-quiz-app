package com.example.memorease

import com.example.memorease.data.MemoryRequest
import com.example.memorease.data.QuestionRequest
import com.example.memorease.data.QuestionResponse
import retrofit2.http.Body
import retrofit2.Call
import retrofit2.http.Headers

import retrofit2.http.POST


interface ApiService {
    @POST("run/predict")
    fun generateQuestion(
        @Body request: QuestionRequest
    ): Call<QuestionResponse>
}
