package com.example.productsadder.ui.viewmodel


import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class LoginViewModel(private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),private val firestore: FirebaseFirestore= FirebaseFirestore.getInstance()) : ViewModel() {
    private val loginStateSubject: PublishSubject<LoginViewState> = PublishSubject.create()
    val loginState: Observable<LoginViewState> = loginStateSubject.hide()
    val db = FirebaseFirestore.getInstance()
    fun login(email: String, password: String) {

        loginStateSubject.onNext(LoginViewState.LoadingState(true))
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        val userId = user.uid
                        db.collection("user")
                            .document(userId)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val userType = document.getString("user_type")
                                    if (userType == "admin") {
                                        getUserToken()
                                        loginStateSubject.onNext(LoginViewState.SuccessMessage("Log In Successfully..."))
                                    } else {
                                        FirebaseAuth.getInstance().signOut()
                                        loginStateSubject.onNext(LoginViewState.ErrorMessage("Please check email or password is incorrect"))
                                    }
                                } else {
                                    loginStateSubject.onNext(LoginViewState.ErrorMessage("User data not found."))
                                }
                            }
                            .addOnFailureListener { exception ->
                                loginStateSubject.onNext(LoginViewState.ErrorMessage(exception.message.toString()))
                            }
                    }
                } else {
                    loginStateSubject.onNext(LoginViewState.ErrorMessage("Invalid email or password please try again"))
                }
            }
    }
    private fun getUserToken() {
        loginStateSubject.onNext(LoginViewState.LoadingState(true))
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                loginStateSubject.onNext(LoginViewState.ErrorMessage("Fetching FCM registration token failed"))
                return@OnCompleteListener
            }

            val token = task.result
            val map: MutableMap<String, String> = HashMap()
            map["fcm_token"] = token
            firebaseAuth.uid?.let { uid ->
                firestore.collection("user").document(uid).set(map, SetOptions.mergeFields("fcm_token"))
                    .addOnSuccessListener {
                        loginStateSubject.onNext(LoginViewState.TokenSuccessMessage("FCM token updated successfully"))
                    }
                    .addOnFailureListener { exception ->
                        loginStateSubject.onNext(LoginViewState.ErrorMessage(exception.message.toString()))
                    }
            }

        })
    }

    fun registerUser(email: String, password: String, firstName: String, lastName: String) {
        loginStateSubject.onNext(LoginViewState.LoadingState(true))
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
                                    // Save user data to Firestore
                                    val userId = user.uid
                                    val userMap = mapOf(
                                        "email" to email,
                                        "firstName" to firstName,
                                        "lastName" to lastName,
                                        "user_type" to "admin"
                                    )
                                    db.collection("user")
                                        .document(userId)
                                        .set(userMap)
                                        .addOnCompleteListener { dbTask ->
                                            if (dbTask.isSuccessful) {
                                                getUserToken()
                                                loginStateSubject.onNext(
                                                    LoginViewState.SuccessMessage(
                                                        "Registration successful"
                                                    )
                                                )
                                            } else {
                                                loginStateSubject.onNext(
                                                    LoginViewState.ErrorMessage(
                                                        dbTask.exception?.message.toString()
                                                    )
                                                )
                                            }
                                        }
                                } else {
                                    loginStateSubject.onNext(LoginViewState.ErrorMessage(profileTask.exception?.message.toString()))
                                }
                            }
                    }
                } else {
                    loginStateSubject.onNext(LoginViewState.ErrorMessage(task.exception?.message.toString()))
                }
            }
    }

    fun checkUserStatus() {
        loginStateSubject.onNext(LoginViewState.LoadingState(true))
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            firestore.collection("user")
                .whereEqualTo("email", currentUser.email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val isAdmin = querySnapshot.documents.any { document ->
                        document.getString("user_type") == "admin"
                    }
                    if (isAdmin) {
                        loginStateSubject.onNext(LoginViewState.UserStatusSuccess(true))
                    } else {
//                                _userStatus.value = Resource.Success(false) // Invalid user
                        loginStateSubject.onNext(LoginViewState.ErrorMessage("Invalid user please check email or password is incorrect"))
                    }
                }
                .addOnFailureListener {
                    loginStateSubject.onNext(LoginViewState.ErrorMessage("Failed to fetch user data."))
                }
        } else {
            loginStateSubject.onNext(LoginViewState.ErrorMessage("User not logged in."))
        }
    }

}

sealed class LoginViewState {
    data class ErrorMessage(val errorMessage: String) : LoginViewState()
    data class SuccessMessage(val successMessage: String) : LoginViewState()
    data class TokenSuccessMessage(val successMessage: String) : LoginViewState()
    data class LoadingState(val isLoading: Boolean) : LoginViewState()
    data class UserStatusSuccess(val userStatus: Boolean) : LoginViewState()
}