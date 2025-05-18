package com.example.memorease.models

data class Memory(
    var id: String = "",
    val type: String = "",
    val url: String? = null,
    val description: String = "",
    val uploadedBy: String = "",
    val timestamp: com.google.firebase.Timestamp? = null
)

