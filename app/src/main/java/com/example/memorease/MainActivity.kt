package com.example.memorease

import QuestionApi
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.memorease.data.MemoryRequest
import com.example.memorease.data.QuestionRequest
import com.example.memorease.data.QuestionResponse
import com.example.memorease.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        replaceFragment(HomeFragment())

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home_icon -> replaceFragment(HomeFragment())
                R.id.leaderboard_icon -> replaceFragment(LeaderboardFragment())
                R.id.quiz_icon -> replaceFragment(QuizFragment())
                R.id.report_icon -> replaceFragment(ReportFragment())
                else ->{

                }
            }
            true
        }
        warmUpModel()



    }


    private fun warmUpModel() {
        val warmupRequest = QuestionRequest("This is a warm-up request.")
        RetrofitClient.api.generateQuestion(warmupRequest).enqueue(object : Callback<QuestionResponse> {
            override fun onResponse(call: Call<QuestionResponse>, response: Response<QuestionResponse>) {
                // Başarılıysa logla ama kullanıcıya gösterme
                if (response.isSuccessful) {
                    println("Model warmed up successfully.")
                }
            }

            override fun onFailure(call: Call<QuestionResponse>, t: Throwable) {
                println("Warm-up failed: ${t.message}")
            }
        })
    }


    //method that will replace the fragments
    private fun replaceFragment(fragment: Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()

    }
}