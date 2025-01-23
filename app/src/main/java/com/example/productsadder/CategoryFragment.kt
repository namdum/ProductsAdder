package com.example.productsadder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.productsadder.activity.AddCategoryActivity
import com.example.productsadder.activity.EditCategoryActivity
import com.example.productsadder.adapter.CategoryAdapter
import com.example.productsadder.data.Category
import com.example.productsadder.data.CategoryState
import com.example.productsadder.databinding.FragmentCategoryBinding
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.util.showBottomNavigationView
import com.example.productsadder.viewmodel.CategoryViewModel
import com.example.productsadder.viewmodel.CategoryViewModelFactory
import com.example.productsadder.viewmodel.CategoryViewState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.internal.notify
import okhttp3.internal.notifyAll

class CategoryFragment : Fragment(R.layout.fragment_category) {
    private lateinit var binding: FragmentCategoryBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var viewModel: CategoryViewModel
    val categoryList:ArrayList<Category> = arrayListOf()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoryBinding.inflate(inflater, container, false)
        val view = binding.root

        val viewModelFactory = CategoryViewModelFactory(FirebaseFirestore.getInstance(),FirebaseAuth.getInstance())
        viewModel = ViewModelProvider(this, viewModelFactory)[CategoryViewModel::class.java]

        val recyclerView = binding.categoryRV
        recyclerView.layoutManager = LinearLayoutManager(context)

        categoryAdapter = CategoryAdapter(requireContext())
        recyclerView.adapter = categoryAdapter

        listenToViewModel()

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.categoryFloatingButton.setOnClickListener {
            startActivity(Intent(requireContext(), AddCategoryActivity::class.java))
        }

        categoryAdapter.categoryItemClicks.subscribeAndObserveOnMainThread { state ->
            when(state){
                is CategoryState.CategoryDeleteClick->{
                    deleteCategory(state.category)
                }
                is CategoryState.CategoryEditClick->{
                    val intent = Intent(context, EditCategoryActivity::class.java)
                    intent.putExtra("category_name", state.category.category)
                    intent.putExtra("category_image", state.category.image)
                    startActivity(intent)
                }
            }

        }

    }

    private fun listenToViewModel() {
        viewModel.categoryState.subscribeAndObserveOnMainThread { viewState ->
            when (viewState) {
                is CategoryViewState.LoadingState -> {
                }
                is CategoryViewState.SuccessMessage -> {
                    Toast.makeText(requireContext(), viewState.successMessage, Toast.LENGTH_SHORT).show()


                }
                is CategoryViewState.FetchCategoriesSuccess -> {
                    categoryList.clear()
                    categoryList.addAll(viewState.fetchCategories)
                    categoryAdapter.categories = categoryList
                }
                is CategoryViewState.ErrorMessage -> {
                    Toast.makeText(requireContext(), viewState.errorMessage, Toast.LENGTH_SHORT).show()
                }
                is CategoryViewState.CategoryDeleted -> {

//                    categoryList.forEach { item ->
//                        if(item.category.equals(viewState.category.category)){
//                            val index = categoryList.indexOf(viewState.category)
//                            if(index != -1){
//                                categoryList.remove(viewState.category)
//                                categoryAdapter.categories = categoryList
//                            }
//                        }
//                    }


                }
            }
        }
    }

    private fun deleteCategory(category: Category) {
        val alertDialog = android.app.AlertDialog.Builder(context)

        alertDialog.setTitle("Delete Category")
        alertDialog.setMessage("Are you sure you want to delete this category?")

        alertDialog.setPositiveButton("Yes") { _, _ ->
                    viewModel.deleteCategory(category = category)
        }

        alertDialog.setNegativeButton("No") { _, _ -> }

        alertDialog.show()
    }

    override fun onResume() {
        super.onResume()
        showBottomNavigationView()
        viewModel.fetchCategories()
    }
}
