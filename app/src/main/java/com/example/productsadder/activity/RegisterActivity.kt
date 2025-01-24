package com.example.productsadder.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.productsadder.databinding.ActivityRegisterBinding
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.viewmodel.LoginViewModel
import com.example.productsadder.viewmodel.LoginViewModelFactory
import com.example.productsadder.viewmodel.LoginViewState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
private lateinit var viewModel: LoginViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewModelFactory = LoginViewModelFactory(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]

        listenToViewEvent()
        listenToViewModel()

    }

    private fun listenToViewEvent() {

        binding.registerRegisterBtn.setOnClickListener {
            registerUser()
        }

        binding.registerHeaderText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun listenToViewModel() {
        viewModel.loginState.subscribeAndObserveOnMainThread {
            when(it){
                is LoginViewState.LoadingState->{
                    binding.progressbar.isVisible=true
                    binding.registerRegisterBtn.isVisible=false
                }
                is LoginViewState.SuccessMessage->{
                    Toast.makeText(this, it.successMessage, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    binding.progressbar.isVisible=false
                    binding.registerRegisterBtn.isVisible=true
                }
                is LoginViewState.ErrorMessage->{
                    Toast.makeText(this, it.errorMessage, Toast.LENGTH_SHORT).show()
                    binding.progressbar.isVisible=false
                    binding.registerRegisterBtn.isVisible=true
                }

                else->{}
            }
        }
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

    override fun getOnBackInvokedDispatcher(): OnBackInvokedDispatcher {
        return super.getOnBackInvokedDispatcher()
        finish()
    }
}