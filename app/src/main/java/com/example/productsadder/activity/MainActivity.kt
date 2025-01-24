package com.example.productsadder.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.productsadder.databinding.ActivityMainBinding
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.util.Resource
import com.example.productsadder.viewmodel.LoginViewModel
import com.example.productsadder.viewmodel.LoginViewModelFactory
import com.example.productsadder.viewmodel.LoginViewState
import com.google.api.ResourceProto.resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
//    private lateinit var loginViewModel: LoginViewModel

    private lateinit var viewModel: LoginViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewModelFactory = LoginViewModelFactory(FirebaseAuth.getInstance(),FirebaseFirestore.getInstance())
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Check user status
        viewModel.checkUserStatus()

        // Observe user status
        viewModel.userStatus.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Show a loading indicator
                }
                is Resource.Success -> {
                    if (resource.data == true) {
                        // Admin user, navigate to HomeActivity
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        // Invalid user
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
                is Resource.Error -> {
                    // Show error message
                    Log.d("MyTesting","error:-${resource.message}")
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else->{}
            }
        }



//        listenToViewModel()

    }

    private fun listenToViewModel() {
        Log.d("MyTesting","listenToViewModel..")
                viewModel.loginState.subscribeAndObserveOnMainThread {
            when(it){
                is LoginViewState.LoadingState->{
                    Log.d("MyTesting","Loading..")
                }
                is LoginViewState.UserStatusSuccess->{
                    if (it.userStatus == true) {
                        // Admin user, navigate to HomeActivity
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                        Log.d("MyTesting","true..")
                    } else {
                        // Invalid user
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                        Log.d("MyTesting","false..")
                    }
                }
                is LoginViewState.ErrorMessage->{
                    Log.d("MyTesting","User not logged in")
                    Log.d("MyTesting","error:-${it.errorMessage}")
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is LoginViewState.UserStatusErrorMessage->{
//                    Log.d("MyTesting","User not logged in....")
                    Log.d("MyTesting","error...:-${it.errorMessage}")
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                else->{}
            }
        }
    }
}