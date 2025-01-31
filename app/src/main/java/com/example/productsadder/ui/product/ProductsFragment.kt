package com.example.productsadder.ui.product

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.productsadder.R
import com.example.productsadder.ui.product.view.ProductAdapter
import com.example.productsadder.application.ProductAdderApplication
import com.example.productsadder.model.Product
import com.example.productsadder.model.ProductState
import com.example.productsadder.databinding.FragmentProductsBinding
import com.example.productsadder.network.extension.getViewModelFromFactory
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.di.ViewModelFactory
import com.example.productsadder.ui.viewmodel.ProductViewModel
import com.example.productsadder.ui.viewmodel.ProductViewState
import javax.inject.Inject

class ProductsFragment : Fragment() {

    private lateinit var binding: FragmentProductsBinding
    private lateinit var productAdapter: ProductAdapter
    @Inject
    internal lateinit var productViewModelFactory: ViewModelFactory<ProductViewModel>
    lateinit var viewModel: ProductViewModel
    companion object {
        @JvmStatic
        fun newInstance() = ProductsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductsBinding.inflate(inflater, container, false)
        val view = binding.root

        ProductAdderApplication.component.inject(this)
        viewModel = getViewModelFromFactory(productViewModelFactory)

        listenToViewEvent()
        listenToViewModel()

        return view
    }

    private fun listenToViewModel() {
        viewModel.productState.subscribeAndObserveOnMainThread {
            when(it){
                is ProductViewState.LoadingState->{
                    binding.progressbar.isVisible=true
                }
                is ProductViewState.FetchProductSuccess->{
                    binding.progressbar.isVisible=false
                    productAdapter.products.clear()
                    productAdapter.products.addAll(it.fetchProducts)
                    productAdapter.notifyDataSetChanged()
                }
                is ProductViewState.ErrorMessage->{
                    binding.progressbar.isVisible=false
                }
                else->{}
            }
        }
    }

    private fun listenToViewEvent() {
        val recyclerView = binding.productRV
        recyclerView.layoutManager = LinearLayoutManager(context)

        productAdapter = ProductAdapter(mutableListOf())
        recyclerView.adapter = productAdapter

        productAdapter.apply {
            productClicks.subscribeAndObserveOnMainThread{ state ->
                when(state){
                    is ProductState.EditProductClick->{
                        requireContext().startActivity(EditProductActivity.getIntent(requireContext(), state.editProduct))
                    }
                    is ProductState.DeleteProductClick->{
                        deleteCategory(state.deleteProduct)
                    }
                }

            }
        }

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
        viewModel.fetchProducts()
    }
}