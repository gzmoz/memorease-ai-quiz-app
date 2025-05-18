package com.example.memorease

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memorease.adapters.WeeklyReportAdapter
import com.example.memorease.databinding.FragmentWeeklyReportBinding
import com.example.memorease.models.WeeklyReport
import com.example.memorease.models.QuizResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WeeklyReportFragment : Fragment() {

    private var _binding: FragmentWeeklyReportBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: WeeklyReportAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeeklyReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = arguments?.getString("targetUserId") ?: auth.currentUser?.uid
        if (userId == null) return

        setupRecyclerView()
        loadWeeklyReports(userId!!)
    }

    private fun setupRecyclerView() {
        adapter = WeeklyReportAdapter { report ->
            val detailFragment = WeeklyDetailFragment.newInstance(
                weekId = report.weekId,
                results = report.results,
                targetUserId = userId
            )
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.recyclerViewWeeklyReports.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewWeeklyReports.adapter = adapter
    }

    private fun loadWeeklyReports(userId: String) {
        db.collection("users")
            .document(userId)
            .collection("quizHistory")
            .get()
            .addOnSuccessListener { result ->
                val weekMap = mutableMapOf<String, MutableList<QuizResult>>()

                for (document in result) {
                    val week = document.getString("weekOfYear") ?: continue
                    val type = document.getString("type") ?: continue
                    val isCorrect = document.getBoolean("isCorrect") ?: false

                    val quizResult = QuizResult(type, isCorrect)
                    if (!weekMap.containsKey(week)) {
                        weekMap[week] = mutableListOf()
                    }
                    weekMap[week]?.add(quizResult)
                }

                val weekReports = weekMap.entries.map { entry ->
                    WeeklyReport(entry.key, entry.value)
                }.sortedByDescending { it.weekId }

                adapter.submitList(weekReports)
            }
            .addOnFailureListener {
                binding.recyclerViewWeeklyReports.visibility = View.GONE
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
