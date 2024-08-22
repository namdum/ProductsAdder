package com.example.productsadder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.productsadder.activity.AddProductActivity
import com.example.productsadder.adapter.ProductAdapter
import com.example.productsadder.databinding.FragmentProductsBinding
import com.example.productsadder.util.showBottomNavigationView
import com.example.productsadder.viewmodel.ProductViewModel
import com.example.productsadder.viewmodel.ProductViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProductsFragment : Fragment(R.layout.fragment_products) {
    private lateinit var binding: FragmentProductsBinding
    private lateinit var productAdapter: ProductAdapter
    private lateinit var viewModel: ProductViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductsBinding.inflate(inflater, container, false)
        val view = binding.root

        val viewModelFactory = ProductViewModelFactory(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
        viewModel = ViewModelProvider(this, viewModelFactory)[ProductViewModel::class.java]

        val recyclerView = binding.productRV
        recyclerView.layoutManager = LinearLayoutManager(context)

        productAdapter = ProductAdapter(mutableListOf())
        recyclerView.adapter = productAdapter

        viewModel.fetchProducts()

        lifecycleScope.launchWhenStarted {
            viewModel.products.collect { products ->
                productAdapter.products.clear()
                productAdapter.products.addAll(products)
                productAdapter.notifyDataSetChanged()
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.productFloatingButton.setOnClickListener {
            startActivity(Intent(requireContext(), AddProductActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        showBottomNavigationView()
    }
}