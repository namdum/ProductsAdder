package com.example.productsadder.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.productsadder.R
import com.example.productsadder.activity.OrderDetailsActivity
import com.example.productsadder.data.Order
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class OrderListAdapter(var orders: List<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
private val VIEW_TYPE_DATE_HEADER = 0
    private val VIEW_TYPE_ORDER_ITEM = 1

    var isAscending = true
    fun updateItems(newItems: List<Any>) {
        orders = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_DATE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_date_header, parent, false)
            DateHeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.order_list_item, parent, false)
            OrderViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (orders[position] is String) VIEW_TYPE_DATE_HEADER else VIEW_TYPE_ORDER_ITEM
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_DATE_HEADER -> (holder as DateHeaderViewHolder).bind(orders[position] as String)
            VIEW_TYPE_ORDER_ITEM -> (holder as OrderViewHolder).bind(orders[position] as Order)
        }
    }

    override fun getItemCount(): Int {
        return orders.size
    }

    inner class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(dateLabel: String) {
            itemView.findViewById<TextView>(R.id.headerTextView).text = dateLabel
        }
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(order: Order) {
            itemView.findViewById<TextView>(R.id.orderNumber).text = order.orderId.toString()
            itemView.findViewById<TextView>(R.id.orderStatus).text = order.orderStatus
            itemView.findViewById<TextView>(R.id.orderFullName).text = order.address?.fullName
            itemView.setOnClickListener {
                val order = order
                val intent = Intent(itemView.context, OrderDetailsActivity::class.java).apply {
                    putExtra("order", order) // Ensure Order implements Parcelable
                }
                itemView.context.startActivity(intent)

            }
        }
    }
    // Utility method to convert date format from yyyy-MM-dd to dd-MM-yyyy
    private fun convertDateFormat(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString // Return original dateString if parsing fails
        }
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
    private fun extractDatePart(datetime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val date = inputFormat.parse(datetime)
            val today = Calendar.getInstance()
            val yesterday = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }

            date?.let {
                val calendarDate = Calendar.getInstance().apply { time = it }
                when {
                    calendarDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                            calendarDate.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                            calendarDate.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) -> "Today"
                    calendarDate.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                            calendarDate.get(Calendar.MONTH) == yesterday.get(Calendar.MONTH) &&
                            calendarDate.get(Calendar.DAY_OF_MONTH) == yesterday.get(Calendar.DAY_OF_MONTH) -> "Yesterday"
                    else -> inputFormat.format(it) // Format to just the date part for other dates
                }
            } ?: datetime
        } catch (e: Exception) {
            datetime // Return original string if parsing fails
        }
    }

}