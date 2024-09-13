package com.example.productsadder.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meangene.notification.NotificationRepository
import com.example.meangene.notification.NotificationViewModelFactory
import com.example.meangene.notification.model.NotificationInfo
import com.example.meangene.notification.model.NotificationViewModel
import com.example.productsadder.OrderFragment
import com.example.productsadder.R
import com.example.productsadder.adapter.OrderDetailAdapter
import com.example.productsadder.adapter.OrderListAdapter
import com.example.productsadder.adapter.ProductAdapter
import com.example.productsadder.adapter.StatusSpinnerAdapter
import com.example.productsadder.data.Order
import com.example.productsadder.data.Product
import com.example.productsadder.databinding.ActivityEditeProductBinding
import com.example.productsadder.databinding.ActivityOrderDetailsBinding
import com.example.productsadder.util.OrderStatus
import com.example.productsadder.util.Resource
import com.example.productsadder.util.VerticalItemDecoration
import com.example.productsadder.viewmodel.OrderViewModel
import com.example.productsadder.viewmodel.StatusSpinnerSetup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject

class OrderDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderDetailsBinding
    private lateinit var orderDetailAdapter: OrderDetailAdapter
    private var selectedOrderStatus = ""
    private var order: Order? = null
    private lateinit var auth: FirebaseAuth
    private val notificationViewModel: NotificationViewModel by viewModels {
        NotificationViewModelFactory(NotificationRepository())
    }
    private val orderViewModel: OrderViewModel by viewModels()
    var orderId=0L
    var userId =  ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        order = intent.getParcelableExtra("order")
        val products = order?.products ?: emptyList()
        orderId = order?.orderId?:0L
         userId = order?.address?.userId ?: ""
        selectedOrderStatus = order?.orderStatus.toString()
        binding.rvProducts.apply {
            orderDetailAdapter = OrderDetailAdapter(mutableListOf())
            adapter = orderDetailAdapter
            layoutManager =
                LinearLayoutManager(this@OrderDetailsActivity, RecyclerView.VERTICAL, false)
            addItemDecoration(VerticalItemDecoration())
        }



        orderDetailAdapter.updateItems(products)
        orderDetailAdapter.notifyDataSetChanged()
        binding.imageClose.setOnClickListener { finish() }
        binding.totalPrice.text = "Total:- " + "$${String.format("%.2f", order?.totalPrice)}"

        notificationViewModel.notificationResult.observe(this, Observer { result ->
            when (result) {
                is Resource.Loading -> {
                    showLoading()
                }
                is Resource.Success -> {
                    hideLoading()
                }
                is Resource.Error -> {
                    hideLoading()
                }
                else->{}
            }
        })


        orderViewModel.updateUserStatus.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading()

                }
                is Resource.Success -> {
                    hideLoading()
                }
                is Resource.Error -> {
                    hideLoading()
                }
                else->{}
            }
        }

        orderViewModel.setupStatusSpinner(orderId, selectedOrderStatus)

        orderViewModel.spinnerSetupStatus.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading()
                }
                is Resource.Success -> {
                    resource.data?.let { setupSpinner(it) }
                    hideLoading()
                }
                is Resource.Error -> {
                    hideLoading()
                }
                else->{}
            }
        }

        // Observe the LiveData
        orderViewModel.updateOrderStatus.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading()

                }
                is Resource.Success -> {
                                    notificationViewModel.sendNotification(
                                        NotificationInfo(
                                            title = "Testing",
                                            message = "Testing Message",
                                            notificationType = "Your Order id ${selectedOrderStatus}",
                                            receiverId = order?.address?.userId.toString(),
                                            senderId = auth.currentUser?.uid.toString()
                                        )
                                    )

                    hideLoading()
                }
                is Resource.Error -> {
                    // Show error message
                    hideLoading()
                }
                else->{}
            }
        }

    }

    private fun setupSpinner(statusSpinnerSetup: StatusSpinnerSetup) {
        val statuses = statusSpinnerSetup.statuses
        val currentStatus = statusSpinnerSetup.currentStatus
        selectedOrderStatus=currentStatus.status

        // Create the spinner adapter and pass the current status
        val adapter = StatusSpinnerAdapter(this, statuses, currentStatus)
        binding.statusAppCompatSpinner.adapter = adapter

        // Set default selection
        val selectedPosition = statuses.indexOf(currentStatus)
        if (selectedPosition >= 0) {
            binding.statusAppCompatSpinner.setSelection(selectedPosition)
        }

        binding.statusAppCompatSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedStatus = statuses[position]
                    if (selectedStatus != currentStatus && adapter.isEnabled(position)) {
                        orderViewModel.updateUser(orderId, selectedOrderStatus, userId)
                        orderViewModel.updateOrderStatus(orderId, selectedOrderStatus)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Handle case where nothing is selected if necessary
                }
            }

    }


    private fun showLoading() {
        Log.d("MyTesting","showLoading...")
    }

    private fun hideLoading() {
        Log.d("MyTesting","showLoading...")
    }
}