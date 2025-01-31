package com.example.productsadder.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meangene.notification.model.NotificationInfo
import com.example.productsadder.BasicActivity
import com.example.productsadder.adapter.OrderDetailAdapter
import com.example.productsadder.adapter.StatusSpinnerAdapter
import com.example.productsadder.application.FuelApplication
import com.example.productsadder.data.Category
import com.example.productsadder.data.Order
import com.example.productsadder.data.Product
import com.example.productsadder.databinding.ActivityEditeProductBinding
import com.example.productsadder.databinding.ActivityOrderDetailsBinding
import com.example.productsadder.network.extension.getViewModelFromFactory
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.notification.CreateChatRoomViewModel
import com.example.productsadder.notification.CreateChatRoomViewState
import com.example.productsadder.notification.ViewModelFactory
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

class OrderDetailsActivity : BasicActivity() {
    private lateinit var binding: ActivityOrderDetailsBinding
    private lateinit var orderDetailAdapter: OrderDetailAdapter
    private var selectedOrderStatus = ""
    private var order: Order? = null
    private lateinit var auth: FirebaseAuth

    @Inject
    internal lateinit var fuelViewModelFactory: ViewModelFactory<CreateChatRoomViewModel>
    lateinit var fuelViewModel: CreateChatRoomViewModel


    private val orderViewModel: OrderViewModel by viewModels()
    var orderId = 0L
    var userId = ""
    companion object {
        const val ORDER = "ORDER"
        fun getIntent(context: Context, order: Order): Intent {
            val intent = Intent(context, OrderDetailsActivity::class.java)
            intent.putExtra(ORDER, order)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FuelApplication.component.inject(this)
        fuelViewModel = getViewModelFromFactory(fuelViewModelFactory)

        initUI()
        listenToViewEvent()
        listenToViewModel()

        orderViewModel.setupStatusSpinner(orderId, selectedOrderStatus)

    }

    private fun listenToViewEvent() {
        binding.apply {
            imageClose.setOnClickListener { finish() }
            totalPrice.text = "Total:- " + "$${String.format("%.2f", order?.totalPrice)}"
        }
    }

    private fun initUI() {
        auth = FirebaseAuth.getInstance()
        this.order = intent?.getParcelableExtra(ORDER) ?: return
        val products = order?.products ?: emptyList()
        orderId = order?.orderId ?: 0L
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
    }

    private fun listenToViewModel() {
        orderViewModel.orderState.subscribeAndObserveOnMainThread {
            when (it) {
                is OrderViewState.LoadingState -> {
                    showLoading()
                }

                is OrderViewState.SuccessMessage -> {
                    fuelViewModel.sendNotification(
                        NotificationInfo(
                            title = "Order updates",
                            message = "Your order for ${order?.products?.firstOrNull()?.product?.name} is ${selectedOrderStatus}",
                            notificationType = "Your Order id ${selectedOrderStatus}",
                            receiverId = order?.address?.userId.toString(),
                            senderId = auth.currentUser?.uid.toString()
                        )
                    )

                    hideLoading()
                    Log.d("MyTesting", "SuccessMessage:--${it.successMessage}")
                    onBackPressedDispatcher
                }

                is OrderViewState.FetchStatusSpinnerSetup -> {
                    it.fetchStatusSpinnerSetup?.let { setupSpinner(it) }
                    hideLoading()
                }

                is OrderViewState.ErrorMessage -> {
                    hideLoading()
                    Log.d("MyTesting", "SuccessMessage:--${it.errorMessage}")
                }

                else -> {}
            }
        }

        fuelViewModel.createChatRoomState.observe(this) { result ->
            when (result) {
                is CreateChatRoomViewState.LoadingState -> {
                    showLoading()
                }

                is CreateChatRoomViewState.CreateRoomSuccess -> {
                    hideLoading()
                    Log.d("MyTesting","notifocation:--${result.chatRoomInfo}")
                }

                is CreateChatRoomViewState.SuccessMessage -> {
                    hideLoading()
                }
                is CreateChatRoomViewState.ErrorMessage -> {
                    hideLoading()
                    Log.d("MyTesting","notifocation:--${result.errorMessage}")
                }

                else -> {}
            }
        }
    }

    private fun setupSpinner(statusSpinnerSetup: StatusSpinnerSetup) {
        val statuses = statusSpinnerSetup.statuses
        val currentStatus = statusSpinnerSetup.currentStatus
        selectedOrderStatus = currentStatus.status

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
                    id: Long,
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