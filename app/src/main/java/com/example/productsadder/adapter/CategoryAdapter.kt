package com.example.productsadder.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.productsadder.data.Category
import com.example.productsadder.data.CategoryState
import com.example.productsadder.view.CategoryView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class CategoryAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val CategoryItemClicksSubject: PublishSubject<CategoryState> = PublishSubject.create()
    val categoryItemClicks: Observable<CategoryState> = CategoryItemClicksSubject.hide()


    private var adapterItems = listOf<AdapterItem>()
//    var categories: MutableList<Category> = mutableListOf()
//        set(value) {
//            field = value
//            updateAdapterItems()
//        }
//
//    private fun updateAdapterItems() {
//        categories.clear()
////        val newAdapterItems = categories.map {
////            AdapterItem.CategoryItemViewItem(it)
////        }
//        val newAdapterItems=categories.forEach {
//           AdapterItem.CategoryItemViewItem(it)
//       }
//        adapterItems = newAdapterItems
//        notifyDataSetChanged()
//    }

    var categories: ArrayList<Category>? = null
        set(listOfHashtagInfo) {
            field = listOfHashtagInfo
            updateAdapterItem()
        }

    private fun updateAdapterItem() {
        val adapterItems = mutableListOf<AdapterItem>()
        categories?.let {
            it.forEach { data ->
                adapterItems.add(AdapterItem.CategoryItemViewItem(data))
            }
        }
        this.adapterItems = adapterItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.CategoryItemType.ordinal -> {
                CategoryAdapterViewHolder(
                    CategoryView(context).apply {
                        categoryItemClicks.subscribe { CategoryItemClicksSubject.onNext(it) }
                    }
                )
            }
            else -> throw IllegalArgumentException("Unsupported ViewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val adapterItem = adapterItems.getOrNull(position) ?: return
        when (adapterItem) {
            is AdapterItem.CategoryItemViewItem -> {
                (holder.itemView as CategoryView).bind(adapterItem.category)
            }
        }
    }

    override fun getItemCount(): Int {
        return adapterItems.size
    }

    override fun getItemViewType(position: Int): Int {
        return adapterItems[position].type
    }

    private class CategoryAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view)

    sealed class AdapterItem(val type: Int) {
        data class CategoryItemViewItem(val category: Category) :
            AdapterItem(ViewType.CategoryItemType.ordinal)
    }

    private enum class ViewType {
        CategoryItemType,
    }
}