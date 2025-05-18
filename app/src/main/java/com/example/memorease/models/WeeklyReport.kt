package com.example.memorease.models

data class WeeklyReport(
    val weekId: String = "",
    val results: List<QuizResult> = emptyList()
)