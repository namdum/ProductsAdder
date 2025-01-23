package com.example.productsadder.data

data class Category(var image : String, var category : String)
sealed class CategoryState {
    data class CategoryDeleteClick(val category: Category) : CategoryState()
    data class CategoryEditClick(val category: Category) : CategoryState()
}