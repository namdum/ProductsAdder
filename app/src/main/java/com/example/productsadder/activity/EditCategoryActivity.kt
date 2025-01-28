package com.example.productsadder.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.productsadder.data.Category
import com.example.productsadder.databinding.ActivityEditCategoryBinding
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.util.Resource
import com.example.productsadder.viewmodel.CategoryViewModel
import com.example.productsadder.viewmodel.CategoryViewModelFactory
import com.example.productsadder.viewmodel.CategoryViewState
import com.example.productsadder.viewmodel.ProductViewState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.collectLatest
import java.io.ByteArrayOutputStream
import java.util.UUID

class EditCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditCategoryBinding
    private lateinit var viewModel: CategoryViewModel
    private lateinit var category: Category

    companion object {
        const val CATEGORY = "CATEGORY"
        fun getIntent(context: Context,category :Category): Intent {
            val intent = Intent(context, EditCategoryActivity::class.java)
            intent.putExtra(CATEGORY, category)
            return intent
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.category = intent?.getParcelableExtra(CATEGORY) ?: return
        

        val viewModelFactory = CategoryViewModelFactory(FirebaseFirestore.getInstance())
        viewModel = ViewModelProvider(this, viewModelFactory)[CategoryViewModel::class.java]

        listenToViewEvent()
        listenToViewModel()

    }

    private fun listenToViewModel() {

        viewModel.categoryState.subscribeAndObserveOnMainThread {
            when(it){
                is CategoryViewState.LoadingState->{
                    binding.progressbarAddress.visibility = View.VISIBLE
                    binding.saveAppCompatButton.visibility = View.INVISIBLE
                }
                is CategoryViewState.SuccessMessage->{
                    binding.progressbarAddress.visibility = View.INVISIBLE
                    binding.saveAppCompatButton.visibility = View.VISIBLE

                    Toast.makeText(this@EditCategoryActivity, it.successMessage, Toast.LENGTH_LONG).show()
                    finish()
                }
                is CategoryViewState.ErrorMessage->{
                    Toast.makeText(this@EditCategoryActivity, it.errorMessage, Toast.LENGTH_LONG).show()
                    binding.progressbarAddress.visibility = View.INVISIBLE
                    binding.saveAppCompatButton.visibility = View.VISIBLE


                }
                else->{}
            }
        }
    }

    private fun listenToViewEvent() {
        binding.imageClose.setOnClickListener {
            finish()
        }

        binding.imageAppCompatImageView.setOnClickListener{
            chooseFromGallery()
        }

        binding.categoryEditText.setText(category.category)
        Glide.with(this).load(category.image).into(binding.imageAppCompatImageView)

        binding.saveAppCompatButton.setOnClickListener {
            binding.progressbarAddress.visibility = View.VISIBLE
            binding.saveAppCompatButton.visibility = View.GONE
            val newCategoryName = binding.categoryEditText.text.toString()
            val bitmap = (binding.imageAppCompatImageView.drawable as BitmapDrawable).bitmap
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
                    val newCategoryImage = task.result.toString()
                    val oldCategory = Category(category.image, category.category)
                    val newCategory = Category(newCategoryImage,newCategoryName)
                    editCategory(oldCategory, newCategory)
                }
            }
        }
    }

    private fun editCategory(oldCategory: Category, newCategory: Category) {
        viewModel.editCategory(oldCategory, newCategory)
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