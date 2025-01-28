package com.example.productsadder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.productsadder.activity.AddProductActivity
import com.example.productsadder.adapter.ProductAdapter
import com.example.productsadder.data.Category
import com.example.productsadder.data.Product
import com.example.productsadder.databinding.FragmentProductsBinding
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.util.showBottomNavigationView
import com.example.productsadder.viewmodel.ProductViewModel
import com.example.productsadder.viewmodel.ProductViewModelFactory
import com.example.productsadder.viewmodel.ProductViewState
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

        val viewModelFactory = ProductViewModelFactory(FirebaseFirestore.getInstance())
        viewModel = ViewModelProvider(this, viewModelFactory)[ProductViewModel::class.java]

        val recyclerView = binding.productRV
        recyclerView.layoutManager = LinearLayoutManager(context)

        productAdapter = ProductAdapter(mutableListOf())
        recyclerView.adapter = productAdapter

            viewModel.productState.subscribeAndObserveOnMainThread {
                when(it){
                    is ProductViewState.LoadingState->{}
                    is ProductViewState.FetchProductSuccess->{
                        productAdapter.products.clear()
                        productAdapter.products.addAll(it.fetchProducts)
                        productAdapter.notifyDataSetChanged()
                        Log.d("MyTesting","FetchProductSuccess:->${it.fetchProducts}")
                    }
                    is ProductViewState.ErrorMessage->{
                        Log.d("MyTesting","error:->${it.errorMessage}")
                    }
                    else->{}
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
    private fun deleteCategory(product: Product) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.delete_product))
            setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_product))
            setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteProduct(product)
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
        viewModel.fetchProducts()
    }
}