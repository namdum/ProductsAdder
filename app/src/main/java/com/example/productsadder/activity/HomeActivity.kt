package com.example.productsadder.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.productsadder.R
import com.example.productsadder.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth

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

        binding.logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this,LoginActivity::class.java))
        }
    }
}