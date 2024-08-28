package com.example.productsadder.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.productsadder.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val firestore = FirebaseFirestore.getInstance()

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {

            firestore.collection("users")
                .whereEqualTo("email", auth?.currentUser?.email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    Log.i("test", querySnapshot.toString())

                    println("auth.currentUser?.email: " + auth.currentUser?.email)
                    firestore.collection("user").whereEqualTo("email", auth.currentUser?.email)
                        .get().addOnSuccessListener { querySnapshot ->
                            querySnapshot.documents.map { document ->
                                println("document.getString(\"user_type\"): " + document.getString("user_type"))
                                if(document.getString("user_type").toString().equals("admin")) {
                                    startActivity(Intent(this, HomeActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this@MainActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }.addOnFailureListener { exception ->
                            Toast.makeText(this@MainActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this@MainActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
        } else {
            Handler(Looper.getMainLooper()).postDelayed({

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }, 1000)
        }
    }
}