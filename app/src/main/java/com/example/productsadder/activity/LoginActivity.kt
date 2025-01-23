package com.example.productsadder.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val firestore = FirebaseFirestore.getInstance()

        val viewModelFactory = LoginViewModelFactory(FirebaseAuth.getInstance(),FirebaseFirestore.getInstance())
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]

        auth = FirebaseAuth.getInstance()
        db= FirebaseFirestore.getInstance()
            binding.loginEmailEdit.setText("")
         binding.loginPasswordEdit.setText("")

//        if (auth.currentUser != null) {
//
//            firestore.collection("users")
//                .whereEqualTo("email", auth?.currentUser?.email)
//                .get()
//                .addOnSuccessListener { querySnapshot ->
//                    Log.i("test", querySnapshot.toString())
//
//                    firestore.collection("user").whereEqualTo("email", auth.currentUser?.email)
//                        .get().addOnSuccessListener { querySnapshot ->
//                            querySnapshot.documents.map { document ->
//                                if(document.getString("user_type").toString().equals("admin")) {
//                                    startActivity(Intent(this, HomeActivity::class.java))
//                                    finish()
//                                } else {
//                                    Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
//                                }
//                            }
//                        }.addOnFailureListener { exception ->
//                            Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
//                        }
//                }
//                .addOnFailureListener { exception ->
//                    Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
//                }
//        }
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

        viewModel.loginResult.observe(this, Observer { result ->
            when (result) {
                is Resource.Success -> {
                    binding.progressbar.isVisible=false
                    binding.loginLoginBtn.isVisible=true
                    Toast.makeText(this, result.data, Toast.LENGTH_SHORT).show()
                    Log.d("MyTesting","result.data${result.data}")
                    startActivity(Intent(this, HomeActivity::class.java))

                }
                is Resource.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                    binding.progressbar.isVisible=false
                    binding.loginLoginBtn.isVisible=true
                }
                is Resource.Loading -> {
                    // Show progress dialog
                    binding.progressbar.isVisible=true
                    binding.loginLoginBtn.isVisible=false
                }

                else -> {}
            }
        })

        binding.dontHaveAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        binding.loginEmailEdit.setText("")
        binding.loginPasswordEdit.setText("")

    }
}