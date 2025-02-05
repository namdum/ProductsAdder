package com.example.productsadder.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.productsadder.application.ProductAdderApplication
import com.example.productsadder.databinding.ActivityLoginBinding
import com.example.productsadder.network.extension.getViewModelFromFactory
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.di.ViewModelFactory
import com.example.productsadder.network.extension.hideKeyboard
import com.example.productsadder.ui.viewmodel.LoginViewModel
import com.example.productsadder.ui.viewmodel.LoginViewState
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    @Inject
    internal lateinit var loginViewModelFactory: ViewModelFactory<LoginViewModel>
    lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ProductAdderApplication.component.inject(this)
        viewModel = getViewModelFromFactory(loginViewModelFactory)

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
                hideKeyboard()
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