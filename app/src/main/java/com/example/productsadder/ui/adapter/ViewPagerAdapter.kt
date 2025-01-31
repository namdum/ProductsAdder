package com.example.productsadder.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.productsadder.ui.category.CategoryFragment
import com.example.productsadder.ui.order.OrderFragment
import com.example.productsadder.ui.product.ProductsFragment

class ViewPagerAdapter (activity: FragmentActivity) : FragmentStateAdapter(activity) {


    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                CategoryFragment.newInstance()
            }
            1 -> {
                ProductsFragment.newInstance()
            }
            2 -> {
                OrderFragment.newInstance()
            }
            else -> {
                CategoryFragment.newInstance()
            }
        }
    }
}