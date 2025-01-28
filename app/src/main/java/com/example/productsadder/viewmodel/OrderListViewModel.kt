package com.example.productsadder.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productsadder.data.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class OrderListViewModel (private val firestore: FirebaseFirestore) : ViewModel() {

//    private val _orders = MutableStateFlow<List<Order>>(emptyList())
//    val orders: StateFlow<List<Order>> = _orders
    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    private val orderListStateSubject: PublishSubject<OrderListViewState> = PublishSubject.create()
    val orderListState: Observable<OrderListViewState> = orderListStateSubject.hide()
    init {
        fetchOrders()
    }

    fun fetchOrders() {
        orderListStateSubject.onNext(OrderListViewState.LoadingState(true))

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
//                    _orders.emit(ordersList)
                    orderListStateSubject.onNext(OrderListViewState.FetchOrderList(ordersList))
                }
            }
            .addOnFailureListener { exception ->
                Log.e("OrderListViewModel", "Error fetching orders: ${exception.message}")
                viewModelScope.launch {
//                    _orders.emit(emptyList())
                    orderListStateSubject.onNext(OrderListViewState.ErrorMessage(exception.message.toString()))
                }
            }
    }
}

sealed class OrderListViewState {
    data class ErrorMessage(val errorMessage: String) : OrderListViewState()
    data class LoadingState(val isLoading: Boolean) : OrderListViewState()
    data class FetchOrderList(val fetchOrderList: List<Order>) : OrderListViewState()
}