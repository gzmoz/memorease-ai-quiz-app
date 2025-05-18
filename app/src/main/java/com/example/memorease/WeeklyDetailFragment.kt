package com.example.memorease

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memorease.adapters.InsightAdapter
import com.example.memorease.databinding.FragmentWeeklyDetailBinding
import com.example.memorease.models.QuizResult
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WeeklyDetailFragment : Fragment() {

    private var _binding: FragmentWeeklyDetailBinding? = null
    private val binding get() = _binding!!

    private var weekId: String? = null
    private var results: ArrayList<QuizResult> = arrayListOf()
    private var targetUserId: String? = null

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            weekId = it.getString(ARG_PARAM1)
            results = it.getParcelableArrayList(ARG_PARAM2) ?: arrayListOf()
            targetUserId = it.getString(ARG_PARAM3)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeeklyDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvWeekTitle.text = weekId?.replace("W", " - Week ")
        setupPieChart()
        fetchPreviousWeekDataAndSetupRecyclerView()
    }

    private fun setupPieChart() {
        if (results.isEmpty()) return

        val grouped = results.groupBy { it.type }
        val entries = grouped.map { (type, list) ->
            val incorrect = list.count { !it.isCorrect }
            val total = list.size
            val percentage = (incorrect.toFloat() / total) * 100f
            PieEntry(percentage, type.replaceFirstChar { it.uppercase() })
        }

        val dataSet = PieDataSet(entries, "Error Distribution").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 14f
        }
        val data = PieData(dataSet)

        binding.pieChartWeekly.data = data
        binding.pieChartWeekly.description.text = "Error Breakdown"
        binding.pieChartWeekly.animateY(1000)
        binding.pieChartWeekly.invalidate()
    }

    private fun fetchPreviousWeekDataAndSetupRecyclerView() {
        val userId = targetUserId ?: FirebaseAuth.getInstance().currentUser?.uid ?: return
        val prevWeekId = getPreviousWeekId(weekId ?: return)

        db.collection("users")
            .document(userId)
            .collection("quizHistory")
            .whereEqualTo("weekOfYear", prevWeekId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    setupRecyclerView(emptyList()) // <-- boş olursa da çalışmalı
                } else {
                    val prevResults = snapshot.mapNotNull { doc ->
                        val type = doc.getString("type") ?: return@mapNotNull null
                        val isCorrect = doc.getBoolean("isCorrect") ?: false
                        QuizResult(type, isCorrect)
                    }
                    setupRecyclerView(prevResults)
                }
            }
            .addOnFailureListener {
                setupRecyclerView(emptyList())
            }
    }


    private fun setupRecyclerView(previousWeekResults: List<QuizResult>) {
        binding.recyclerViewInsights.layoutManager = LinearLayoutManager(requireContext())

        val insights = mutableListOf<String>()
        val currentGrouped = results.groupBy { it.type }
        val prevGrouped = previousWeekResults.groupBy { it.type }

        for ((type, list) in currentGrouped) {
            val incorrect = list.count { !it.isCorrect }
            val total = list.size
            val errorRate = (incorrect.toFloat() / total) * 100f

            val prevErrorRate = prevGrouped[type]?.let {
                val prevIncorrect = it.count { result -> !result.isCorrect }
                val prevTotal = it.size
                (prevIncorrect.toFloat() / prevTotal) * 100f
            } ?: -1f

            val alert = if (errorRate > 50f) " This may require further clinical attention." else ""

            val mainComment = when (type.lowercase()) {
                "image" -> "The patient has a ${"%.1f".format(errorRate)}% error rate in image-based questions, suggesting possible difficulties in visual memory processing."
                "voice" -> "The patient has a ${"%.1f".format(errorRate)}% error rate in voice-related questions, potentially indicating impairments in auditory recognition."
                "text" -> "The patient has a ${"%.1f".format(errorRate)}% error rate in text-based questions, which might reflect challenges in language processing areas."
                else -> null
            }

            val trendComment = if (prevErrorRate >= 0f) {
                val delta = errorRate - prevErrorRate
                when {
                    delta > 0 -> "The error rate increased by ${"%.1f".format(delta)}% compared to the previous week."
                    delta < 0 -> "The error rate decreased by ${"%.1f".format(-delta)}% compared to the previous week."
                    else -> "The error rate remained unchanged compared to the previous week."
                }
            } else null

            mainComment?.let {
                insights.add(it + " " + (trendComment ?: "") + alert)
            }
        }

        val adapter = InsightAdapter(insights)
        binding.recyclerViewInsights.adapter = adapter
    }

    private fun getPreviousWeekId(current: String): String {
        val parts = current.split("W")
        if (parts.size != 2) return current
        val prefix = parts[0]
        val weekNum = parts[1].toIntOrNull() ?: return current
        val prevWeekNum = (weekNum - 1).coerceAtLeast(1)
        return "%sW%02d".format(prefix, prevWeekNum)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PARAM1 = "weekId"
        private const val ARG_PARAM2 = "results"
        private const val ARG_PARAM3 = "targetUserId"

        fun newInstance(weekId: String, results: List<QuizResult>, targetUserId: String?) =
            WeeklyDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, weekId)
                    putParcelableArrayList(ARG_PARAM2, ArrayList(results))
                    putString(ARG_PARAM3, targetUserId)
                }
            }
    }
}

