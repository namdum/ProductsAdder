package com.example.productsadder

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.productsadder.activity.AddProductActivity
import com.example.productsadder.databinding.FragmentProductsBinding
import com.example.productsadder.util.showBottomNavigationView

class ProductsFragment : Fragment(R.layout.fragment_products) {
    private lateinit var binding: FragmentProductsBinding
   // private lateinit var productAdapter: ProductAdapter
   // private lateinit var viewModel: CategoryViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductsBinding.inflate(inflater, container, false)
        val view = binding.root

//        val viewModelFactory = CategoryViewModelFactory(
//            FirebaseFirestore.getInstance(),
//            FirebaseAuth.getInstance())
//        viewModel = ViewModelProvider(this, viewModelFactory)[CategoryViewModel::class.java]
//
//        val recyclerView = binding.productRV
//        recyclerView.layoutManager = LinearLayoutManager(context)
//
//        productAdapter = ProductAdapter(mutableListOf())
//        recyclerView.adapter = productAdapter
//
//        viewModel.fetchCategories()
//
//        lifecycleScope.launchWhenStarted {
//            viewModel.categories.collect { categories ->
//                productAdapter.categories.clear()
//                productAdapter.categories.addAll(categories)
//                productAdapter.notifyDataSetChanged()
//            }
//        }

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