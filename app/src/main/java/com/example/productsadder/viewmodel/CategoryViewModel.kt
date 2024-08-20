package com.example.productsadder.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productsadder.data.Category
import com.example.productsadder.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(private val firestore: FirebaseFirestore, private val auth: FirebaseAuth) : ViewModel() {

    private val _addNewAddress = MutableStateFlow<Resource<Category>>(Resource.Unspecified())
    val addNewAddress = _addNewAddress.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    fun addCategory(category: Category) {
        val validateInputs = validateInputs(category)

        if (validateInputs) {
            viewModelScope.launch { _addNewAddress.emit(Resource.Loading()) }
        firestore.collection("user").document(auth.uid!!).collection("Category").document()
                .set(category).addOnSuccessListener {
                    Log.i("test", category.toString())
                viewModelScope.launch {
                    val currentCategories = categories.value.toMutableList()
                    currentCategories.add(category)
                    _categories.emit(currentCategories)
                    _addNewAddress.emit(Resource.Success(category))
                }
                    //viewModelScope.launch { _addNewAddress.emit(Resource.Success(category)) }
                }.addOnFailureListener {
                    viewModelScope.launch { _addNewAddress.emit(Resource.Error(it.message.toString())) }
                }
        } else {
            viewModelScope.launch {
                _error.emit("Category fields are required")
            }
        }
    }
    fun fetchCategories() {
        firestore.collection("user").document(auth.uid!!).collection("Category")
            .get().addOnSuccessListener { querySnapshot ->
                val categories = querySnapshot.documents.map { document ->
                    Category(document.getString("category")!!)
                }
                viewModelScope.launch {
                    _categories.emit(categories)
                }
            }.addOnFailureListener { exception ->
                viewModelScope.launch {
                    _error.emit(exception.message.toString())
                }
            }
    }


    private fun validateInputs(category: Category): Boolean {
        return category.category.trim().isNotEmpty()
    }
}