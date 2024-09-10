package com.example.productsadder.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.productsadder.data.Products
import com.example.productsadder.databinding.OrderProductsItemBinding

class OrderDetailAdapter(var products: List<Products>)  : RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder>()
{
    fun updateItems(newItems: List<Products>) {
        products = newItems
        notifyDataSetChanged()
    }

    inner class OrderDetailViewHolder(val binding: OrderProductsItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(billingProduct: Products) {
            binding.apply {
                Glide.with(itemView).load(billingProduct?.product?.images?.get(0)).into(imageCartProduct)
                tvProductCartName.text = billingProduct?.product?.name
                tvBillingProductQuantity.visibility=View.GONE
                val priceAfterPercentage = billingProduct?.product?.price?.let {
                    billingProduct?.product?.offerPercentage.getProductPrice(
                        it
                    )
                }
                tvProductCartPrice.text = "$${String.format("%.2f", priceAfterPercentage)}"
                tvProductCartDescountPrice.paintFlags= Paint.STRIKE_THRU_TEXT_FLAG

            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailViewHolder {
        return OrderDetailViewHolder(
            OrderProductsItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: OrderDetailViewHolder, position: Int) {

        holder.bind(products[position])
    }
    override fun getItemCount(): Int {
        return products.size
    }

    fun Float?.getProductPrice(price: Float): Float{
        //this --> Percentage
        if (this == null)
            return price
        val remainingPricePercentage = 1f - this
        val priceAfterOffer = remainingPricePercentage * price

        return priceAfterOffer
    }
}