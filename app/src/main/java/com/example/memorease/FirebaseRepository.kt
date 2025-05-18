package com.example.memorease

import com.example.memorease.data.Memory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getAllMemories(): List<Memory> {
        val snapshot = db.collectionGroup("memories").get().await()
        return snapshot.documents.mapNotNull { it.toObject(Memory::class.java) }
    }
}
