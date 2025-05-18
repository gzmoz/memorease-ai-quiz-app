package com.example.memorease.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.memorease.databinding.ItemInsightBinding


class InsightAdapter(private val items: List<String>) : RecyclerView.Adapter<InsightAdapter.InsightViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsightViewHolder {
        val binding = ItemInsightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InsightViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InsightViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class InsightViewHolder(private val binding: ItemInsightBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String) {
            binding.tvInsightText.text = text
        }
    }
}
