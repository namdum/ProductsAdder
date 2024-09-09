package com.example.productsadder.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.productsadder.R
import com.example.productsadder.data.Product
import com.example.productsadder.databinding.ActivityEditeProductBinding
import com.example.productsadder.databinding.ActivityOrderDetailsBinding
import com.google.firebase.firestore.FirebaseFirestore

class OrderDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        fetchCategories()
        binding.apply {
            imageClose.setOnClickListener {  }
            onBackPressed()
        }
    } private fun fetchCategories() {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("orders")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val categories = querySnapshot.documents.map { document ->
                    document.getString("status") ?: ""
                }

                populateSpinner(categories)
            }
            .addOnFailureListener { exception ->
                Log.e("Error", "Error fetching categories: $exception")
            }
    }
    private fun populateSpinner(categories: List<String>) {
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        binding.statusAppCompatSpinner.adapter = spinnerAdapter

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val product: Product? = intent.getParcelableExtra("orders")
        val categoryIndex = categories.indexOf(product?.category ?: "")

        binding.statusAppCompatSpinner.setSelection(categoryIndex)
        binding.statusAppCompatSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedCategory = categories[position]
                Log.d("Selected Category", selectedCategory)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }
}