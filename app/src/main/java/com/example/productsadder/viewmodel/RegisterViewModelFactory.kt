package com.example.productsadder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.inject.Provider
import dagger.internal.DaggerGenerated
import dagger.internal.QualifierMetadata
import dagger.internal.ScopeMetadata
import dagger.internal.Factory


@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
class RegisterViewModelFactory(private val firebaseAuth: FirebaseAuth) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegisterViewModel(firebaseAuth) as T
    }
}

//class RegisterViewModelFactory(
//    private val firebaseAuthProvider: Provider<FirebaseAuth>,
//    private val dbProvider: Provider<FirebaseFirestore>
//) : Factory<RegisterViewModel> {
//
//    override fun get(): RegisterViewModel {
//        return newInstance(firebaseAuthProvider.get(), dbProvider.get())
//    }
//
//    companion object {
//        fun create(firebaseAuthProvider: Provider<FirebaseAuth>, dbProvider: Provider<FirebaseFirestore>): RegisterViewModelFactory {
//            return RegisterViewModelFactory(firebaseAuthProvider, dbProvider)
//        }
//
//        fun newInstance(firebaseAuth: FirebaseAuth, db: FirebaseFirestore): RegisterViewModel {
//            return RegisterViewModel(firebaseAuth, db)
//        }
//    }
//}