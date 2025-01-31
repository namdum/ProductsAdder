package com.example.productsadder.di

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestoreDatatbase() = Firebase.firestore
}