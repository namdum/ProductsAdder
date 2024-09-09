package com.example.productsadder.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.example.productsadder.R
import com.example.productsadder.viewmodel.Order
import com.example.productsadder.viewmodel.OrderGroup

class OrderListAdapter(var orders: List<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    fun updateItems(newItems: List<Any>) {
        orders = newItems
        notifyDataSetChanged()
    }
    private val VIEW_TYPE_DATE_HEADER = 0
    private val VIEW_TYPE_ORDER_ITEM = 1
//    class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
//    }
//
//    class OrderItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val idTextView: TextView = itemView.findViewById(R.id.idTextView)
//        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
//        val addressTextView: TextView = itemView.findViewById(R.id.addressTextView)
//    }

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
            // Bind dateLabel to your header view
            // For example:
            itemView.findViewById<TextView>(R.id.headerTextView).text = dateLabel
        }
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(order: Order) {
            // Bind order details to your item view
            // For example:
            itemView.findViewById<TextView>(R.id.orderId).text = order.orderId.toString()
            itemView.findViewById<TextView>(R.id.orderName).text = order.orderStatus
            itemView.findViewById<TextView>(R.id.orderAddress).text = order.address?.fullName
        }
    }


}