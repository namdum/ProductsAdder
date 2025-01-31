package com.example.productsadder.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productsadder.model.Category
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.launch

class CategoryViewModel(private val firestore: FirebaseFirestore) : ViewModel() {

    private val categoryStateSubject: PublishSubject<CategoryViewState> = PublishSubject.create()
    val categoryState: Observable<CategoryViewState> = categoryStateSubject.hide()

    fun editCategory(oldCategory: Category, newCategory: Category) {
        val validateInputs = validateInputs(newCategory)

        if (validateInputs) {
            categoryStateSubject.onNext(CategoryViewState.LoadingState(true))
            val firestore = FirebaseFirestore.getInstance()

            firestore.collection("Category")
                .whereEqualTo("category", oldCategory.category)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.documents.isNotEmpty()) {
                        val document = querySnapshot.documents[0]
                        document.reference.update("image", newCategory.image,"category", newCategory.category )
                            .addOnSuccessListener {
                                categoryStateSubject.onNext(CategoryViewState.SuccessMessage("Category Update successfully.."))
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
        } else {
            categoryStateSubject.onNext(CategoryViewState.ErrorMessage("Category fields are required"))
        }
    }

    fun addCategory(category: Category) {
        val validateInputs = validateInputs(category)

        if (validateInputs) {
            viewModelScope.launch {
                categoryStateSubject.onNext(CategoryViewState.LoadingState(true))
            }
            firestore.collection("Category").document()
                .set(category).addOnSuccessListener {
                    viewModelScope.launch {
                        categoryStateSubject.onNext(CategoryViewState.SuccessMessage("Category Add successfully.."))
                    }
                }.addOnFailureListener {
                    viewModelScope.launch {
                        categoryStateSubject.onNext(CategoryViewState.ErrorMessage(it.message.toString()))
                    }
                }
        } else {
            viewModelScope.launch {
                categoryStateSubject.onNext(CategoryViewState.ErrorMessage("Category fields are required"))
            }
        }
    }

    fun fetchCategories() {
        categoryStateSubject.onNext(CategoryViewState.LoadingState(true))
        firestore.collection("Category")
            .get().addOnSuccessListener { querySnapshot ->
                val categories = querySnapshot.documents.map { document ->
                    Category(document.getString("image")!!,document.getString("category")!!)
                }
                viewModelScope.launch {
                    categoryStateSubject.onNext(CategoryViewState.FetchCategorySuccess(categories))
                }
            }.addOnFailureListener { exception ->
                viewModelScope.launch {
                    categoryStateSubject.onNext(CategoryViewState.ErrorMessage(exception.message.toString()))
                }
            }
    }

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
                                categoryStateSubject.onNext(CategoryViewState.SuccessMessage("Category Delete successfully.."))
                                fetchCategories()
                            }
                            .addOnFailureListener { exception ->
                                categoryStateSubject.onNext(CategoryViewState.ErrorMessage(exception.message.toString()))
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    categoryStateSubject.onNext(CategoryViewState.ErrorMessage(exception.message.toString()))
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
    data class FetchCategorySuccess(val fetchCategorys: List<Category>) : CategoryViewState()
}