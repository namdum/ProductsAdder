package com.example.productsadder.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.productsadder.util.Resource
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

class RegisterViewModel(private val firebaseAuth: FirebaseAuth) : ViewModel() {
    private val _registerResult = MutableLiveData<Resource<String>>()
    val registerResult: LiveData<Resource<String>> = _registerResult

    val db= FirebaseFirestore.getInstance()
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
                                    getUserToken()
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

//    private fun getUserToken() {
//        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
//                return@OnCompleteListener
//            }
//
//            val token = task.result
//            Log.w("TAG", "Fetching FCM registration token successFull :- $token")
//            val map: MutableMap<String, String> = HashMap()
//            map["fcm_token"] = token
//            firebaseAuth.uid?.let { db.collection("user").document(it).set(map, SetOptions.mergeFields("fcm_token")) }
//        })
//    }
    private fun getUserToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                _registerResult.value = Resource.Error("Error fetching FCM token: ${task.exception?.message}")
                return@OnCompleteListener
            }

            val token = task.result
            val tokenData = mapOf("fcm_token" to token)

            // Use mergeFields to only update the fcm_token without overwriting the other fields
            firebaseAuth.uid?.let { uid ->
                Log.w("TAG", "Fetching FCM registration token successFull :- $uid")
                db.collection("user").document(uid).set(tokenData, SetOptions.mergeFields("fcm_token"))
                    .addOnSuccessListener {
                        _registerResult.value = Resource.Success("FCM token updated successfully")
                    }
                    .addOnFailureListener { exception ->
                        _registerResult.value = Resource.Error("Error updating FCM token: ${exception.message}")
                    }
            }
        })
    }
}