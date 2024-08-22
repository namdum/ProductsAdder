package com.example.productsadder.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.productsadder.ProductsFragment
import com.example.productsadder.adapter.ColorsAdapter
import com.example.productsadder.data.Product
import com.example.productsadder.databinding.ActivityAddProductBinding
import com.example.productsadder.util.Resource
import com.example.productsadder.viewmodel.ProductViewModel
import com.example.productsadder.viewmodel.ProductViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.flow.collectLatest
import java.util.UUID

class AddProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProductBinding
    private lateinit var viewModel: ProductViewModel
    private lateinit var colorsAdapter: ColorsAdapter
    private val selectedColors: MutableList<Int> = mutableListOf()
    private val selectedImages: MutableList<Uri> = mutableListOf()
    private val uploadedImageUrls: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory = ProductViewModelFactory(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
        viewModel = ViewModelProvider(this, viewModelFactory)[ProductViewModel::class.java]

        binding.imageClose.setOnClickListener {
            finish()
        }

        binding.addImageImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            imagePickerLauncher.launch(intent)
        }

        colorsAdapter = ColorsAdapter()
        binding.rvColors.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvColors.adapter = colorsAdapter

        binding.addColorImageView.setOnClickListener {
            ColorPickerDialog
                .Builder(this)
                .setTitle("Product color")
                .setPositiveButton("Select", object : ColorEnvelopeListener {

                    override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                        envelope?.let {
                            val color = it.color
                            if (selectedColors.contains(color)) {
                                selectedColors.remove(color)
                            } else {
                                selectedColors.add(color)
                            }
                            colorsAdapter.updateColors(selectedColors)
                            colorsAdapter.notifyDataSetChanged()
                        }
                    }

                }).setNegativeButton("Cancel") { colorPicker, _ ->
                    colorPicker.dismiss()
                }.show()
        }

        binding.addAppCompatButton.setOnClickListener {
            binding.progressbarAddress.visibility = View.VISIBLE
            binding.apply {
                val name = productNameEditText.text.toString().trim()
                val category = categoryEditText.text.toString().trim()
                val description = productDescriptionEditText.text.toString().trim()
                val price = priceEditText.text.toString().trim().toFloatOrNull() ?: 0f
                val offerpercentage = offerPercentageEditText.text.toString().trim().toFloatOrNull() ?: 0f
                val size = sizeEditText.text.toString().trim().split(",").map { it.trim() }

                val product = Product(name, category, price, offerpercentage, description, size, selectedColors, uploadedImageUrls)
                viewModel.addProduct(product)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.addNewProduct.collectLatest {
                when (it) {
                    is Resource.Loading -> {}

                    is Resource.Success -> {
                        binding.progressbarAddress.visibility = View.INVISIBLE
                        Toast.makeText(this@AddProductActivity, "Add Product", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@AddProductActivity, ProductsFragment::class.java))
                    }

                    is Resource.Error -> {
                        Toast.makeText(this@AddProductActivity, it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.error.collectLatest {
                Toast.makeText(this@AddProductActivity, it, Toast.LENGTH_SHORT).show()
            }
        }
    }


    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUris = result.data?.clipData?.itemCount?.let { itemCount ->
                (0 until itemCount).map { index ->
                    result.data?.clipData?.getItemAt(index)?.uri
                }
            } ?: listOf(result.data?.data)

            imageUris.forEach { imageUri ->
                selectedImages.add(imageUri!!)
                uploadImageToFirebaseStorage(imageUri)
            }
            binding.imageTextView.text = "${selectedImages.size}"
        }
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${UUID.randomUUID()}.jpg")
        val uploadTask = imageRef.putFile(imageUri)

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
                uploadedImageUrls.add(downloadUrl.toString())
            }
        }
    }

}