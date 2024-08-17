package com.example.productsadder.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.productsadder.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterViewModel(private val firebaseAuth: FirebaseAuth) : ViewModel() {
    private val _registerResult = MutableLiveData<Resource<String>>()
    val registerResult: LiveData<Resource<String>> = _registerResult

    fun registerUser(email: String, password: String, firstName: String, lastName: String) {
        _registerResult.value = Resource.Loading()
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(firstName)
                            .build()

                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    _registerResult.value = Resource.Success("Registration successful")
                                } else {
                                    _registerResult.value = Resource.Error("Error updating profile: ${profileTask.exception?.message}")
                                }
                            }
                    }
                } else {
                    _registerResult.value = Resource.Error("Registration failed: ${task.exception?.message}")
                }
            }
    }
}