package com.example.productsadder.ui.order.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.productsadder.model.Order
import com.example.productsadder.databinding.ItemDateHeaderBinding
import com.example.productsadder.databinding.OrderListItemBinding
import com.example.productsadder.util.convertDateFormat
import com.example.productsadder.util.extractDatePart
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
        val groupedOrders = orders.groupBy { order ->
            order.date?.let { extractDatePart(it) }
        }
        val result = mutableListOf<Any>()

        for ((date, ordersForDate) in groupedOrders) {
            result.add(convertDateFormat(date.toString()))
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