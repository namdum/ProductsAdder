package com.example.productsadder.ui.product

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.productsadder.ui.adapter.ColorsAdapter
import com.example.productsadder.ui.adapter.ImageAdapter
import com.example.productsadder.application.ProductAdderApplication
import com.example.productsadder.model.Product
import com.example.productsadder.databinding.ActivityAddProductBinding
import com.example.productsadder.network.extension.getViewModelFromFactory
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.di.ViewModelFactory
import com.example.productsadder.model.CategoryState
import com.example.productsadder.ui.viewmodel.CategoryViewModel
import com.example.productsadder.ui.viewmodel.CategoryViewState
import com.example.productsadder.ui.viewmodel.ProductViewModel
import com.example.productsadder.ui.viewmodel.ProductViewState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import javax.inject.Inject
import timber.log.Timber

class AddProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProductBinding
    @Inject
    internal lateinit var productViewModelFactory: ViewModelFactory<ProductViewModel>
    lateinit var viewModel: ProductViewModel
    private lateinit var colorsAdapter: ColorsAdapter
    private lateinit var imageAdapter: ImageAdapter
    private var selectedColors: MutableList<Int> = mutableListOf()
    private var selectedImages: MutableList<Uri> = mutableListOf()
    private var uploadedImageString: MutableList<String> = mutableListOf()

    @Inject
    internal lateinit var categoryViewModelFactory: ViewModelFactory<CategoryViewModel>
    lateinit var categoryViewModel: CategoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ProductAdderApplication.component.inject(this)
        viewModel = getViewModelFromFactory(productViewModelFactory)
        categoryViewModel = getViewModelFromFactory(categoryViewModelFactory)

        categoryViewModel.fetchCategories()
        listenToViewEvent()
        listenToViewModel()
    }

    private fun listenToViewEvent() {

        binding.apply {
            imageAdapter = ImageAdapter(uploadedImageString)
            rvImage.layoutManager = LinearLayoutManager(this@AddProductActivity, LinearLayoutManager.HORIZONTAL, false)
            rvImage.adapter = imageAdapter

            colorsAdapter = ColorsAdapter()
            rvColors.layoutManager = LinearLayoutManager(this@AddProductActivity, LinearLayoutManager.HORIZONTAL, false)
            rvColors.adapter = colorsAdapter
            addAppCompatButton.setOnClickListener {
                if (categoryEditText.selectedItem.equals("Select Category")){
                    Toast.makeText(this@AddProductActivity,"Select Category",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                    val name = productNameEditText.text.toString().trim()
                    val description = productDescriptionEditText.text.toString().trim()
                    val price = priceEditText.text.toString().trim().toFloatOrNull() ?: 0f
                    val offerpercentage = offerPercentageEditText.text.toString().trim().toFloatOrNull() ?: 0f
                    val size = sizeEditText.text.toString().trim().split(",").map { it.trim() }
                    val selectedCategory = categoryEditText.selectedItem.toString()

                    val product = Product(name, selectedCategory, price, offerpercentage, description, size ?: mutableListOf(), selectedColors, uploadedImageString)
                    viewModel.addProduct(product)
            }
            addColorImageView.setOnClickListener {
                ColorPickerDialog
                    .Builder(this@AddProductActivity)
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
            addImageImageView.setOnClickListener {
                val intent = Intent()
                intent.setType("image/*")
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.action = Intent.ACTION_GET_CONTENT
                imagePickerLauncher.launch(intent)
            }
            imageClose.setOnClickListener {
                finish()
            }
        }
    }
    private fun listenToViewModel() {
        viewModel.productState.subscribeAndObserveOnMainThread {
            when(it){
                is ProductViewState.LoadingState->{
                    binding.progressbarAddress.visibility = View.VISIBLE
                    binding.addAppCompatButton.visibility = View.GONE
                }
                is ProductViewState.SuccessMessage->{
                    binding.progressbarAddress.visibility = View.INVISIBLE
                    binding.addAppCompatButton.visibility = View.VISIBLE
                    Toast.makeText(this@AddProductActivity, it.successMessage, Toast.LENGTH_LONG).show()
                    finish()
                }
                is ProductViewState.ErrorMessage->{
                    Toast.makeText(this@AddProductActivity,it.errorMessage,Toast.LENGTH_SHORT).show()
                    binding.progressbarAddress.visibility = View.INVISIBLE
                    binding.addAppCompatButton.visibility = View.VISIBLE
                }
                else->{}
            }
        }
        categoryViewModel.categoryState.subscribeAndObserveOnMainThread {
            when(it){
                is CategoryViewState.LoadingState->{}
                is CategoryViewState.FetchCategorySuccess->{
                    val categories =it.fetchCategorys
                    val categoryNames = categories.map { category -> category.category }
                    populateSpinner(categoryNames)
                }
                is CategoryViewState.ErrorMessage->{
                    Toast.makeText(this@AddProductActivity, it.errorMessage, Toast.LENGTH_LONG).show()
                }
                else->{}
            }
        }
    }
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        uploadedImageString = imageAdapter.getImage().toMutableList()
        if (result.resultCode == Activity.RESULT_OK) {
            binding.progressbar.visibility = View.VISIBLE
            val imageUris = result.data?.clipData?.itemCount?.let { itemCount ->
                (0 until itemCount).map { index ->
                    result.data?.clipData?.getItemAt(index)?.uri
                }
            } ?: listOf(result.data?.data)

            imageUris.forEach { imageUri ->
                selectedImages.add(imageUri!!)
                Log.i("test","${selectedImages}")
                Timber.e("test:${selectedImages}")
                uploadImageToFirebaseStorage(imageUri)
            }
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
                    binding.progressbar.visibility = View.GONE
                }
            }
        }
    }


    private fun populateSpinner(categories: List<String>) {
        val updatedCategories = mutableListOf("Select Category").apply { addAll(categories) }

        val spinnerAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, updatedCategories)
        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        binding.categoryEditText.adapter = spinnerAdapter

        binding.categoryEditText.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedCategory = updatedCategories[position]
                if (position != 0) { // Avoid logging "Select Category"
                    Timber.d("$selectedCategory")
                    val selectedCategory = categories[position]
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

}