package com.example.productsadder.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@kotlinx.parcelize.Parcelize
data class Product(
    val name: String,
    val category: String,
    val price: Float,
    val offerPercentage: Float? = null,
    val description: String? = null,
    val sizes: List<String>? = listOf(),
    val colors: List<Int>? = listOf(),
    val images: List<String> = listOf()
): Parcelable {
    constructor(): this("", "", 0f, 0f, "", listOf(), listOf(), listOf())
}