package com.example.productsadder.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.productsadder.R
import com.example.productsadder.adapter.ColorsAdapter
import com.example.productsadder.adapter.ImageAdapter
import com.example.productsadder.data.Category
import com.example.productsadder.data.Product
import com.example.productsadder.databinding.ActivityAddProductBinding
import com.example.productsadder.databinding.ActivityEditeProductBinding
import com.example.productsadder.util.Resource
import com.example.productsadder.viewmodel.CategoryViewModel
import com.example.productsadder.viewmodel.CategoryViewModelFactory
import com.example.productsadder.viewmodel.ProductViewModel
import com.example.productsadder.viewmodel.ProductViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.flow.collectLatest
import java.io.ByteArrayOutputStream
import java.util.UUID

class EditeProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditeProductBinding
    private lateinit var viewModel: ProductViewModel
    private lateinit var colorsAdapter: ColorsAdapter
    private lateinit var imageAdapter: ImageAdapter
    private var selectedColors: MutableList<Int> = mutableListOf()
    private var selectedImages: MutableList<Uri> = mutableListOf()
    private val uploadedImageUrls: MutableList<String> = mutableListOf()
    private var categories: List<String> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditeProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewModelFactory = ProductViewModelFactory(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
        viewModel = ViewModelProvider(this, viewModelFactory)[ProductViewModel::class.java]

        val product: Product? = intent.getParcelableExtra("product")

        imageAdapter = ImageAdapter(selectedImages)
        binding.rvImage.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvImage.adapter = imageAdapter

        colorsAdapter = ColorsAdapter()
        binding.rvColors.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvColors.adapter = colorsAdapter

        fetchCategories()

        binding.imageClose.setOnClickListener {
            finish()
        }

        binding.addImageImageView.setOnClickListener {
            val intent = Intent()
            intent.setType("image/*")
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            imagePickerLauncher.launch(intent)
            imageAdapter.updateImageUris(selectedImages)
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
        binding.productNameEditText.setText(product?.name)
        binding.categoryEditText.setSelection(categories.indexOf(product?.category?: ""))
        binding.productDescriptionEditText.setText(product?.description)
        binding.priceEditText.setText(product?.price.toString())
        binding.offerPercentageEditText.setText(product?.offerPercentage.toString())
        binding.sizeEditText.setText(product?.sizes.toString())

        product?.images?.let { uploadedImageUrls.addAll(it)
        Log.i("test","$it")}
        imageAdapter.updateImageString(uploadedImageUrls)

        product?.colors?.let { selectedColors.addAll(it) }
        colorsAdapter.updateColors(selectedColors)

        binding.saveAppCompatButton.setOnClickListener {
            binding.progressbarAddress.visibility = View.VISIBLE
            binding.saveAppCompatButton.visibility = View.GONE

            binding.apply {
                val name = productNameEditText.text.toString().trim()
                val description = productDescriptionEditText.text.toString().trim()
                val price = priceEditText.text.toString().trim().toFloatOrNull() ?: 0f
                val offerpercentage = offerPercentageEditText.text.toString().trim().toFloatOrNull() ?: 0f
                val size = sizeEditText.text.toString().trim().split(",").map { it.trim() }
                val selectedCategory = binding.categoryEditText.selectedItem.toString()

                val oldProduct = Product(
                    name = product?.name ?: "",
                    category = product?.category ?: "",
                    price = product?.price ?: 0f,
                    offerPercentage = product?.offerPercentage,
                    description = product?.description,
                    sizes = product?.sizes,
                    colors = product?.colors?.map { it.toInt() },
                    images = product?.images ?: emptyList()
                )
                val newProduct = Product(name, selectedCategory, price, offerpercentage, description, size, selectedColors, uploadedImageUrls)
                viewModel.editProduct(oldProduct,newProduct)
            }
        }

    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        selectedImages = imageAdapter.getImage().toMutableList()
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
            imageAdapter.imageUris = selectedImages
            imageAdapter.notifyDataSetChanged()
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
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
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