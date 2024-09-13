package com.example.productsadder.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.productsadder.util.Resource
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

class LoginViewModel(private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),private val firestore: FirebaseFirestore= FirebaseFirestore.getInstance()) : ViewModel() {
    private val _loginResult = MutableLiveData<Resource<String>>()
    val loginResult: LiveData<Resource<String>> = _loginResult

    fun login(email: String, password: String) {
        _loginResult.value = Resource.Loading()
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginResult.value = Resource.Success("Log In Successfully...")
                    getUserToken()
                } else {
                    _loginResult.value = Resource.Error("Log In Failed...")
                }
            }
    }

    private fun getUserToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            val token = task.result
            val map: MutableMap<String, String> = HashMap()
            map["fcm_token"] = token
//            firebaseAuth.uid?.let { firestore.collection("user").document(it).set(map, SetOptions.mergeFields("fcm_token")) }
            firebaseAuth.uid?.let { uid ->
                Log.w("MyTesting", "Fetching FCM registration token successFull :- $uid")
                firestore.collection("user").document(uid).set(map, SetOptions.mergeFields("fcm_token"))
                    .addOnSuccessListener {
                        Log.d("MyTesting","FCM token updated successfully")
                    }
                    .addOnFailureListener { exception ->
                        Log.d("MyTesting","Error updating FCM token: ${exception.message}")
                    }
            }

        })
    }
    private val _userStatus = MutableLiveData<Resource<Boolean>>()
    val userStatus: LiveData<Resource<Boolean>> = _userStatus

    fun checkUserStatus() {
        _userStatus.value = Resource.Loading()

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            firestore.collection("users")
                .whereEqualTo("email", currentUser.email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    firestore.collection("user").whereEqualTo("email", currentUser.email)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            val isAdmin = querySnapshot.documents.any { document ->
                                document.getString("user_type") == "admin"
                            }

                            if (isAdmin) {
                                _userStatus.value = Resource.Success(true) // Admin user
                            } else {
                                _userStatus.value = Resource.Success(false) // Invalid user
                            }
                        }
                        .addOnFailureListener {
                            _userStatus.value = Resource.Error("Failed to fetch user data.")
                        }
                }
                .addOnFailureListener {
                    _userStatus.value = Resource.Error("Failed to fetch user data.")
                }
        } else {
            _userStatus.value = Resource.Error("User not logged in.")
        }
    }

}