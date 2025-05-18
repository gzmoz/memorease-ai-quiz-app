package com.example.memorease

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.memorease.databinding.FragmentReportBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UID önceliği: targetUserId > currentUser
        userId = arguments?.getString("targetUserId") ?: auth.currentUser?.uid

        if (userId == null) {
            binding.pieChart.setNoDataText("No user ID available.")
            return
        }

        loadOverallStats(userId!!)

        binding.btnSeeWeeklyReports.setOnClickListener {
            val weeklyReport = WeeklyReportFragment().apply {
                arguments = Bundle().apply {
                    putString("targetUserId", userId)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, weeklyReport)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadOverallStats(userId: String) {
        val types = listOf("text", "image", "voice")
        val entries = mutableListOf<PieEntry>()
        var loadedCount = 0

        types.forEach { type ->
            db.collection("users")
                .document(userId)
                .collection("quizStats")
                .document(type)
                .get()
                .addOnSuccessListener { doc ->
                    val correct = (doc.getLong("correct") ?: 0L).toFloat()
                    val total = (doc.getLong("total") ?: 0L).toFloat()
                    if (total > 0f) {
                        val incorrectPercent = ((total - correct) / total) * 100f
                        entries.add(PieEntry(incorrectPercent, type.replaceFirstChar { it.uppercase() }))
                    }
                    loadedCount++
                    if (loadedCount == types.size) displayChart(entries)
                }
                .addOnFailureListener {
                    loadedCount++
                    if (loadedCount == types.size) displayChart(entries)
                }
        }
    }

    private fun displayChart(entries: List<PieEntry>) {
        val pieChart: PieChart = binding.pieChart
        if (entries.isEmpty()) {
            pieChart.clear()
            pieChart.setNoDataText("No chart data available.")
            return
        }

        val dataSet = PieDataSet(entries, "Error Distribution").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 16f
        }
        val data = PieData(dataSet)

        pieChart.data = data
        pieChart.description.text = "General Report"
        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
