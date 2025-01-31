package com.example.productsadder.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productsadder.util.OrderStatus
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrderViewModel: ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val orderStateSubject: PublishSubject<OrderViewState> = PublishSubject.create()
    val orderState: Observable<OrderViewState> = orderStateSubject.hide()

    fun updateUser(orderId: Long, selectedOrderStatus: String, userId: String) {
        orderStateSubject.onNext(OrderViewState.LoadingState(true))
        viewModelScope.launch {
            try {
                // Query for the specific user's orders
                val documents = firestore.collection("user")
                    .document(userId)
                    .collection("orders")
                    .whereEqualTo("orderId", orderId)
                    .get()
                    .await()

                if (documents.isEmpty) {
                    orderStateSubject.onNext(OrderViewState.ErrorMessage("No orders found for the given orderId"))
                    return@launch
                }

                val updates = documents.documents.map { doc ->
                    firestore.collection("user")
                        .document(userId)
                        .collection("orders")
                        .document(doc.id)
                        .update("orderStatus", selectedOrderStatus)
                        .addOnSuccessListener {
                            orderStateSubject.onNext(OrderViewState.SuccessMessage("Order status updated successfully."))
                        }
                        .addOnFailureListener { exception ->
                            orderStateSubject.onNext(OrderViewState.ErrorMessage(exception.message.toString()))
                        }
                }

                // Wait for all updates to complete
                Tasks.whenAllComplete(updates).await()

            } catch (e: Exception) {
                orderStateSubject.onNext(OrderViewState.ErrorMessage(e.message.toString()))
            }
        }
    }



    fun setupStatusSpinner(orderId: Long, selectedOrderStatus: String) {
        orderStateSubject.onNext(OrderViewState.LoadingState(true))
        viewModelScope.launch {
            try {
                val currentStatusString = selectedOrderStatus
                val currentStatus = OrderStatus.values().find { it.status == currentStatusString }
                    ?: OrderStatus.ORDER_CONFIRMED

                val statuses = OrderStatus.values().toList()
                orderStateSubject.onNext(
                    OrderViewState.FetchStatusSpinnerSetup(
                        StatusSpinnerSetup(
                            statuses,
                            currentStatus
                        )
                    )
                )

            } catch (e: Exception) {
                orderStateSubject.onNext(OrderViewState.ErrorMessage(e.message.toString()))
            }
        }
    }



    fun updateOrderStatus(orderId: Long, selectedStatus: String) {
        orderStateSubject.onNext(OrderViewState.LoadingState(true))
        viewModelScope.launch {
            try {
                val ordersCollection = firestore.collection("orders")

                val documents = ordersCollection.whereEqualTo("orderId", orderId).get().await()

                if (documents.isEmpty) {
                    orderStateSubject.onNext(OrderViewState.ErrorMessage("No documents found for orderId: $orderId"))
                    return@launch
                }

                for (document in documents.documents) {
                    ordersCollection.document(document.id)
                        .update("orderStatus", selectedStatus)
                        .await() // Wait for the update to complete
                }

                orderStateSubject.onNext(OrderViewState.SuccessMessage("Order status updated successfully."))

            } catch (e: Exception) {
                orderStateSubject.onNext(OrderViewState.ErrorMessage(e.message.toString()))
            }
        }
    }

}

data class StatusSpinnerSetup(
    val statuses: List<OrderStatus>,
    val currentStatus: OrderStatus
)

sealed class OrderViewState {
    data class ErrorMessage(val errorMessage: String) : OrderViewState()
    data class SuccessMessage(val successMessage: String) : OrderViewState()
    data class LoadingState(val isLoading: Boolean) : OrderViewState()
    data class FetchStatusSpinnerSetup(val fetchStatusSpinnerSetup: StatusSpinnerSetup) : OrderViewState()
}