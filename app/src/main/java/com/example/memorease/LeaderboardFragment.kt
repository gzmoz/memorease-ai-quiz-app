package com.example.memorease

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memorease.adapters.LeaderboardUserAdapter
import com.example.memorease.data.LeaderboardUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LeaderboardFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var topUserImages: List<ImageView>
    private lateinit var topUserNames: List<TextView>
    private lateinit var topUserScores: List<TextView>

    private lateinit var extraScoreText: TextView
    private lateinit var extraRankText: TextView
    private lateinit var extraProfileImage: ImageView

    private lateinit var recyclerLeaderboard: RecyclerView
    private lateinit var adapter: LeaderboardUserAdapter
    private val leaderboardList = mutableListOf<LeaderboardUser>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        topUserImages = listOf(
            view.findViewById(R.id.profile_picture_1),
            view.findViewById(R.id.profile_picture_2),
            view.findViewById(R.id.profile_picture_3)
        )

        topUserNames = listOf(
            view.findViewById(R.id.username_1),
            view.findViewById(R.id.username_2),
            view.findViewById(R.id.username_3)
        )

        topUserScores = listOf(
            view.findViewById(R.id.score_1),
            view.findViewById(R.id.score_2),
            view.findViewById(R.id.score_3)
        )

        extraScoreText = view.findViewById(R.id.extra_score)
        extraRankText = view.findViewById(R.id.extra_rank)
        extraProfileImage = view.findViewById(R.id.extra_profile_picture)

        recyclerLeaderboard = view.findViewById(R.id.recyclerLeaderboard)
        recyclerLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        adapter = LeaderboardUserAdapter(leaderboardList)
        recyclerLeaderboard.adapter = adapter

        fetchLeaderboardData()
    }

    private fun fetchLeaderboardData() {
        db.collection("users")
            .orderBy("score", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val currentUserId = auth.currentUser?.uid
                var currentUserRank = -1
                var currentUserScore = 0L
                leaderboardList.clear()

                Log.d("Firestore", "Toplam kullanıcı: ${documents.size()}")

                for ((index, doc) in documents.withIndex()) {
                    val name = doc.getString("name") ?: ""
                    val surname = doc.getString("surname") ?: ""
                    val fullName = "$name $surname".trim()
                    val score = doc.getLong("score") ?: 0
                    val imageUrl = doc.getString("profileImageUrl") ?: ""

                    leaderboardList.add(
                        LeaderboardUser(
                            name = fullName,
                            score = score,
                            imageUrl = imageUrl,
                            rank = index + 1
                        )
                    )

                    if (index < 3) {
                        topUserNames[index].text = fullName
                        topUserScores[index].text = formatScore(score)
                        Glide.with(this)
                            .load(imageUrl)
                            .circleCrop()
                            .into(topUserImages[index])
                    }

                    if (doc.id == currentUserId) {
                        currentUserRank = index + 1
                        currentUserScore = score
                        extraScoreText.text = formatScore(currentUserScore)
                        extraRankText.text = "$currentUserRank"
                        Glide.with(this)
                            .load(imageUrl)
                            .circleCrop()
                            .into(extraProfileImage)
                    }
                }

                Log.d("Firestore", "Görüntülenecek kullanıcı sayısı: ${leaderboardList.size}")
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.e("Firestore", "Veri alınırken hata: ${it.message}")
            }
    }

    private fun formatScore(score: Long): String {
        return "%,d".format(score)
    }
}
