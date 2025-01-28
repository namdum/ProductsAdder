package com.example.productsadder

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
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

class CategoryFragment : Fragment(R.layout.fragment_category) {
    private lateinit var binding: FragmentCategoryBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var viewModel: CategoryViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoryBinding.inflate(inflater, container, false)
        val view = binding.root

        val viewModelFactory = CategoryViewModelFactory(FirebaseFirestore.getInstance())
        viewModel = ViewModelProvider(this, viewModelFactory)[CategoryViewModel::class.java]

        listenToViewEvent()
        listenToViewModel()

        return view
    }

    private fun listenToViewEvent() {
        val recyclerView = binding.categoryRV
        recyclerView.layoutManager = LinearLayoutManager(context)

        categoryAdapter = CategoryAdapter(mutableListOf())
        recyclerView.adapter = categoryAdapter

        categoryAdapter.apply {
            categoryClicks.subscribeAndObserveOnMainThread{ state ->
                when(state){
                    is CategoryState.EditCategoryClick->{
                        requireContext().startActivity(EditCategoryActivity.getIntent(requireContext(), state.editCategory))
                    }
                    is CategoryState.DeleteCategoryClick->{
                        deleteCategory(state.deleteCategory)
                    }
                }

            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.categoryFloatingButton.setOnClickListener {
            startActivity(Intent(requireContext(), AddCategoryActivity::class.java))
        }
    }
    private fun listenToViewModel() {
        viewModel.categoryState.subscribeAndObserveOnMainThread {
            when(it){
                is CategoryViewState.LoadingState->{
                    binding.progressbar.isVisible=true

                }
                is CategoryViewState.FetchCategorySuccess->{
                    categoryAdapter.categories.clear()
                    categoryAdapter.categories.addAll(it.fetchCategorys)
                    categoryAdapter.notifyDataSetChanged()
                    binding.progressbar.isVisible=false

                }
                is CategoryViewState.ErrorMessage->{
                    binding.progressbar.isVisible=false

                }
                else->{}
            }
        }
    }

    private fun deleteCategory(category: Category) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.delete_category))
            setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_category))
            setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteCategory(category)
            }
            setNegativeButton(getString(R.string.no)) { dialog, _ ->
                // Dismiss the dialog
                dialog.dismiss()
            }
            create()
            show()
        }
    }
    override fun onResume() {
        super.onResume()
        showBottomNavigationView()
        viewModel.fetchCategories()
    }
}
