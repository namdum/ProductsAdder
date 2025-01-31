package com.example.productsadder.di

import com.example.productsadder.ui.viewmodel.CategoryViewModel
import com.example.productsadder.ui.viewmodel.LoginViewModel
import com.example.productsadder.ui.viewmodel.OrderListViewModel
import com.example.productsadder.ui.viewmodel.OrderViewModel
import com.example.productsadder.ui.viewmodel.ProductViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ProductAdderModelModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    @Provides
    fun provideOrderListViewModel(firestore: FirebaseFirestore): OrderListViewModel {
        return OrderListViewModel(firestore)
    }
    @Provides
    fun provideCategoryListViewModel(firestore: FirebaseFirestore): CategoryViewModel {
        return CategoryViewModel(firestore)
    }

    @Provides
    fun provideLoginViewModel(firebaseAuth: FirebaseAuth,firestore: FirebaseFirestore): LoginViewModel {
        return LoginViewModel(firebaseAuth,firestore)
    }
    @Provides
    fun provideProductViewModel(firestore: FirebaseFirestore): ProductViewModel {
        return ProductViewModel(firestore)
    }

    @Provides
    fun provideOrderViewModel(): OrderViewModel {
        return OrderViewModel()
    }
}