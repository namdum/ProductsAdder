package com.example.productsadder.ui.product.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.productsadder.R
import com.example.productsadder.model.Product
import com.example.productsadder.model.ProductState
import io.reactivex.subjects.PublishSubject

class ProductAdapter(val products: MutableList<Product>) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {
    private val productItemClicksSubject: PublishSubject<ProductState> = PublishSubject.create()
    val productClicks: io.reactivex.Observable<ProductState> = productItemClicksSubject.hide()
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: AppCompatTextView = itemView.findViewById(R.id.productName)
        val categoryNameTextView: AppCompatTextView = itemView.findViewById(R.id.category)
        val priceTextView: AppCompatTextView = itemView.findViewById(R.id.price)
        val sizeTextView: AppCompatTextView = itemView.findViewById(R.id.size)
        val productAppCompatImageView: AppCompatImageView = itemView.findViewById(R.id.images)
        val editButton: AppCompatImageView = itemView.findViewById(R.id.editImageView)
        val deleteButton: AppCompatImageView = itemView.findViewById(R.id.deleteImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.nameTextView.text = product.name
        holder.categoryNameTextView.text = product.category
        holder.priceTextView.text = product.price.toString()
        holder.sizeTextView.text = product.sizes?.joinToString(", ")

        Glide.with(holder.itemView.context)
            .load(product.images.firstOrNull())
            .placeholder(R.drawable.chair)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.productAppCompatImageView)

        holder.editButton.setOnClickListener {
            productItemClicksSubject.onNext(ProductState.EditProductClick(product))
        }

        holder.deleteButton.setOnClickListener {
            productItemClicksSubject.onNext(ProductState.DeleteProductClick(product))

        }
    }
    override fun getItemCount(): Int = products.size
}