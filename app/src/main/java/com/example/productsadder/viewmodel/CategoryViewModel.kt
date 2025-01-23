package com.example.productsadder.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productsadder.data.Category
import com.example.productsadder.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(private val firestore: FirebaseFirestore, private val auth: FirebaseAuth) : ViewModel() {

    private val categoryStateSubject: PublishSubject<CategoryViewState> = PublishSubject.create()
    val categoryState: Observable<CategoryViewState> = categoryStateSubject.hide()

    fun editCategory(oldCategory: Category, newCategory: Category) {
        if (validateInputs(newCategory)) {
            categoryStateSubject.onNext(CategoryViewState.LoadingState(true))
            firestore.collection("Category")
                .whereEqualTo("category", oldCategory.category)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.documents.isNotEmpty()) {
                        val document = querySnapshot.documents[0]
                        document.reference.update("image", newCategory.image, "category", newCategory.category)
                            .addOnSuccessListener {
                                categoryStateSubject.onNext(CategoryViewState.SuccessMessage("Category updated successfully"))
                            }
                            .addOnFailureListener { exception ->
                                categoryStateSubject.onNext(CategoryViewState.ErrorMessage(exception.message.toString()))
                            }
                    } else {
                        categoryStateSubject.onNext(CategoryViewState.ErrorMessage("Category not found"))
                    }
                }
                .addOnFailureListener { exception ->
                    categoryStateSubject.onNext(CategoryViewState.ErrorMessage(exception.message.toString()))
                }
                .addOnCompleteListener {
                    categoryStateSubject.onNext(CategoryViewState.LoadingState(false))
                }
        } else {
            categoryStateSubject.onNext(CategoryViewState.ErrorMessage("Category fields are required"))
        }
    }

    fun addCategory(category: Category) {
        if (validateInputs(category)) {
            categoryStateSubject.onNext(CategoryViewState.LoadingState(true))
            firestore.collection("Category").document()
                .set(category)
                .addOnSuccessListener {
                    categoryStateSubject.onNext(CategoryViewState.SuccessMessage("Category added successfully"))
                }
                .addOnFailureListener { exception ->
                    categoryStateSubject.onNext(CategoryViewState.ErrorMessage(exception.message.toString()))
                }
                .addOnCompleteListener {
                    categoryStateSubject.onNext(CategoryViewState.LoadingState(false))
                }
        } else {
            categoryStateSubject.onNext(CategoryViewState.ErrorMessage("Category fields are required"))
        }
    }

    fun fetchCategories() {
        categoryStateSubject.onNext(CategoryViewState.LoadingState(true))
        firestore.collection("Category")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val categories = querySnapshot.documents.map { document ->
                    Category(document.getString("image")!!, document.getString("category")!!)
                }
                categoryStateSubject.onNext(CategoryViewState.FetchCategoriesSuccess(categories))
            }
            .addOnFailureListener { exception ->
                categoryStateSubject.onNext(CategoryViewState.ErrorMessage(exception.message.toString()))
            }
            .addOnCompleteListener {
                categoryStateSubject.onNext(CategoryViewState.LoadingState(false))
            }
    }
//    fun deleteCategory(category: Category) {
//        categoryStateSubject.onNext(CategoryViewState.LoadingState(true))
//        firestore.collection("Category")
//            .whereEqualTo("category", category.category)
//            .get()
//            .addOnSuccessListener { querySnapshot ->
//                if (querySnapshot.documents.isNotEmpty()) {
//                    val documentId = querySnapshot.documents[0].id
//                    firestore.collection("Category").document(documentId)
//                        .delete()
//                        .addOnSuccessListener {
//                            categoryStateSubject.onNext(CategoryViewState.SuccessMessage("Category deleted successfully"))
//                        }
//                        .addOnFailureListener { exception ->
//                            categoryStateSubject.onNext(CategoryViewState.ErrorMessage("Error deleting category: ${exception.message}"))
//                        }
//                } else {
//                    categoryStateSubject.onNext(CategoryViewState.ErrorMessage("Category not found"))
//                }
//            }
//            .addOnFailureListener { exception ->
//                categoryStateSubject.onNext(CategoryViewState.ErrorMessage("Error getting category: ${exception.message}"))
//            }
//            .addOnCompleteListener {
//                categoryStateSubject.onNext(CategoryViewState.LoadingState(false))
//            }
//    }

    fun deleteCategory(category: Category) {
        categoryStateSubject.onNext(CategoryViewState.LoadingState(true))
        firestore.collection("Category")
            .whereEqualTo("category", category.category)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    val documentId = querySnapshot.documents[0].id
                    firestore.collection("Category").document(documentId)
                        .delete()
                        .addOnSuccessListener {
                            // Update adapter data after deletion
                            categoryStateSubject.onNext(CategoryViewState.CategoryDeleted(category))
                            categoryStateSubject.onNext(CategoryViewState.SuccessMessage("Category deleted successfully"))
                            Log.d("DeleteCategory", "Emitted CategoryDeleted state for: $category")

                        }
                        .addOnFailureListener { exception ->
                            categoryStateSubject.onNext(CategoryViewState.ErrorMessage("Error deleting category: ${exception.message}"))
                        }
                } else {
                    categoryStateSubject.onNext(CategoryViewState.ErrorMessage("Category not found"))
                }
            }
            .addOnFailureListener { exception ->
                categoryStateSubject.onNext(CategoryViewState.ErrorMessage("Error getting category: ${exception.message}"))
            }
            .addOnCompleteListener {
                categoryStateSubject.onNext(CategoryViewState.LoadingState(false))
            }
    }
    private fun validateInputs(category: Category): Boolean {
        return category.category.trim().isNotEmpty() && category.image.trim().isNotEmpty()
    }
}

sealed class CategoryViewState {
    data class ErrorMessage(val errorMessage: String) : CategoryViewState()
    data class SuccessMessage(val successMessage: String) : CategoryViewState()
    data class LoadingState(val isLoading: Boolean) : CategoryViewState()
    data class FetchCategoriesSuccess(val fetchCategories: List<Category>) : CategoryViewState()
    data class CategoryDeleted(val category: Category) : CategoryViewState()
}