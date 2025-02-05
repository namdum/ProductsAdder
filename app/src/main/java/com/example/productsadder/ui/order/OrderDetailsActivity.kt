package com.example.productsadder.ui.order

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meangene.notification.model.NotificationInfo
import com.example.productsadder.base.BasicActivity
import com.example.productsadder.ui.order.view.OrderDetailAdapter
import com.example.productsadder.ui.adapter.StatusSpinnerAdapter
import com.example.productsadder.application.ProductAdderApplication
import com.example.productsadder.model.Order
import com.example.productsadder.databinding.ActivityOrderDetailsBinding
import com.example.productsadder.network.extension.getViewModelFromFactory
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.notification.NotificationViewModel
import com.example.productsadder.notification.CreateNotificationViewState
import com.example.productsadder.di.ViewModelFactory
import com.example.productsadder.util.VerticalItemDecoration
import com.example.productsadder.ui.viewmodel.OrderViewModel
import com.example.productsadder.ui.viewmodel.OrderViewState
import com.example.productsadder.ui.viewmodel.StatusSpinnerSetup
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber
import javax.inject.Inject

class OrderDetailsActivity : BasicActivity() {
    private lateinit var binding: ActivityOrderDetailsBinding
    private lateinit var orderDetailAdapter: OrderDetailAdapter
    private var selectedOrderStatus = ""
    private var order: Order? = null
    private lateinit var auth: FirebaseAuth

    @Inject
    internal lateinit var notificationViewModelFactory: ViewModelFactory<NotificationViewModel>
    lateinit var notificationViewModel: NotificationViewModel

    @Inject
    internal lateinit var orderViewModelFactory: ViewModelFactory<OrderViewModel>
    lateinit var orderViewModel: OrderViewModel
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

        ProductAdderApplication.component.inject(this)
        notificationViewModel = getViewModelFromFactory(notificationViewModelFactory)
        orderViewModel = getViewModelFromFactory(orderViewModelFactory)

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
                    Timber.d("SuccessMessage:--${it.successMessage}")
                    onBackPressedDispatcher
                }

                is OrderViewState.FetchStatusSpinnerSetup -> {
                    it.fetchStatusSpinnerSetup?.let { setupSpinner(it) }
                    hideLoading()
                }

                is OrderViewState.ErrorMessage -> {
                    hideLoading()
                    Timber.e("errorMessage:--${it.errorMessage}")
                }

                else -> {}
            }
        }

        notificationViewModel.createChatRoomState.observe(this) { result ->
            when (result) {
                is CreateNotificationViewState.LoadingState -> {
                    showLoading()
                }

                is CreateNotificationViewState.CreateRoomSuccess -> {
                    hideLoading()
                    Timber.d("notification:--${result.chatRoomInfo}")
                    onBackPressedDispatcher.onBackPressed()
                }

                is CreateNotificationViewState.SuccessMessage -> {
                    hideLoading()
                }
                is CreateNotificationViewState.ErrorMessage -> {
                    hideLoading()
                    Timber.e("notification:--${result.errorMessage}")
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