package com.example.productsadder.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.productsadder.R
import com.example.productsadder.databinding.ActivityAddProductBinding
import com.example.productsadder.databinding.ActivityEditeProductBinding

class EditeProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditeProductBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditeProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageClose.setOnClickListener {
            finish()
        }

    }
}