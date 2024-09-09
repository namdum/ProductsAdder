package com.example.productsadder.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productsadder.data.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class OrderListViewModel (private val firestore: FirebaseFirestore, private val auth: FirebaseAuth) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders
    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()
    init {
        fetchOrders()
    }

    fun fetchOrders() {
        Log.d("OrderListViewModel", "Fetching orders...")
        firestore.collection("orders")
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("OrderListViewModel", "Orders fetched successfully")
                val ordersList = querySnapshot.documents.mapNotNull { document ->
                    val order = document.toObject(Order::class.java)
                    Log.d("OrderListViewModel", "Document data: $order")
                    order
                }
                viewModelScope.launch {
                    _orders.emit(ordersList)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("OrderListViewModel", "Error fetching orders: ${exception.message}")
                viewModelScope.launch {
                    _orders.emit(emptyList())
                }
            }
    }
}