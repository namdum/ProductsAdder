package com.example.productsadder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth

class LoginViewModelFactory(private val firebaseAuth: FirebaseAuth) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(firebaseAuth) as T
    }
}