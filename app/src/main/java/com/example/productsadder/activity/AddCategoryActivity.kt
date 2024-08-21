package com.example.productsadder.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.productsadder.CategoryFragment
import com.example.productsadder.data.Category
import com.example.productsadder.databinding.ActivityAddCategoryBinding
import com.example.productsadder.util.Resource
import com.example.productsadder.viewmodel.CategoryViewModel
import com.example.productsadder.viewmodel.CategoryViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.collectLatest
import java.io.ByteArrayOutputStream
import java.util.UUID

class AddCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddCategoryBinding
    private lateinit var viewModel: CategoryViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory = CategoryViewModelFactory(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
        viewModel = ViewModelProvider(this, viewModelFactory)[CategoryViewModel::class.java]

        binding.imageClose.setOnClickListener {
            finish()
            //startActivity(Intent(this@AddCategoryActivity, CategoryFragment::class.java))
        }

        binding.imageAppCompatImageView.setOnClickListener {
            chooseFromGallery()
        }

        binding.addAppCompatButton.setOnClickListener {
            binding.progressbarAddress.visibility = View.VISIBLE
            binding.addAppCompatButton.visibility = View.GONE
            binding.apply {
                val category = categoryEditText.text.toString().trim()

                if (imageAppCompatImageView.drawable != null) {
                    val bitmap = (imageAppCompatImageView.drawable as BitmapDrawable).bitmap
                    val storageRef = FirebaseStorage.getInstance().reference
                    val imageRef = storageRef.child("images/${UUID.randomUUID()}.jpg")
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()

                    val uploadTask = imageRef.putBytes(data)

                    uploadTask.continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        imageRef.downloadUrl
                    }.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downloadUrl = task.result
                            val address = Category(downloadUrl.toString(), category)
                            viewModel.addCategory(address)
                        }
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.addNewCategory.collectLatest {
                when (it) {
                    is Resource.Loading -> {}

                    is Resource.Success -> {
                        binding.progressbarAddress.visibility = View.INVISIBLE
                        Toast.makeText(this@AddCategoryActivity, "Add Category", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@AddCategoryActivity, CategoryFragment::class.java))
                    }

                    is Resource.Error -> {
                        Toast.makeText(this@AddCategoryActivity, it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.error.collectLatest {
                Toast.makeText(this@AddCategoryActivity, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun chooseFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 100)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                100 -> {
                    val selectedImageUri = data?.data
                    binding.imageAppCompatImageView.setImageURI(selectedImageUri)
                }
                101 -> {
                    val thumbnail = data?.extras?.get("data") as Bitmap
                    binding.imageAppCompatImageView.setImageBitmap(thumbnail)
                }
            }
        }
    }
}