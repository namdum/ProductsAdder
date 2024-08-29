package com.example.productsadder.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.productsadder.databinding.ActivityLoginBinding
import com.example.productsadder.util.Resource
import com.example.productsadder.viewmodel.LoginViewModel
import com.example.productsadder.viewmodel.LoginViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val firestore = FirebaseFirestore.getInstance()

        val viewModelFactory = LoginViewModelFactory(FirebaseAuth.getInstance())
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {

            firestore.collection("users")
                .whereEqualTo("email", auth?.currentUser?.email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    Log.i("test", querySnapshot.toString())

                    firestore.collection("user").whereEqualTo("email", auth.currentUser?.email)
                        .get().addOnSuccessListener { querySnapshot ->
                            querySnapshot.documents.map { document ->
                                if(document.getString("user_type").toString().equals("admin")) {
                                    startActivity(Intent(this, HomeActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }.addOnFailureListener { exception ->
                            Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
        }
        binding.loginLoginBtn.setOnClickListener {
            val email = binding.loginEmailEdit.text.toString()
            val password = binding.loginPasswordEdit.text.toString()
            viewModel.login(email, password)
        }

        viewModel.loginResult.observe(this, Observer { result ->
            when (result) {
                is Resource.Success -> {
                    Toast.makeText(this, result.data, Toast.LENGTH_SHORT).show()
                    firestore.collection("users")
                        .whereEqualTo("email", auth?.currentUser?.email)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            Log.i("test", querySnapshot.toString())

                            firestore.collection("user").whereEqualTo("email", auth.currentUser?.email)
                                .get().addOnSuccessListener { querySnapshot ->
                                    querySnapshot.documents.map { document ->
                                        if(document.getString("user_type").toString().equals("admin")) {
                                            startActivity(Intent(this, HomeActivity::class.java))
                                            finish()
                                        } else {
                                            Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }.addOnFailureListener { exception ->
                                    Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                        }

                }
                is Resource.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                }

                else -> {}
            }
        })

        binding.dontHaveAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}