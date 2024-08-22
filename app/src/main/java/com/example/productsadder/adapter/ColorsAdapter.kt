package com.example.productsadder.adapter

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.productsadder.databinding.ColorRvItemBinding

class ColorsAdapter : RecyclerView.Adapter<ColorsAdapter.ColorsViewHolder>() {

    private var colors: MutableList<Int> = mutableListOf()
    private var selectedColors: MutableList<Boolean> = mutableListOf()

    inner class ColorsViewHolder(private val binding: ColorRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(color: Int, position: Int) {
            val imageDrawable = ColorDrawable(color)
            binding.imageColor.setImageDrawable(imageDrawable)
            binding.imagePicked.setOnClickListener {
                colors.removeAt(position)
                selectedColors.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorsViewHolder {
        return ColorsViewHolder(ColorRvItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ColorsViewHolder, position: Int) {
        val color = colors[position]
        holder.bind(color, position)

        holder.itemView.setOnClickListener {
            selectedColors[position] = !selectedColors[position]
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return colors.size
    }

    fun updateColors(newColors: List<Int>) {
        colors.clear()
        colors.addAll(newColors)
        selectedColors.clear()
        selectedColors.addAll(newColors.map { false })
        notifyDataSetChanged()
    }
}