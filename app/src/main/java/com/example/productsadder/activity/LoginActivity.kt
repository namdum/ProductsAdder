package com.example.productsadder.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.productsadder.databinding.ActivityLoginBinding
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.viewmodel.LoginViewModel
import com.example.productsadder.viewmodel.LoginViewModelFactory
import com.example.productsadder.viewmodel.LoginViewState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory = LoginViewModelFactory(FirebaseAuth.getInstance(),FirebaseFirestore.getInstance())
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]

        listenToViewEvent()
        listenToViewModel()
    }

    private fun listenToViewEvent() {
        binding.loginEmailEdit.setText("")
        binding.loginPasswordEdit.setText("")

        binding.loginLoginBtn.setOnClickListener {
            val email = binding.loginEmailEdit.text.toString()
            val password = binding.loginPasswordEdit.text.toString()

            if(email.isNullOrEmpty()|| password.isNullOrEmpty()){
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
            else {
                viewModel.login(email, password)
            }
        }

        binding.dontHaveAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun listenToViewModel() {
        viewModel.loginState.subscribeAndObserveOnMainThread {
            when(it){
                is LoginViewState.LoadingState->{
                    binding.progressbar.isVisible=true
                    binding.loginLoginBtn.isVisible=false
                }
                is LoginViewState.SuccessMessage->{
                    binding.progressbar.isVisible=false
                    binding.loginLoginBtn.isVisible=true
                    Toast.makeText(this, it.successMessage, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                }
                is LoginViewState.ErrorMessage->{

                    Toast.makeText(this, it.errorMessage, Toast.LENGTH_SHORT).show()
                    binding.progressbar.isVisible=false
                    binding.loginLoginBtn.isVisible=true
                }

                else->{}
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.loginEmailEdit.setText("")
        binding.loginPasswordEdit.setText("")

    }
}