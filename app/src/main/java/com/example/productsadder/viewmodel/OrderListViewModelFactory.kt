package com.example.productsadder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OrderListViewModelFactory (private val firestore: FirebaseFirestore, private val auth: FirebaseAuth) : ViewModelProvider.Factory  {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return OrderListViewModel(firestore,auth) as T
    }
}