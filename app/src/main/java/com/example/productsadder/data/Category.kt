package com.example.productsadder.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    var image : String,
    var category : String
): Parcelable
