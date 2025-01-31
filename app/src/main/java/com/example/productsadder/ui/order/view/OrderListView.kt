package com.example.productsadder.ui.order.view

import androidx.recyclerview.widget.RecyclerView
import com.example.productsadder.model.Order
import com.example.productsadder.databinding.OrderListItemBinding
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.network.extension.throttleClicks
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class OrderListView(private val binding: OrderListItemBinding) :  RecyclerView.ViewHolder(binding.root) {

    private val orderListItemClicksSubject: PublishSubject<Order> = PublishSubject.create()
    val orderListItemItemClicks: Observable<Order> = orderListItemClicksSubject.hide()

    private lateinit var orderInfo: Order

    init {
        inflateUi()
    }

    private fun inflateUi() {

        binding.apply {
            orderLinearLayout.throttleClicks().subscribeAndObserveOnMainThread {
                orderListItemClicksSubject.onNext(orderInfo)
            }
        }
    }

    fun bind(order: Order) {
        this.orderInfo = order


        binding.orderNumber.text = "#"+order.orderId.toString()
        binding.orderStatus.text = "Status: "+order.orderStatus
        binding.orderFullName.text=order.address?.fullName

    }
}