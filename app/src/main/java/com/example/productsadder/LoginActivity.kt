package com.example.productsadder

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.productsadder.databinding.ActivityLoginBinding
import com.example.productsadder.util.Resource
import com.example.productsadder.viewmodel.LoginViewModel
import com.example.productsadder.viewmodel.LoginViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewModelFactory = LoginViewModelFactory(FirebaseAuth.getInstance())
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]

        binding.loginLoginBtn.setOnClickListener {
            val email = binding.loginEmailEdit.text.toString()
            val password = binding.loginPasswordEdit.text.toString()
            viewModel.login(email, password)
        }

        viewModel.loginResult.observe(this, Observer { result ->
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

        binding.dontHaveAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}