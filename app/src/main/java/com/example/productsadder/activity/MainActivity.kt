package com.example.productsadder.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.productsadder.databinding.ActivityMainBinding
import com.example.productsadder.util.Resource
import com.example.productsadder.viewmodel.LoginViewModel
import com.google.api.ResourceProto.resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var loginViewModel: LoginViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
//        val firestore = FirebaseFirestore.getInstance()
//
//        auth = FirebaseAuth.getInstance()
//        if (auth.currentUser != null) {
//
//            firestore.collection("users")
//                .whereEqualTo("email", auth?.currentUser?.email)
//                .get()
//                .addOnSuccessListener { querySnapshot ->
//                    Log.i("test", querySnapshot.toString())
//
//                    firestore.collection("user").whereEqualTo("email", auth.currentUser?.email)
//                        .get().addOnSuccessListener { querySnapshot ->
//                            querySnapshot.documents.map { document ->
//                                if(document.getString("user_type").toString().equals("admin")) {
//                                    startActivity(Intent(this, HomeActivity::class.java))
//                                    finish()
//                                } else {
//                                    Toast.makeText(this@MainActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
//                                }
//                            }
//                        }.addOnFailureListener { exception ->
//                            Toast.makeText(this@MainActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
//                        }
//                }
//                .addOnFailureListener { exception ->
//                    Toast.makeText(this@MainActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
//                }
//        } else {
//            Handler(Looper.getMainLooper()).postDelayed({
//
//                val intent = Intent(this, LoginActivity::class.java)
//                startActivity(intent)
//                finish()
//            }, 1000)
//        }

        // Observe user status
        loginViewModel.userStatus.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Show a loading indicator
                    Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show()
                }
                is Resource.Success -> {
                    if (resource.data == true) {
                        // Admin user, navigate to HomeActivity
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        // Invalid user
                        Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    // Show error message
                    Toast.makeText(this, resource.message ?: "An error occurred", Toast.LENGTH_SHORT).show()
                }
                else->{}
            }
            if (resource is Resource.Error && resource.message == "User not logged in.") {
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }, 1000)
            }
        }

        // Check user status
        loginViewModel.checkUserStatus()


    }

    fun openLoginScreen() {
        Handler(Looper.getMainLooper()).postDelayed({

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 1000)
    }
}