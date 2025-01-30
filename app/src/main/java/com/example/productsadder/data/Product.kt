package com.example.productsadder.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val name: String="",
    val category: String="",
    val price: Float=0f,
    val offerPercentage: Float? = null,
    val description: String? = null,
    val sizes: List<String>? = listOf(),
    val colors: List<Int>? = listOf(),
    val images: List<String> = listOf()
): Parcelable
sealed class ProductState{
    data class EditProductClick(val editProduct: Product) : ProductState()
    data class DeleteProductClick(val deleteProduct: Product) : ProductState()

}