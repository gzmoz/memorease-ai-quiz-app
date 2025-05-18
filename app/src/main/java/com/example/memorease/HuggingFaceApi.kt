package com.example.memorease

import com.example.memorease.data.QuestionRequest
import com.example.memorease.data.QuestionResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.Call


interface HuggingFaceApi {

    @Headers("Content-Type: application/json")
    @POST("/ask")  // çünkü /ask endpoint'ini yazdık
    fun generateQuestion(@Body request: QuestionRequest): Call<QuestionResponse>
}

