package com.example.productsadder.util

import android.view.View
import androidx.fragment.app.Fragment
import com.example.productsadder.HomeActivity
import com.example.productsadder.R
import com.google.android.material.bottomnavigation.BottomNavigationView


fun Fragment.hideBottomNavigationView(){
    val bottomNavigationView = (activity as HomeActivity).findViewById<BottomNavigationView>(R.id.bottomNavigation)
    bottomNavigationView.visibility = View.GONE
}

fun Fragment.showBottomNavigationView(){
    val bottomNavigationView = (activity as HomeActivity).findViewById<BottomNavigationView>(R.id.bottomNavigation)
    bottomNavigationView.visibility = View.VISIBLE
}