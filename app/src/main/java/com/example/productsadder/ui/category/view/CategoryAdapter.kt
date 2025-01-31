package com.example.productsadder.ui.category.view


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.productsadder.R
import com.example.productsadder.model.Category
import com.example.productsadder.model.CategoryState
import io.reactivex.subjects.PublishSubject


class CategoryAdapter(val categories: MutableList<Category>) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private val categoryItemClicksSubject: PublishSubject<CategoryState> = PublishSubject.create()
    val categoryClicks: io.reactivex.Observable<CategoryState> = categoryItemClicksSubject.hide()
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryNameTextView: AppCompatTextView = itemView.findViewById(R.id.categoryAppCompatTextView)
        val categoryAppCompatImageView : AppCompatImageView = itemView.findViewById(R.id.categoryAppCompatImageView)
        val editButton: AppCompatImageView = itemView.findViewById(R.id.editbtn)
        val deleteButton: AppCompatImageView = itemView.findViewById(R.id.deletebtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryNameTextView.text = category.category
        Glide.with(holder.itemView.context)
            .load(category.image)
            .placeholder(R.drawable.chair)
            .into(holder.categoryAppCompatImageView)

        holder.editButton.setOnClickListener {
            categoryItemClicksSubject.onNext(CategoryState.EditCategoryClick(category))

        }

        holder.deleteButton.setOnClickListener {
            categoryItemClicksSubject.onNext(CategoryState.DeleteCategoryClick(category))
        }
    }
    override fun getItemCount(): Int = categories.size
}
