package com.example.productsadder.activity

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
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.util.OrderStatus
import com.example.productsadder.util.Resource
import com.example.productsadder.util.VerticalItemDecoration
import com.example.productsadder.viewmodel.OrderViewModel
import com.example.productsadder.viewmodel.OrderViewState
import com.example.productsadder.viewmodel.StatusSpinnerSetup
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
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
        listenToViewModel()

        orderViewModel.setupStatusSpinner(orderId, selectedOrderStatus)

    }

    private fun listenToViewModel() {
        orderViewModel.orderState.subscribeAndObserveOnMainThread {
            when(it){
                is OrderViewState.LoadingState->{
                    showLoading()
                }
                is OrderViewState.SuccessMessage->{
                    notificationViewModel.sendNotification(
                        NotificationInfo(
                            title = "Order updates",
                            message = "Your order for ${order?.products?.firstOrNull()?.product?.name} is ${selectedOrderStatus}",
                            notificationType = "Your Order id ${selectedOrderStatus}",
                            receiverId = order?.address?.userId.toString(),
                            senderId = auth.currentUser?.uid.toString()
                        )
                    )

                    hideLoading()
                    Log.d("MyTesting","SuccessMessage:--${it.successMessage}")
                    onBackPressedDispatcher
                }
                is OrderViewState.FetchStatusSpinnerSetup->{
                    it.fetchStatusSpinnerSetup?.let { setupSpinner(it) }
                    hideLoading()
                }
                is OrderViewState.ErrorMessage->{
                    hideLoading()
                    Log.d("MyTesting","SuccessMessage:--${it.errorMessage}")
                }
                else->{}
            }
        }

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
                    selectedOrderStatus = selectedStatus.status
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
//        Log.d("MyTesting","showLoading...")
    }

    private fun hideLoading() {
//        Log.d("MyTesting","showLoading...")
    }
}