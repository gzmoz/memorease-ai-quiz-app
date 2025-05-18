package com.example.memorease

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.memorease.databinding.FragmentQuestionsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem

class QuestionsFragment : Fragment() {

    private var _binding: FragmentQuestionsBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var currentCorrectAnswer: String
    private lateinit var currentQuestionType: String
    private var exoPlayer: ExoPlayer? = null
    private var correctStreak = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchAndDisplayScore()
        fetchRandomQuestion()

        binding.btnSubmitAnswer.setOnClickListener {
            checkAnswer()
        }

        binding.btnFinishQuiz.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.questionsFragment, HomeFragment())
                .commit()
        }
    }

    private fun fetchAndDisplayScore() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            val score = document.getLong("score") ?: 0
            binding.tvScore.text = score.toString()
        }.addOnFailureListener {
            Log.e("ScoreFetch", "Failed to get score", it)
        }
    }

    private fun fetchRandomQuestion() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("memories")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) return@addOnSuccessListener

                val memoryList = documents.shuffled()
                val randomMemory = memoryList.first()

                val question = randomMemory.getString("question")
                val answer = randomMemory.getString("answer")
                val type = randomMemory.getString("type") ?: "text"
                val url = randomMemory.getString("url") ?: ""

                if (question.isNullOrBlank() || answer.isNullOrBlank()) {
                    fetchRandomQuestion()
                    return@addOnSuccessListener
                }

                currentCorrectAnswer = answer.trim().lowercase()
                currentQuestionType = type

                resetViews()

                binding.tvQuestionText.text = question
                binding.tvQuestionText.visibility = View.VISIBLE

                when (type) {
                    "image" -> {
                        if (url.isNotBlank()) {
                            binding.imageQuestion.visibility = View.VISIBLE
                            Glide.with(requireContext()).load(url).into(binding.imageQuestion)
                        }
                    }
                    "voice" -> {
                        if (url.isNotBlank()) {
                            binding.voicePlayer.visibility = View.VISIBLE
                            playAudio(url)
                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.e("QuizError", "Failed to load question", it)
            }
    }

    private fun resetViews() {
        exoPlayer?.release()
        binding.imageQuestion.setImageDrawable(null)
        binding.imageQuestion.visibility = View.GONE
        binding.voicePlayer.visibility = View.GONE
        binding.tvQuestionText.text = ""
        binding.tvQuestionText.visibility = View.GONE
    }

    private fun playAudio(url: String) {
        exoPlayer?.release()
        exoPlayer = ExoPlayer.Builder(requireContext()).build()
        binding.voicePlayer.player = exoPlayer
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.playWhenReady = true
    }

    private fun checkAnswer() {
        val userAnswer = binding.etAnswer.text.toString().trim().lowercase()
        val isCorrect = userAnswer == currentCorrectAnswer

        binding.tvFeedback.apply {
            text = if (isCorrect) "Correct Answer" else "Wrong Answer"
            setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (isCorrect) android.R.color.holo_green_dark else android.R.color.holo_red_dark
                )
            )
            visibility = View.VISIBLE
        }

        updateQuizStats(isCorrect)

        if (isCorrect) {
            correctStreak++
            updateUserScore()
        } else {
            correctStreak = 0
        }

        Handler(Looper.getMainLooper()).postDelayed({
            binding.etAnswer.text.clear()
            binding.tvFeedback.visibility = View.GONE
            fetchRandomQuestion()
        }, 3500)
    }

    private fun updateUserScore() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)

        var baseScore = when (currentQuestionType) {
            "image" -> 20
            "voice" -> 15
            else -> 10
        }

        if (correctStreak > 0 && correctStreak % 3 == 0) {
            baseScore += 10
        }

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentScore = snapshot.getLong("score") ?: 0
            val newScore = currentScore + baseScore
            transaction.update(userRef, "score", newScore)
            newScore
        }.addOnSuccessListener { newScore ->
            binding.tvScore.text = newScore.toString()
            Log.d("ScoreUpdate", "Score updated successfully")
        }.addOnFailureListener {
            Log.e("ScoreUpdate", "Failed to update score", it)
        }
    }

    /*private fun updateQuizStats(isCorrect: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val statsRef = db.collection("users").document(userId)
            .collection("quizStats").document(currentQuestionType)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(statsRef)
            val total = (snapshot.getLong("total") ?: 0) + 1
            val correct = (snapshot.getLong("correct") ?: 0) + if (isCorrect) 1 else 0

            transaction.set(statsRef, mapOf(
                "total" to total,
                "correct" to correct
            ), SetOptions.merge())
        }.addOnSuccessListener {
            Log.d("QuizStats", "Stats updated successfully")
        }.addOnFailureListener {
            Log.e("QuizStats", "Failed to update stats", it)
        }
    }*/

    private fun updateQuizStats(isCorrect: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)

        // 🔁 Yeni: Haftalık quizHistory koleksiyonuna ekleme
        val now = java.util.Calendar.getInstance()
        val week = now.get(java.util.Calendar.WEEK_OF_YEAR)
        val year = now.get(java.util.Calendar.YEAR)
        val weekId = "${year}-W${week.toString().padStart(2, '0')}"

        val quizEntry = mapOf(
            "timestamp" to com.google.firebase.Timestamp.now(),
            "type" to currentQuestionType,
            "isCorrect" to isCorrect,
            "weekOfYear" to weekId
        )

        userRef.collection("quizHistory")
            .add(quizEntry)
            .addOnSuccessListener {
                Log.d("QuizHistory", "Quiz recorded for $weekId")
            }
            .addOnFailureListener {
                Log.e("QuizHistory", "Failed to record quiz", it)
            }

        // 🔁 Mevcut istatistiklerin güncellenmesi (opsiyonel, istersen kaldırabilirsin)
        val statsRef = userRef.collection("quizStats").document(currentQuestionType)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(statsRef)
            val total = (snapshot.getLong("total") ?: 0) + 1
            val correct = (snapshot.getLong("correct") ?: 0) + if (isCorrect) 1 else 0

            transaction.set(statsRef, mapOf(
                "total" to total,
                "correct" to correct
            ), SetOptions.merge())
        }.addOnSuccessListener {
            Log.d("QuizStats", "Stats updated successfully")
        }.addOnFailureListener {
            Log.e("QuizStats", "Failed to update stats", it)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        exoPlayer?.release()
        exoPlayer = null
    }
}
