package com.example.productsadder.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.productsadder.R
import com.example.productsadder.util.OrderStatus

class StatusSpinnerAdapter(
    private val context: Context,
    private val statuses: List<OrderStatus>,
    private val currentStatus: OrderStatus
) : ArrayAdapter<OrderStatus>(context, android.R.layout.simple_spinner_item, statuses) {

    private fun isDisabled(position: Int): Boolean {
        val selectedStatus = statuses[position]
        return when (currentStatus) {
            OrderStatus.ORDER_CONFIRMED -> selectedStatus == OrderStatus.ORDER_CONFIRMED
            OrderStatus.READY_FOR_DISPATCH -> selectedStatus == OrderStatus.ORDER_CONFIRMED || selectedStatus == OrderStatus.READY_FOR_DISPATCH
            OrderStatus.DISPATCHED -> selectedStatus == OrderStatus.ORDER_CONFIRMED || selectedStatus == OrderStatus.READY_FOR_DISPATCH || selectedStatus == OrderStatus.DISPATCHED
            OrderStatus.IN_TRANSIT -> selectedStatus == OrderStatus.ORDER_CONFIRMED || selectedStatus == OrderStatus.READY_FOR_DISPATCH || selectedStatus == OrderStatus.DISPATCHED || selectedStatus == OrderStatus.IN_TRANSIT
            OrderStatus.DELIVERED -> selectedStatus == OrderStatus.ORDER_CONFIRMED || selectedStatus == OrderStatus.READY_FOR_DISPATCH || selectedStatus == OrderStatus.DISPATCHED || selectedStatus == OrderStatus.IN_TRANSIT || selectedStatus == OrderStatus.DELIVERED
        }
    }
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val textView = view as TextView
        if (isDisabled(position)) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.g_gray500)) // Use lighter color for disabled items
            textView.isEnabled = false // Disable the item
        } else {
            textView.setTextColor(ContextCompat.getColor(context, R.color.black)) // Use default color for enabled items
            textView.isEnabled = true // Enable the item
        }

        return view
    }

    override fun isEnabled(position: Int): Boolean {
        return !isDisabled(position)
    }
}