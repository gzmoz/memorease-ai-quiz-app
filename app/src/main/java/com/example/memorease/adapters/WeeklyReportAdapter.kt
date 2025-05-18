package com.example.memorease.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.memorease.databinding.ItemWeeklyReportBinding
import com.example.memorease.models.WeeklyReport

class WeeklyReportAdapter(
    private val onItemClick: (WeeklyReport) -> Unit
) : ListAdapter<WeeklyReport, WeeklyReportAdapter.WeeklyReportViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeeklyReportViewHolder {
        val binding = ItemWeeklyReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WeeklyReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeeklyReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WeeklyReportViewHolder(private val binding: ItemWeeklyReportBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WeeklyReport) {
            binding.tvWeekRange.text = item.weekId
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<WeeklyReport>() {
        override fun areItemsTheSame(oldItem: WeeklyReport, newItem: WeeklyReport): Boolean {
            return oldItem.weekId == newItem.weekId
        }

        override fun areContentsTheSame(oldItem: WeeklyReport, newItem: WeeklyReport): Boolean {
            return oldItem == newItem
        }
    }
}
