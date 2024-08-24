package com.example.productsadder.activity

import android.R
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.productsadder.ProductsFragment
import com.example.productsadder.adapter.ColorsAdapter
import com.example.productsadder.adapter.ImageAdapter
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
import kotlinx.coroutines.launch
import java.util.UUID

class AddProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProductBinding
    private lateinit var viewModel: ProductViewModel
    private lateinit var colorsAdapter: ColorsAdapter
    private lateinit var imageAdapter: ImageAdapter
    private var selectedColors: MutableList<Int> = mutableListOf()
    private var selectedImages: MutableList<Uri> = mutableListOf()
    private var uploadedImageString: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory = ProductViewModelFactory(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
        viewModel = ViewModelProvider(this, viewModelFactory)[ProductViewModel::class.java]

        imageAdapter = ImageAdapter(uploadedImageString)
        binding.rvImage.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvImage.adapter = imageAdapter

        colorsAdapter = ColorsAdapter()
        binding.rvColors.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvColors.adapter = colorsAdapter

        binding.imageClose.setOnClickListener {
            finish()
        }

        fetchCategories()

        binding.addImageImageView.setOnClickListener {
            val intent = Intent()
            intent.setType("image/*")
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            imagePickerLauncher.launch(intent)
        }

        binding.addColorImageView.setOnClickListener {
            ColorPickerDialog
                .Builder(this)
                .setTitle("Product color")
                .setPositiveButton("Select", object : ColorEnvelopeListener {

                    override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                        envelope?.let {
                            val color = it.color

                            selectedColors = colorsAdapter.getColor().toMutableList()
                            if (selectedColors.contains(color)) {
                                selectedColors.remove(color)
                                Log.i("test","${selectedColors}")
                            } else {
                                selectedColors.add(color)
                                Log.i("test","${selectedColors}")
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
            binding.addAppCompatButton.visibility = View.GONE
            binding.apply {
                println("uploadedImageString: " + uploadedImageString)
                val name = productNameEditText.text.toString().trim()
                val description = productDescriptionEditText.text.toString().trim()
                val price = priceEditText.text.toString().trim().toFloatOrNull() ?: 0f
                val offerpercentage = offerPercentageEditText.text.toString().trim().toFloatOrNull() ?: 0f
                val size = sizeEditText.text.toString().trim().split(",").map { it.trim() }
                val selectedCategory = binding.categoryEditText.selectedItem.toString()

                val product = Product(name, selectedCategory, price, offerpercentage, description, size, selectedColors, uploadedImageString)
                viewModel.addProduct(product)
            }
        }

        lifecycleScope.launch {
            viewModel.addNewProduct.collectLatest {
                when (it) {
                    is Resource.Loading -> {}

                    is Resource.Success -> {
                        binding.progressbarAddress.visibility = View.INVISIBLE
                        Toast.makeText(this@AddProductActivity, "Add Product", Toast.LENGTH_LONG).show()
                        finish()
                    }

                    is Resource.Error -> {
                        Toast.makeText(this@AddProductActivity, it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }

        lifecycleScope.launch {
            viewModel.error.collectLatest {
                Toast.makeText(this@AddProductActivity, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        uploadedImageString = imageAdapter.getImage().toMutableList()
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUris = result.data?.clipData?.itemCount?.let { itemCount ->
                (0 until itemCount).map { index ->
                    result.data?.clipData?.getItemAt(index)?.uri
                }
            } ?: listOf(result.data?.data)

            imageUris.forEach { imageUri ->
                selectedImages.add(imageUri!!)
                Log.i("test","${selectedImages}")
                uploadImageToFirebaseStorage(imageUri)
            }
//            imageAdapter.imageUris = selectedImages
           // imageAdapter.updateImageUris(selectedImages)
//            imageAdapter.updateImageString(uploadedImageString)
//            imageAdapter.notifyDataSetChanged()

            println("UploadedImage: " + uploadedImageString.toString())
        }
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${imageUri.lastPathSegment}.jpg")
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

                runOnUiThread {
                    uploadedImageString.add(downloadUrl.toString())
                    imageAdapter.updateImageString(uploadedImageString)
                    imageAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun fetchCategories() {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("Category")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val categories = querySnapshot.documents.map { document ->
                    document.getString("category") ?: ""
                }

                populateSpinner(categories)
            }
            .addOnFailureListener { exception ->
                Log.e("Error", "Error fetching categories: $exception")
            }
    }

    private fun populateSpinner(categories: List<String>) {
        val spinnerAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, categories)
        binding.categoryEditText.adapter = spinnerAdapter
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.categoryEditText.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedCategory = categories[position]
                Log.d("Selected Category", selectedCategory)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

}