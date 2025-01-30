package com.example.productsadder.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Address(
    val addressTitle: String="",
    val fullName: String="",
    val street: String="",
    val phone: String="",
    val city: String="",
    val state: String="",
    val userId: String=""
): Parcelable

@Parcelize
data class Order(
    val orderStatus: String? = null,
    val totalPrice: Double? = null,
    val products: List<Products>? = listOf(Products()),
    val address: Address? = Address(),
    val date: String? = null,
    val orderId: Long? = null
) : Parcelable

@Parcelize
data class Products(
    val product: Product? = null,
): Parcelable

sealed class OrderState{
    data class OrderClick(val order: Order) : OrderState()

}