package com.example.productsadder.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productsadder.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.launch

class ProductViewModel(private val firestore: FirebaseFirestore) : ViewModel() {

    private val productStateSubject: PublishSubject<ProductViewState> = PublishSubject.create()
    val productState: Observable<ProductViewState> = productStateSubject.hide()

    fun addProduct(product: Product) {
        val validateInputs = validateInputs(product)

        if (validateInputs) {
            viewModelScope.launch {
                productStateSubject.onNext(ProductViewState.LoadingState(true))
            }
            firestore.collection("Products").document()
                .set(product).addOnSuccessListener {
                    viewModelScope.launch {
                        productStateSubject.onNext(ProductViewState.SuccessMessage("Product Add successfully.."))
                    }
                }.addOnFailureListener { e ->
                    viewModelScope.launch {
                        productStateSubject.onNext(ProductViewState.ErrorMessage(e.message.toString()))
                    }
                }
        } else {
            viewModelScope.launch {
                productStateSubject.onNext(ProductViewState.ErrorMessage("Product fields are required"))
            }
        }
    }

    fun fetchProducts() {
        productStateSubject.onNext(ProductViewState.LoadingState(true))
        firestore.collection("Products")
            .get().addOnSuccessListener { querySnapshot ->
                val products = querySnapshot.documents.map { document ->
                    val name = document.getString("name") ?: ""
                    val category = document.getString("category") ?: ""
                    val description = document.getString("description") ?: ""
                    val price = document.getDouble("price")?.toFloat() ?: 0f
                    val offerPercentage = document.getDouble("offerPercentage")?.toFloat() ?: 0f
                    val size = document.get("sizes") as? List<String> ?: mutableListOf()
                    val colorStrings = document.get("colors") as? List<Long> ?: mutableListOf()
                    val colors = colorStrings.map { it.toInt() ?: 0 }
                    val images = document.get("images") as? List<String> ?: mutableListOf()

                    Product(name, category, price, offerPercentage, description, size, colors, images.toMutableList())
                }
                viewModelScope.launch {
                    productStateSubject.onNext(ProductViewState.FetchProductSuccess(products))

                }
            }.addOnFailureListener { exception ->
                viewModelScope.launch {
                    productStateSubject.onNext(ProductViewState.ErrorMessage(exception.message.toString()))
                }
            }
    }

     fun deleteProduct(product: Product) {
         productStateSubject.onNext(ProductViewState.LoadingState(true))

         val firestore = FirebaseFirestore.getInstance()

            firestore.collection("Products")
                .whereEqualTo("name", product.name)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.documents.isNotEmpty()) {
                        val documentId = querySnapshot.documents[0].id
                        firestore.collection("Products").document(documentId)
                            .delete()
                            .addOnSuccessListener {
                               fetchProducts()
                                productStateSubject.onNext(ProductViewState.SuccessMessage("Product Delete successfully.."))
                            }
                            .addOnFailureListener { exception ->
                                productStateSubject.onNext(ProductViewState.ErrorMessage(exception.message.toString()))
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    productStateSubject.onNext(ProductViewState.ErrorMessage(exception.message.toString()))
                }

    }

    private fun validateInputs(product: Product): Boolean {
        return product.name.isNotEmpty() &&
                product.category.isNotEmpty() &&
                product.price > 0 &&
                product.images.isNotEmpty()
    }


    fun editProduct(oldProduct: Product, newProduct: Product) {
        val validateInputs = validateInputs(newProduct)

        if (validateInputs) {
            productStateSubject.onNext(ProductViewState.LoadingState(true))
            val firestore = FirebaseFirestore.getInstance()

            firestore.collection("Products")
                .whereEqualTo("name", oldProduct.name)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    Log.i("test", newProduct.toString())
                    if (querySnapshot.documents.isNotEmpty()) {
                        val document = querySnapshot.documents[0]
                        val productMap = hashMapOf(
                            "name" to newProduct.name,
                            "category" to newProduct.category,
                            "description" to newProduct.description,
                            "price" to newProduct.price,
                            "offerPercentage" to newProduct.offerPercentage,
                            "sizes" to newProduct.sizes,
                            "colors" to newProduct.colors,
                            "images" to newProduct.images
                        )
                        document.reference.update(productMap)
                            .addOnSuccessListener {
                                productStateSubject.onNext(ProductViewState.SuccessMessage("Product Update successfully.."))
                            }
                            .addOnFailureListener { exception ->
                                productStateSubject.onNext(ProductViewState.ErrorMessage(exception.message.toString()))
                            }
                    } else {
                        productStateSubject.onNext(ProductViewState.ErrorMessage("Product not found"))
                    }
                }
                .addOnFailureListener { exception ->
                    productStateSubject.onNext(ProductViewState.ErrorMessage(exception.message.toString()))
                }
        } else {
            productStateSubject.onNext(ProductViewState.ErrorMessage("Product fields are required"))
        }
    }

}

sealed class ProductViewState {
    data class ErrorMessage(val errorMessage: String) : ProductViewState()
    data class SuccessMessage(val successMessage: String) : ProductViewState()
    data class LoadingState(val isLoading: Boolean) : ProductViewState()
    data class FetchProductSuccess(val fetchProducts: List<Product>) : ProductViewState()
}