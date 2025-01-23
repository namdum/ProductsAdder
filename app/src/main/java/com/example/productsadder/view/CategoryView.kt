package com.example.productsadder.view

import android.content.Context
import android.content.Intent
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.productsadder.R
import com.example.productsadder.activity.EditCategoryActivity
import com.example.productsadder.data.Category
import com.example.productsadder.data.CategoryState
import com.example.productsadder.databinding.ItemCategoryBinding
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.network.extension.throttleClicks
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class CategoryView(context: Context): ConstraintLayoutWithLifecycle(context) {
    val categoryItemClicksSubject: PublishSubject<CategoryState> = PublishSubject.create()
    val categoryItemClicks: Observable<CategoryState> = categoryItemClicksSubject.hide()

    private lateinit var binding: ItemCategoryBinding
    private lateinit var mentionUserInfo: Category

    init {
        inflateUi()
    }

    private fun inflateUi() {
        val view = View.inflate(context, R.layout.item_category, this)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        binding = ItemCategoryBinding.bind(view)

        binding.apply {
            deletebtn.throttleClicks().subscribeAndObserveOnMainThread {
                categoryItemClicksSubject.onNext(CategoryState.CategoryDeleteClick(mentionUserInfo))
            }
            editbtn.throttleClicks().subscribeAndObserveOnMainThread {
                categoryItemClicksSubject.onNext(CategoryState.CategoryEditClick(mentionUserInfo))
            }
        }
    }

    fun bind(category: Category) {
        this.mentionUserInfo = category
        binding.categoryAppCompatTextView.text = category.category

        Glide.with(context)
            .load(category.image)
            .placeholder(R.drawable.chair)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.categoryAppCompatImageView)


//        binding.editbtn.setOnClickListener {
//            val intent = Intent(context, EditCategoryActivity::class.java)
//            intent.putExtra("category_name", category.category)
//            intent.putExtra("category_image", category.image)
//            context.startActivity(intent)
//        }
    }
}
