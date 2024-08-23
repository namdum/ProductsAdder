package com.example.productsadder.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productsadder.data.Category
import com.example.productsadder.data.Product
import com.example.productsadder.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel(private val firestore: FirebaseFirestore, private val auth: FirebaseAuth) : ViewModel() {

    private val _addNewProduct = MutableStateFlow<Resource<Product>>(Resource.Unspecified())
    val addNewProduct = _addNewProduct.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    private val _editProduct = MutableStateFlow<Resource<Product>>(Resource.Unspecified())
    val editProduct = _editProduct.asStateFlow()

    fun addProduct(product: Product) {
        val validateInputs = validateInputs(product)

        if (validateInputs) {
            viewModelScope.launch { _addNewProduct.emit(Resource.Loading()) }
            firestore.collection("Product").document()
                .set(product).addOnSuccessListener {
                    Log.i("test", product.toString())
                    viewModelScope.launch {
                        _addNewProduct.emit(Resource.Success(product))
                    }
                }.addOnFailureListener { e ->
                    viewModelScope.launch {
                        _addNewProduct.emit(Resource.Error(e.message.toString()))
                    }
                }
        } else {
            viewModelScope.launch {
                _error.emit("Product fields are required")
            }
        }
    }

    fun fetchProducts() {
        firestore.collection("Product")
            .get().addOnSuccessListener { querySnapshot ->
                Log.d("test", "Fetched products: ${querySnapshot.documents.size}")
                Log.d("test", "Fetched products: ${querySnapshot.documents}")
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
                    _products.emit(products)
                }
            }.addOnFailureListener { exception ->
                viewModelScope.launch {
                    _error.emit(exception.message.toString())
                }
            }
    }

    private fun validateInputs(product: Product): Boolean {
        return product.name.isNotEmpty() &&
                product.category.isNotEmpty() &&
                product.description?.isNotEmpty() ?: true &&
                product.price > 0 &&
                product.sizes?.isNotEmpty() ?: true &&
                product.colors?.isNotEmpty() ?: true
    }


    fun editProduct(oldProduct: Product, newProduct: Product) {
        val validateInputs = validateInputs(newProduct)

        if (validateInputs) {
            _editProduct.value = Resource.Loading()
            val firestore = FirebaseFirestore.getInstance()

            firestore.collection("Product")
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
                                _editProduct.value = Resource.Success(newProduct)
                            }
                            .addOnFailureListener { exception ->
                                _editProduct.value = Resource.Error(exception.message.toString())
                            }
                    } else {
                        _editProduct.value = Resource.Error("Product not found")
                    }
                }
                .addOnFailureListener { exception ->
                    _editProduct.value = Resource.Error(exception.message.toString())
                }
        } else {
            _editProduct.value = Resource.Error("Product fields are required")
        }
    }
}