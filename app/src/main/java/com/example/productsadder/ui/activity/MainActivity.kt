package com.example.productsadder.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.productsadder.application.ProductAdderApplication
import com.example.productsadder.databinding.ActivityMainBinding
import com.example.productsadder.network.extension.getViewModelFromFactory
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.di.ViewModelFactory
import com.example.productsadder.util.Resource
import com.example.productsadder.ui.viewmodel.LoginViewModel
import com.example.productsadder.ui.viewmodel.LoginViewState
import retrofit2.http.Headers
import timber.log.Timber
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
@Inject
internal lateinit var loginViewModelFactory: ViewModelFactory<LoginViewModel>
    lateinit var viewModel: LoginViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ProductAdderApplication.component.inject(this)
        viewModel = getViewModelFromFactory(loginViewModelFactory)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        viewModel.checkUserStatus()

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
                    Timber.d("error:-${resource.message}")
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else->{}
            }
        }


//        Handler(Looper.getMainLooper()).postDelayed({
//            listenToViewModel()
//        }, 1000)



    }

    private fun listenToViewModel() {

        Timber.d("listenToViewModel..")

        viewModel.userStatuss.subscribeAndObserveOnMainThread { state->
            Log.d("MyTesting","listenToViewModel...")
            when(state){
                is LoginViewState.LoadingState->{
                   Log.d("MyTesting","LoadingState...")
                }
                is LoginViewState.UserStatusSuccess->{
                    Log.d("MyTesting","UserStatusSuccess:-${state.userStatus}")
                }
                is LoginViewState.ErrorMessage->{
                    Log.d("MyTesting","ErrorMessage:-${state.errorMessage}")
                }

                else->{}
            }
        }
    }
}