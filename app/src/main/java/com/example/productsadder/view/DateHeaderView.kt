package com.example.productsadder.view

import androidx.recyclerview.widget.RecyclerView
import com.example.productsadder.databinding.ItemDateHeaderBinding

class DateHeaderView(private val binding: ItemDateHeaderBinding) : RecyclerView.ViewHolder(binding.root)  {
    private lateinit var dateLabel: String

    fun bind(dateLabel: String) {
        this.dateLabel = dateLabel
        binding.headerTextView.text = dateLabel

    }
}