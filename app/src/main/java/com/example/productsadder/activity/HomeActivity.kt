package com.example.productsadder.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.productsadder.CategoryFragment
import com.example.productsadder.ProductsFragment
import com.example.productsadder.R
import com.example.productsadder.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryFragment=CategoryFragment()
        val productsFragment=ProductsFragment()

        setCurrentFragment(categoryFragment)

        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.categoryFragment->setCurrentFragment(categoryFragment)
                R.id.productsFragment->setCurrentFragment(productsFragment)

            }
            true
        }

        binding.logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            finish()
            startActivity(Intent(this,LoginActivity::class.java))
        }

    }

    private fun setCurrentFragment(fragment: Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.categoryHostFragment,fragment)
            commit()
        }

//    val navController = findNavController(R.id.categoryHostFragment)
//        binding.bottomNavigation.setupWithNavController(navController)
//
//        val menu = binding.bottomNavigation.menu
//        menu.findItem(R.id.categoryFragment).setOnMenuItemClickListener {
//            binding.bottomNavigation.menu.findItem(R.id.categoryFragment).isChecked = true
//            binding.bottomNavigation.menu.findItem(R.id.productsFragment).isChecked = false
//            navController.navigate(R.id.FragmentCategory)
//            true
//        }
//        menu.findItem(R.id.productsFragment).setOnMenuItemClickListener {
//            binding.bottomNavigation.menu.findItem(R.id.categoryFragment).isChecked = false
//            binding.bottomNavigation.menu.findItem(R.id.productsFragment).isChecked = true
//            navController.navigate(R.id.FragmentProducts)
//            true
//        }
//    }
}