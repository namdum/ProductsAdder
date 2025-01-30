package com.example.productsadder.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.productsadder.CategoryFragment
import com.example.productsadder.OrderFragment
import com.example.productsadder.ProductsFragment

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