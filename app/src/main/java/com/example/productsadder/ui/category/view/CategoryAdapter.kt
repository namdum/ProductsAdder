package com.example.productsadder.ui.category.view


import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.productsadder.model.Category
import com.example.productsadder.model.CategoryState
import com.example.productsadder.view.CategoryView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class CategoryAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val categoryItemClicksSubject: PublishSubject<CategoryState> = PublishSubject.create()
    val categoryItemClicks: Observable<CategoryState> = categoryItemClicksSubject.hide()


    private var adapterItems = listOf<AdapterItem>()
    var categories: List<Category>? = null
        set(value) {
            field = value
            updateAdapterItems()
        }


    fun updateAdapterItems() {
        val updatedItems = mutableListOf<AdapterItem>()

        categories?.forEach {
            updatedItems.add(AdapterItem.CategoryItemViewItem(it))
        }
        this.adapterItems = updatedItems
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.CategoryItemType.ordinal -> {
                CategoryAdapterViewHolder(
                    CategoryView(context).apply {
                        categoryItemClicks.subscribe {categoryItemClicksSubject.onNext(it) }
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
