package com.example.productsadder.ui.category

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.productsadder.application.ProductAdderApplication
import com.example.productsadder.model.Category
import com.example.productsadder.databinding.ActivityAddCategoryBinding
import com.example.productsadder.network.extension.getViewModelFromFactory
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.di.ViewModelFactory
import com.example.productsadder.ui.viewmodel.CategoryViewModel
import com.example.productsadder.ui.viewmodel.CategoryViewState
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject

class AddCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddCategoryBinding
    @Inject
    internal lateinit var categoryViewModelFactory: ViewModelFactory<CategoryViewModel>
    lateinit var viewModel: CategoryViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ProductAdderApplication.component.inject(this)
        viewModel = getViewModelFromFactory(categoryViewModelFactory)

        listenToViewEvent()
            listenToViewModel()

    }
    private fun listenToViewEvent() {
        binding.apply {
            imageClose.setOnClickListener {
                finish()
            }
            imageAppCompatImageView.setOnClickListener {
                chooseFromGallery()
            }

            addAppCompatButton.setOnClickListener {
               progressbarAddress.visibility = View.VISIBLE
               addAppCompatButton.visibility = View.GONE
               apply {
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
        }


    }

    private fun listenToViewModel() {

        viewModel.categoryState.subscribeAndObserveOnMainThread {
            when(it){
                is CategoryViewState.LoadingState->{
                    binding.progressbarAddress.visibility = View.VISIBLE
                    binding.addAppCompatButton.visibility = View.INVISIBLE
                }
                is CategoryViewState.SuccessMessage->{
                    binding.progressbarAddress.visibility = View.INVISIBLE
                    binding.addAppCompatButton.visibility = View.VISIBLE

                    Toast.makeText(this@AddCategoryActivity, it.successMessage, Toast.LENGTH_LONG).show()
                    finish()
                }
                is CategoryViewState.ErrorMessage->{
                    Toast.makeText(this@AddCategoryActivity, it.errorMessage, Toast.LENGTH_LONG).show()
                    binding.progressbarAddress.visibility = View.INVISIBLE
                    binding.addAppCompatButton.visibility = View.VISIBLE

                }
                else->{}
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