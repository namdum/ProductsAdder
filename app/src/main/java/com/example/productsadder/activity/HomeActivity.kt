package com.example.productsadder.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.productsadder.CategoryFragment
import com.example.productsadder.ProductsFragment
import com.example.productsadder.R
import com.example.productsadder.databinding.ActivityHomeBinding
import com.example.productsadder.OrderFragment
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryFragment=CategoryFragment()
        val productsFragment=ProductsFragment()
        val orderListFragment= OrderFragment()

        setCurrentFragment(categoryFragment)

        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.categoryFragment->setCurrentFragment(categoryFragment)
                R.id.productsFragment->setCurrentFragment(productsFragment)
                R.id.orderFragment->setCurrentFragment(orderListFragment)

            }
            true
        }
        binding.logoutButton.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle(getString(R.string.sign_out))
                setMessage(getString(R.string.are_you_sure_you_want_to_sign_out))
                setPositiveButton(getString(R.string.yes)) { _, _ ->
                    // Sign out the user
                    FirebaseAuth.getInstance().signOut()
                    finishAffinity()
                    startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                }
                setNegativeButton(getString(R.string.no)) { dialog, _ ->
                    // Dismiss the dialog
                    dialog.dismiss()
                }
                create()
                show()
            }
        }

    }

    private fun setCurrentFragment(fragment: Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.categoryHostFragment,fragment)
            commit()
        }
}