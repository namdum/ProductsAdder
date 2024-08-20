package com.example.productsadder

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.productsadder.databinding.ActivityHomeBinding
import com.example.productsadder.databinding.ActivityLoginBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.categoryHostFragment)
        binding.bottomNavigation.setupWithNavController(navController)

        val menu = binding.bottomNavigation.menu
        menu.findItem(R.id.categoryFragment).setOnMenuItemClickListener {
            navController.navigate(R.id.FragmentCategory)
            true
        }
        menu.findItem(R.id.productsFragment).setOnMenuItemClickListener {
            navController.navigate(R.id.FragmentProducts)
            true
        }
    }
}