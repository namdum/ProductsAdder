package com.example.productsadder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
class RegisterViewModelFactory(private val firebaseAuth: FirebaseAuth) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegisterViewModel(firebaseAuth) as T
    }
}