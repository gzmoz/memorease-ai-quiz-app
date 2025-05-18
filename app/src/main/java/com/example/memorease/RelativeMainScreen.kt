package com.example.memorease

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.memorease.databinding.ActivityRelativeMainScreenBinding

class RelativeMainScreen : AppCompatActivity() {
    private lateinit var binding: ActivityRelativeMainScreenBinding
    private var targetUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRelativeMainScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Relative girişi sırasında gelen target user id'yi al
        targetUserId = intent.getStringExtra("targetUserId")

        // Başlangıçta memories fragmentı aç
        replaceFragment(RelativeUploadReviewMemoryFragment())

        // BottomNavigation işlemleri
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.memories_icon -> replaceFragment(RelativeUploadReviewMemoryFragment())

                R.id.report_icon -> {
                    val reportFragment = ReportFragment().apply {
                        arguments = Bundle().apply {
                            putString("targetUserId", targetUserId)
                        }
                    }
                    replaceFragment(reportFragment)
                }

                else -> {}
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }
}
