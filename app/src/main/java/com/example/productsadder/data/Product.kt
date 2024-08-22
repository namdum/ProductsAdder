package com.example.productsadder.data

data class Product(
    val name: String,
    val category: String,
    val price: Float,
    val offerPercentage: Float? = null,
    val description: String? = null,
    val sizes: List<String>? = null,
    val colors: List<Int>? = null,
    val images: List<String>)
