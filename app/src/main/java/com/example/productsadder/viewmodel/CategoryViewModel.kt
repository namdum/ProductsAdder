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

    private val _addNewCategory = MutableStateFlow<Resource<Category>>(Resource.Unspecified())
    val addNewCategory = _addNewCategory.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    private val _editCategory = MutableStateFlow<Resource<Category>>(Resource.Unspecified())
    val editCategory = _editCategory.asStateFlow()

    fun editCategory(oldCategory: Category, newCategory: Category) {
        val validateInputs = validateInputs(newCategory)

        if (validateInputs) {
            _editCategory.value = Resource.Loading()
            val firestore = FirebaseFirestore.getInstance()
            val userId = FirebaseAuth.getInstance().uid!!

            firestore.collection("user").document(userId).collection("Category")
                .whereEqualTo("category", oldCategory.category)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    Log.i("test", newCategory.toString())
                    if (querySnapshot.documents.isNotEmpty()) {
                        val document = querySnapshot.documents[0]
                        document.reference.update("image", newCategory.image,"category", newCategory.category )
                            .addOnSuccessListener {
                                _editCategory.value = Resource.Success(newCategory)
                                val currentCategories = _categories.value.toMutableList()
                                val index = currentCategories.indexOf(oldCategory)
                                if (index != -1) {
                                    currentCategories[index] = newCategory
                                    _categories.value = currentCategories
                                }
                            }
                            .addOnFailureListener { exception ->
                                _editCategory.value = Resource.Error(exception.message.toString())
                            }
                    } else {
                        _editCategory.value = Resource.Error("Category not found")
                    }
                }
                .addOnFailureListener { exception ->
                    _editCategory.value = Resource.Error(exception.message.toString())
                }
        } else {
            _editCategory.value = Resource.Error("Category fields are required")
        }
    }

    fun addCategory(category: Category) {
        val validateInputs = validateInputs(category)

        if (validateInputs) {
            viewModelScope.launch { _addNewCategory.emit(Resource.Loading()) }
        firestore.collection("user").document(auth.uid!!).collection("Category").document()
                .set(category).addOnSuccessListener {
                    Log.i("test", category.toString())
                viewModelScope.launch {
                    val currentCategories = categories.value.toMutableList()
                    currentCategories.add(category)
                    _categories.emit(currentCategories)
                    _addNewCategory.emit(Resource.Success(category))
                }
                }.addOnFailureListener {
                    viewModelScope.launch { _addNewCategory.emit(Resource.Error(it.message.toString())) }
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
                    Category(document.getString("image")!!,document.getString("category")!!)
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
        return category.category.trim().isNotEmpty() && category.image.trim().isNotEmpty()
    }
}