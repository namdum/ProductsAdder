package com.example.productsadder.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.productsadder.R
import com.example.productsadder.databinding.ActivityAddCategoryBinding
import com.example.productsadder.databinding.ActivityAddProductBinding

class AddProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProductBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageClose.setOnClickListener {
            finish()
        }
    }
}