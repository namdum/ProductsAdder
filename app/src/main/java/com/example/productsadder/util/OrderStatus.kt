package com.example.productsadder.util

enum class OrderStatus(val status: String) {
    ORDER_CONFIRMED("Order Confirmed"),
    READY_FOR_DISPATCH("Ready for Dispatch"),
    DISPATCHED("Dispatched"),
    IN_TRANSIT("In Transit"),
    DELIVERED("Delivered");
}