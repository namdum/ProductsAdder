package com.example.productsadder.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.productsadder.application.ProductAdderApplication
import com.example.productsadder.databinding.ActivityRegisterBinding
import com.example.productsadder.network.extension.getViewModelFromFactory
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.di.ViewModelFactory
import com.example.productsadder.ui.viewmodel.LoginViewModel
import com.example.productsadder.ui.viewmodel.LoginViewState
import javax.inject.Inject

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    @Inject
    internal lateinit var registerViewModelFactory: ViewModelFactory<LoginViewModel>
    lateinit var viewModel: LoginViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ProductAdderApplication.component.inject(this)
        viewModel = getViewModelFromFactory(registerViewModelFactory)

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