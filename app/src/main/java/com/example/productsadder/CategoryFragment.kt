package com.example.productsadder

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.productsadder.adapter.CategoryAdapter
import com.example.productsadder.databinding.FragmentCategoryBinding
import com.example.productsadder.util.showBottomNavigationView
import com.example.productsadder.viewmodel.CategoryViewModel
import com.example.productsadder.viewmodel.CategoryViewModelFactory
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

        val viewModelFactory = CategoryViewModelFactory(FirebaseFirestore.getInstance(),FirebaseAuth.getInstance())
        viewModel = ViewModelProvider(this, viewModelFactory)[CategoryViewModel::class.java]

        val recyclerView = binding.categoryRV
        recyclerView.layoutManager = LinearLayoutManager(context)

        categoryAdapter = CategoryAdapter(mutableListOf())
        recyclerView.adapter = categoryAdapter

        viewModel.fetchCategories()

        lifecycleScope.launchWhenStarted {
            viewModel.categories.collect { categories ->
                categoryAdapter.categories.clear()
                categoryAdapter.categories.addAll(categories)
                categoryAdapter.notifyDataSetChanged()
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.floatingButton.setOnClickListener {
            val navController = Navigation.findNavController(binding.root)
            navController.navigate(R.id.action_FragmentCategory_to_FragmentAddCategory)
        }
    }

    override fun onResume() {
        super.onResume()
        showBottomNavigationView()
    }
}