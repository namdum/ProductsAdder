package com.example.productsadder.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.productsadder.util.Resource
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel(private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()) : ViewModel() {
    private val _loginResult = MutableLiveData<Resource<String>>()
    val loginResult: LiveData<Resource<String>> = _loginResult

    fun login(email: String, password: String) {
        _loginResult.value = Resource.Loading()
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginResult.value = Resource.Success("Log In Successfully...")
                } else {
                    _loginResult.value = Resource.Error("Log In Failed...")
                }
            }
    }
}