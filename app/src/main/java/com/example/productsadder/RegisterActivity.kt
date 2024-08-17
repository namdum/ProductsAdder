package com.example.productsadder

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.productsadder.databinding.ActivityRegisterBinding
import com.example.productsadder.util.Resource
import com.example.productsadder.viewmodel.LoginViewModel
import com.example.productsadder.viewmodel.LoginViewModelFactory
import com.example.productsadder.viewmodel.RegisterViewModel
import com.example.productsadder.viewmodel.RegisterViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewModelFactory = RegisterViewModelFactory(FirebaseAuth.getInstance())
        viewModel = ViewModelProvider(this, viewModelFactory)[RegisterViewModel::class.java]

        binding.registerHeaderText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.registerRegisterBtn.setOnClickListener {
            registerUser()
        }

        viewModel.registerResult.observe(this, Observer { result ->
            when (result) {
                is Resource.Success -> {
                    Toast.makeText(this, result.data, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                }
                is Resource.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                }

                else -> {}
            }
        })
    }

    private fun registerUser() {
        val email = binding.registerEmailEdit.text.toString()
        val password = binding.registerPasswordEdit.text.toString()
        val firstName = binding.registerFirstnameEdit.text.toString()
        val lastName = binding.registerLastnameEdit.text.toString()

        if (firstName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && lastName.isNotEmpty()) {
            viewModel.registerUser(email, password, firstName, lastName)
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }
}