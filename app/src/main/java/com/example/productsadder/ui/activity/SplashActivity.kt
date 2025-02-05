package com.example.productsadder.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.productsadder.application.ProductAdderApplication
import com.example.productsadder.databinding.ActivitySplashBinding
import com.example.productsadder.network.extension.getViewModelFromFactory
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.di.ViewModelFactory
import com.example.productsadder.ui.viewmodel.LoginViewModel
import com.example.productsadder.ui.viewmodel.LoginViewState
import timber.log.Timber
import javax.inject.Inject

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
@Inject
internal lateinit var loginViewModelFactory: ViewModelFactory<LoginViewModel>
    lateinit var viewModel: LoginViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ProductAdderApplication.component.inject(this)
        viewModel = getViewModelFromFactory(loginViewModelFactory)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        listenToViewModel()
        viewModel.checkUserStatus()

    }

    private fun listenToViewModel() {

        viewModel.loginState.subscribeAndObserveOnMainThread { state->
            when(state){
                is LoginViewState.LoadingState->{
                    Timber.d("LoadingState...")
                }
                is LoginViewState.UserStatusSuccess->{
                    if (state.userStatus) {
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
                is LoginViewState.ErrorMessage->{
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                else->{}
            }
        }
    }
}