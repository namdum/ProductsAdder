package com.example.productsadder.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productsadder.util.OrderStatus
import com.example.productsadder.util.Resource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrderViewModel: ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val _updateUserStatus = MutableLiveData<Resource<String>>()
    val updateUserStatus: LiveData<Resource<String>> = _updateUserStatus
    private val _spinnerSetupStatus = MutableLiveData<Resource<StatusSpinnerSetup>>()
    val spinnerSetupStatus: LiveData<Resource<StatusSpinnerSetup>> = _spinnerSetupStatus
    private val _updateOrderStatus = MutableLiveData<Resource<String>>()
    val updateOrderStatus: LiveData<Resource<String>> = _updateOrderStatus

    fun updateUser(orderId: Long, selectedOrderStatus: String, userId: String) {
        _updateUserStatus.value = Resource.Loading() // Set loading state
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
                    _updateUserStatus.value = Resource.Error("No orders found for the given orderId")
                    return@launch
                }

                // Update order status for each document
                val updates = documents.documents.map { doc ->
                    firestore.collection("user")
                        .document(userId)
                        .collection("orders")
                        .document(doc.id)
                        .update("orderStatus", selectedOrderStatus)
                        .addOnSuccessListener {
                            _updateUserStatus.postValue(Resource.Success("Order status updated successfully."))
                        }
                        .addOnFailureListener { exception ->
                            _updateUserStatus.postValue(Resource.Error("Error updating order status: ${exception.message}"))
                        }
                }

                // Wait for all updates to complete
                Tasks.whenAllComplete(updates).await()

            } catch (e: Exception) {
                _updateUserStatus.value = Resource.Error("Error fetching or updating order status: ${e.message}")
            }
        }
    }



    fun setupStatusSpinner(orderId: Long, selectedOrderStatus: String) {
        _spinnerSetupStatus.value = Resource.Loading() // Set loading state

        viewModelScope.launch {
            try {
                // Fetch the order document
                val document = firestore.collection("orders").document(orderId.toString()).get().await()

                val currentStatusString = selectedOrderStatus
                val currentStatus = OrderStatus.values().find { it.status == currentStatusString }
                    ?: OrderStatus.ORDER_CONFIRMED

                val statuses = OrderStatus.values().toList()

                // Pass the data for spinner setup
                _spinnerSetupStatus.value = Resource.Success(StatusSpinnerSetup(statuses, currentStatus))

            } catch (e: Exception) {
                _spinnerSetupStatus.value = Resource.Error("Error fetching order status: ${e.message}")
            }
        }
    }



    fun updateOrderStatus(orderId: Long, selectedStatus: String) {
        _updateOrderStatus.value = Resource.Loading() // Set loading state
        viewModelScope.launch {
            try {
                val ordersCollection = firestore.collection("orders")

                // Query for documents with the specific orderId
                val documents = ordersCollection.whereEqualTo("orderId", orderId).get().await()

                if (documents.isEmpty) {
                    _updateOrderStatus.value = Resource.Error("No documents found for orderId: $orderId")
                    return@launch
                }

                // Update the order status in all documents
                for (document in documents.documents) {
                    ordersCollection.document(document.id)
                        .update("orderStatus", selectedStatus)
                        .await() // Wait for the update to complete
                }

                _updateOrderStatus.value = Resource.Success("Order status updated successfully.")

            } catch (e: Exception) {
                _updateOrderStatus.value = Resource.Error("Error updating order status: ${e.message}")
            }
        }
    }

}

data class StatusSpinnerSetup(
    val statuses: List<OrderStatus>,
    val currentStatus: OrderStatus
)