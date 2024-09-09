package com.example.productsadder.viewmodel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@kotlinx.parcelize.Parcelize
data class Address(
    val addressTitle: String,
    val fullName: String,
    val street: String,
    val phone: String,
    val city: String,
    val state: String
): Parcelable {

    constructor(): this("","","","","","")
}

@Parcelize
data class Order(
    val orderStatus: String? = null,
    val totalPrice: Double? = null,
//    val products: List<Product>? = null,
    val address: Address? = Address(),
    val date: String? = null,
    val orderId: Long? = null
) : Parcelable

data class OrderGroup(
    val header: String,
    val orders: List<Order>
)