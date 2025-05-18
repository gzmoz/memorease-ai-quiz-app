package com.example.memorease.network

import QuestionApi
import com.example.memorease.HuggingFaceApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object RetrofitClient {

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()


    private val retrofit = Retrofit.Builder()
       // .baseUrl("https://api-inference.huggingface.co/") // HuggingFace API URL ✅
       // .baseUrl("https://memorease-flan5-memorease-docker.hf.space/")
        .baseUrl("https://memorease-flan-fine-docker.hf.space/") // ✅ yeni model URL
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

    val api: HuggingFaceApi = retrofit.create(HuggingFaceApi::class.java)
}
