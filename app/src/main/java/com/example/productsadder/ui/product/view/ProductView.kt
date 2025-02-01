package com.example.productsadder.view


import android.content.Context
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.productsadder.R
import com.example.productsadder.base.view.ConstraintLayoutWithLifecycle
import com.example.productsadder.databinding.ItemProductBinding
import com.example.productsadder.model.Product
import com.example.productsadder.model.ProductState
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.network.extension.throttleClicks
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class ProductView(context: Context): ConstraintLayoutWithLifecycle(context) {
    val productItemClicksSubject: PublishSubject<ProductState> = PublishSubject.create()
    val productItemClicks: Observable<ProductState> = productItemClicksSubject.hide()

    private lateinit var binding: ItemProductBinding
    private lateinit var product: Product

    init {
        inflateUi()
    }

    private fun inflateUi() {
        val view = View.inflate(context, R.layout.item_product, this)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        binding = ItemProductBinding.bind(view)

        binding.apply {
            deleteImageView.throttleClicks().subscribeAndObserveOnMainThread {
                productItemClicksSubject.onNext(ProductState.DeleteProductClick(product))
            }
            editImageView.throttleClicks().subscribeAndObserveOnMainThread {
                productItemClicksSubject.onNext(ProductState.EditProductClick(product))
            }
        }
    }

    fun bind(product: Product) {
        this.product = product
        binding.productName.text = product.name
        binding.category.text = product.category
        binding.price.text = product.price.toString()
        binding.size.text = product.sizes?.joinToString(", ")

        Glide.with(context)
            .load(product.images.firstOrNull())
            .placeholder(R.drawable.chair)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.images)
    }
}
