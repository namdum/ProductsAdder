package com.example.productsadder.ui.product.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.productsadder.model.Product
import com.example.productsadder.model.ProductState
import com.example.productsadder.view.ProductView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ProductAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val productViewClicksSubject: PublishSubject<ProductState> =   PublishSubject.create()
          val productItemClicks: Observable<ProductState> = productViewClicksSubject.hide()
    private var adapterItems = listOf<AdapterItem>()
    var products: List<Product>? = null
        set(value) {
            field = value
            updateAdapterItems()
        }


    fun updateAdapterItems() {
        val updatedItems = mutableListOf<AdapterItem>()

        products?.forEach {
            updatedItems.add(AdapterItem.ProductItemViewItem(it))
        }
        this.adapterItems = updatedItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
           ViewType.ProductItemType.ordinal -> {
                ProductAdapterViewHolder(
                    ProductView(context).apply {
                        productItemClicks.subscribe { productViewClicksSubject.onNext(it)}
                    }
                )
            }

            else -> throw IllegalArgumentException("Unsupported ViewType")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val adapterItem = adapterItems.getOrNull(position) ?: return
        when (adapterItem) {
            is AdapterItem.ProductItemViewItem -> {
                (holder.itemView as ProductView).bind(adapterItem.product)
            }
        }
    }
    override fun getItemCount(): Int {
        return adapterItems.size
    }

    override fun getItemViewType(position: Int): Int {
        return adapterItems[position].type
    }

    private class ProductAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view)

    sealed class AdapterItem(val type: Int) {
        data class ProductItemViewItem(val product: Product) :
            AdapterItem(ViewType.ProductItemType.ordinal)
    }

    private enum class ViewType {
        ProductItemType,
    }
}