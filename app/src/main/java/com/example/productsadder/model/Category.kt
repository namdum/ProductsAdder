package com.example.productsadder.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    var image : String="",
    var category : String=""
): Parcelable

sealed class CategoryState{
    data class EditCategoryClick(val editCategory: Category) : CategoryState()
    data class DeleteCategoryClick(val deleteCategory: Category) : CategoryState()

}
