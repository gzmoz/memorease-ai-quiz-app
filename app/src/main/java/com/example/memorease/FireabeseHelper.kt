package com.example.memorease

import com.google.firebase.firestore.FirebaseFirestore

object FirebaseHelper {
    private val db = FirebaseFirestore.getInstance()

    fun getDescriptions(onResult: (List<String>) -> Unit) {
        db.collectionGroup("memories").get()
            .addOnSuccessListener { docs ->
                val list = docs.mapNotNull { it.getString("description") }
                onResult(list)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}
