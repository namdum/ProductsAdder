package com.example.productsadder.ui.category

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.productsadder.R
import com.example.productsadder.ui.category.view.CategoryAdapter
import com.example.productsadder.application.ProductAdderApplication
import com.example.productsadder.model.Category
import com.example.productsadder.model.CategoryState
import com.example.productsadder.databinding.FragmentCategoryBinding
import com.example.productsadder.network.extension.getViewModelFromFactory
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.di.ViewModelFactory
import com.example.productsadder.ui.viewmodel.CategoryViewModel
import com.example.productsadder.ui.viewmodel.CategoryViewState
import javax.inject.Inject

class CategoryFragment : Fragment() {
    private lateinit var binding: FragmentCategoryBinding
    private lateinit var categoryAdapter: CategoryAdapter

    @Inject
    internal lateinit var categoryViewModelFactory: ViewModelFactory<CategoryViewModel>
    lateinit var viewModel: CategoryViewModel
    companion object {
        @JvmStatic
        fun newInstance() = CategoryFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoryBinding.inflate(inflater, container, false)
        val view = binding.root

        ProductAdderApplication.component.inject(this)
        viewModel = getViewModelFromFactory(categoryViewModelFactory)

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
        viewModel.fetchCategories()
    }
}
