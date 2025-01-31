package com.example.productsadder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productsadder.data.Order
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.launch

class OrderListViewModel (private val firestore: FirebaseFirestore) : ViewModel() {

    private val orderListStateSubject: PublishSubject<OrderListViewState> = PublishSubject.create()
    val orderListState: Observable<OrderListViewState> = orderListStateSubject.hide()
    init {
        fetchOrders()
    }

    fun fetchOrders() {
        orderListStateSubject.onNext(OrderListViewState.LoadingState(true))
        firestore.collection("orders")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val ordersList = querySnapshot.documents.mapNotNull { document ->
                    val order = document.toObject(Order::class.java)
                    order
                }
                viewModelScope.launch {
                    orderListStateSubject.onNext(OrderListViewState.FetchOrderList(ordersList))
                }
            }
            .addOnFailureListener { exception ->
                viewModelScope.launch {
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