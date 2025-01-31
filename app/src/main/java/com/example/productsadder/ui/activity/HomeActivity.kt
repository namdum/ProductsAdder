package com.example.productsadder.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.productsadder.R
import com.example.productsadder.databinding.ActivityHomeBinding
import com.example.productsadder.ui.adapter.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var mainHomeTabAdapter: ViewPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
        listenToViewEvent()
    }

    private fun initUI() {
        mainHomeTabAdapter = ViewPagerAdapter(this)
        setupViewPager()
        setupBottomNavigation()
    }

    private fun listenToViewEvent() {
        binding.logoutButton.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle(getString(R.string.sign_out))
                setMessage(getString(R.string.are_you_sure_you_want_to_sign_out))
                setPositiveButton(getString(R.string.yes)) { _, _ ->
                    // Sign out the user
                    FirebaseAuth.getInstance().signOut()
                    finishAffinity()
                    startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                }
                setNegativeButton(getString(R.string.no)) { dialog, _ ->
                    // Dismiss the dialog
                    dialog.dismiss()
                }
                create()
                show()
            }
        }
    }

    private fun setupViewPager() {
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.adapter = mainHomeTabAdapter

        handleTabSelection(0)

    }

    private fun setupBottomNavigation() {
        binding.bottomTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position
                handleTabSelection(position)

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Called when a tab exits the selected state

            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Called when a tab that is already selected is chosen again
            }
        })
    }

    private fun handleTabSelection(tabPosition: Int) {
        when (tabPosition) {
            0 -> {
                binding.viewPager.setCurrentItem(0, false)
                binding.headerAppLogo.setText(resources.getText(R.string.category))
            }

            1 -> {
                binding.viewPager.setCurrentItem(1, false)
                binding.headerAppLogo.setText(resources.getText(R.string.product))
            }

            2 -> {
                binding.viewPager.setCurrentItem(2, false)
                binding.headerAppLogo.setText(resources.getText(R.string.order_list))
            }
        }
    }
}