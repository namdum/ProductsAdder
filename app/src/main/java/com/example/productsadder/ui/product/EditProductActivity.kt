package com.example.productsadder.ui.product

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.example.productsadder.databinding.ActivityEditeProductBinding
import com.example.productsadder.network.extension.getViewModelFromFactory
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.di.ViewModelFactory
import com.example.productsadder.ui.viewmodel.CategoryViewModel
import com.example.productsadder.ui.viewmodel.CategoryViewState
import com.example.productsadder.ui.viewmodel.ProductViewModel
import com.example.productsadder.ui.viewmodel.ProductViewState
import com.google.firebase.storage.FirebaseStorage
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import timber.log.Timber
import javax.inject.Inject

class EditProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditeProductBinding
    @Inject
    internal lateinit var productViewModelFactory: ViewModelFactory<ProductViewModel>
    lateinit var viewModel: ProductViewModel
    @Inject
    internal lateinit var categoryViewModelFactory: ViewModelFactory<CategoryViewModel>
    lateinit var categoryviewModel: CategoryViewModel

    private lateinit var colorsAdapter: ColorsAdapter
    private lateinit var imageAdapter: ImageAdapter
    private var selectedColors: MutableList<Int> = mutableListOf()
    private var selectedImages: MutableList<Uri> = mutableListOf()
    private var uploadedImageString: MutableList<String> = mutableListOf()
    private lateinit var product: Product
    private var selectedCategory:String=""

    companion object {
        const val PRODUCT = "PRODUCT"
        fun getIntent(context: Context, product : Product): Intent {
            val intent = Intent(context, EditProductActivity::class.java)
            intent.putExtra(PRODUCT, product)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditeProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ProductAdderApplication.component.inject(this)
        categoryviewModel = getViewModelFromFactory(categoryViewModelFactory)
        viewModel = getViewModelFromFactory(productViewModelFactory)

        categoryviewModel.fetchCategories()
        listenToViewEvent()
        listenToViewModel()

    }

    private fun listenToViewModel() {
        viewModel.productState.subscribeAndObserveOnMainThread {
            when(it){
                is ProductViewState.LoadingState->{
                    binding.progressbarAddress.visibility = View.VISIBLE
                    binding.saveAppCompatButton.visibility = View.INVISIBLE
                }
                is ProductViewState.SuccessMessage->{
                    binding.progressbarAddress.visibility = View.INVISIBLE
                    binding.saveAppCompatButton.visibility = View.VISIBLE

                    Toast.makeText(this@EditProductActivity, it.successMessage, Toast.LENGTH_LONG).show()
                    finish()
                }
                is ProductViewState.ErrorMessage->{
                    Toast.makeText(this@EditProductActivity, it.errorMessage, Toast.LENGTH_LONG).show()
                    binding.progressbarAddress.visibility = View.INVISIBLE
                    binding.saveAppCompatButton.visibility = View.VISIBLE


                }
                else->{}
            }
        }
        categoryviewModel.categoryState.subscribeAndObserveOnMainThread {
            when(it){
                is CategoryViewState.LoadingState->{}
                is CategoryViewState.SuccessMessage->{}
                is CategoryViewState.FetchCategorySuccess->{
                    val categories =it.fetchCategorys
                    val categoryNames = categories.map { category -> category.category }
                    populateSpinner(categoryNames)
                }
                is CategoryViewState.ErrorMessage->{
                    Toast.makeText(this@EditProductActivity, it.errorMessage, Toast.LENGTH_LONG).show()
                }

            }
        }
    }

    private fun listenToViewEvent() {

        this.product = intent?.getParcelableExtra(PRODUCT) ?: return



        uploadedImageString.addAll(product?.images ?: mutableListOf())
        imageAdapter = ImageAdapter(uploadedImageString)

        binding.apply {
            rvImage.layoutManager = LinearLayoutManager(this@EditProductActivity, LinearLayoutManager.HORIZONTAL, false)
            rvImage.adapter = imageAdapter

            colorsAdapter = ColorsAdapter()
            rvColors.layoutManager = LinearLayoutManager(this@EditProductActivity, LinearLayoutManager.HORIZONTAL, false)
            rvColors.adapter = colorsAdapter

            addImageImageView.setOnClickListener {
                val intent = Intent()
                intent.setType("image/*")
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.action = Intent.ACTION_GET_CONTENT
                imagePickerLauncher.launch(intent)
            }
           addColorImageView.setOnClickListener {
                ColorPickerDialog
                    .Builder(this@EditProductActivity)
                    .setTitle("Product color")
                    .setPositiveButton("Select", object : ColorEnvelopeListener {

                        override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                            envelope?.let {
                                val color = it.color

                                selectedColors = colorsAdapter.getColor().toMutableList()
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

           productNameEditText.setText(product?.name)
           productDescriptionEditText.setText(product?.description)
           priceEditText.setText(product?.price.toString())
           offerPercentageEditText.setText(product?.offerPercentage.toString())
           sizeEditText.setText(product?.sizes?.joinToString(", ").toString())

            saveAppCompatButton.setOnClickListener {
                if (categoryEditText.selectedItem.equals("Select Category")){
                    Toast.makeText(this@EditProductActivity,"Select Category",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                    val name = productNameEditText.text.toString().trim()
                    val description = productDescriptionEditText.text.toString().trim()
                    val price = priceEditText.text.toString().trim().toFloatOrNull() ?: 0f
                    val offerpercentage = offerPercentageEditText.text.toString().trim().toFloatOrNull() ?: 0f
                    val size = sizeEditText.text.toString().trim().split(",").map { it.trim() }
                    selectedCategory = categoryEditText.selectedItem.toString()

                    val oldProduct = Product(
                        name=product.name ,
                        category = product.category,
                        price = product.price ,
                        offerPercentage = product?.offerPercentage,
                        description = product?.description,
                        sizes = product?.sizes,
                        colors = product?.colors?.map { it },
                        images = product.images
                    )

                    val newProduct = Product(name, selectedCategory, price, offerpercentage, description, size, selectedColors, uploadedImageString)
                    viewModel.editProduct(oldProduct,newProduct)

                }

            imageClose.setOnClickListener {
                finish()
            }
        }

        imageAdapter.notifyDataSetChanged()

        product?.colors?.let { selectedColors.addAll(it) }
        colorsAdapter.updateColors(selectedColors)


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
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        binding.categoryEditText.adapter = spinnerAdapter

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val categoryIndex = categories.indexOf(product?.category ?: "")

        binding.categoryEditText.setSelection(categoryIndex)
        binding.categoryEditText.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedCategory = categories[position]
                Timber.e("selectedCategory:${selectedCategory}")
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

}