package com.example.memorease.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize // ✅ doğru olan bu


@Parcelize
data class QuizResult(
    val type: String = "",
    val isCorrect: Boolean = false
) : Parcelable


