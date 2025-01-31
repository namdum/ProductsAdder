package com.example.productsadder.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.productsadder.data.Order
import com.example.productsadder.databinding.ItemDateHeaderBinding
import com.example.productsadder.databinding.OrderListItemBinding
import com.example.productsadder.util.convertDateFormat
import com.example.productsadder.util.extractDatePart
import com.example.productsadder.view.DateHeaderView
import com.example.productsadder.view.OrderListView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class OrderListAdapter(var orders: List<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_DATE_HEADER = 0
    private val VIEW_TYPE_ORDER_ITEM = 1
    private val oderListItemClicksSubject: PublishSubject<Order> = PublishSubject.create()
    val orderListItemItemClicks: Observable<Order> = oderListItemClicksSubject.hide()

    var isAscending = true
    fun updateItems(newItems: List<Any>) {
        orders = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_DATE_HEADER) {
            val binding =
                ItemDateHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            DateHeaderView(binding)
        } else {
            val binding =
                OrderListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            OrderListView(binding).apply {
                orderListItemItemClicks.subscribe { oderListItemClicksSubject.onNext(it) }
            }
        }


    }

    override fun getItemViewType(position: Int): Int {
        return if (orders[position] is String) VIEW_TYPE_DATE_HEADER else VIEW_TYPE_ORDER_ITEM
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_DATE_HEADER -> (holder as DateHeaderView).bind(orders[position] as String)
            VIEW_TYPE_ORDER_ITEM -> (holder as OrderListView).bind(orders[position] as Order)
        }
    }

    override fun getItemCount(): Int {
        return orders.size
    }


    fun groupOrdersByDate(orders: List<Order>): List<Any> {
        // Group orders by date, considering only the date portion (ignoring time)
        val groupedOrders = orders.groupBy { order ->
            // Extract only the date part
            order.date?.let { extractDatePart(it) }
        }

        // Create a new list of headers and orders
        val result = mutableListOf<Any>()

        // Loop through each group
        for ((date, ordersForDate) in groupedOrders) {
            // Add the date header (in your desired format)
            result.add(convertDateFormat(date.toString()))
            // Add all orders for this date
            result.addAll(ordersForDate)
        }

        return result
    }

    fun toggleSorting(orders: List<Order>) {
        isAscending = !isAscending
        val sortedOrders = if (isAscending) {
            orders.sortedBy { it.date }
        } else {
            orders.sortedByDescending { it.date }
        }
        val groupedOrders = groupOrdersByDate(sortedOrders)
        updateItems(groupedOrders)
    }

}