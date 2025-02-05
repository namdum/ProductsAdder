package com.example.productsadder.view

import android.content.Context
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.productsadder.R
import com.example.productsadder.base.view.ConstraintLayoutWithLifecycle
import com.example.productsadder.databinding.ItemCategoryBinding
import com.example.productsadder.model.Category
import com.example.productsadder.model.CategoryState
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.network.extension.throttleClicks
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class CategoryView(context: Context): ConstraintLayoutWithLifecycle(context) {
    val categoryItemClicksSubject: PublishSubject<CategoryState> = PublishSubject.create()
    val categoryItemClicks: Observable<CategoryState> = categoryItemClicksSubject.hide()

    private lateinit var binding: ItemCategoryBinding
    private lateinit var categoryInfo: Category

    init {
        inflateUi()
    }

    private fun inflateUi() {
        val view = View.inflate(context, R.layout.item_category, this)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        binding = ItemCategoryBinding.bind(view)

        binding.apply {
            deletebtn.throttleClicks().subscribeAndObserveOnMainThread {
                categoryItemClicksSubject.onNext(CategoryState.DeleteCategoryClick(categoryInfo))
            }
            editbtn.throttleClicks().subscribeAndObserveOnMainThread {
                categoryItemClicksSubject.onNext(CategoryState.EditCategoryClick(categoryInfo))
            }
        }
    }

    fun bind(category: Category) {
        this.categoryInfo = category
        binding.categoryAppCompatTextView.text = category.category

        Glide.with(context)
            .load(category.image)
            .placeholder(R.drawable.chair)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.categoryAppCompatImageView)

    }
}
