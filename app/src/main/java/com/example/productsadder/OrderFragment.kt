package com.example.productsadder

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.productsadder.adapter.OrderListAdapter
import com.example.productsadder.databinding.FragmentOredrBinding
import com.example.productsadder.viewmodel.Order
import com.example.productsadder.viewmodel.OrderListViewModelFactory
import com.example.productsadder.viewmodel.OrderListViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class OrderFragment : Fragment(R.layout.fragment_oredr) {
    private lateinit var binding: FragmentOredrBinding
    private lateinit var orderAdapter: OrderListAdapter
    private lateinit var viewModel: OrderListViewModel
    private var isAscending: Boolean = true
    private var searchQuery: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Use the specific binding class generated from fragment_order_list.xml
        binding = FragmentOredrBinding.inflate(inflater, container, false)
        val viewModelFactory = OrderListViewModelFactory(
            // Initialize ViewModel
                FirebaseFirestore.getInstance(),
        FirebaseAuth.getInstance()
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[OrderListViewModel::class.java]

        // Set up RecyclerView
        val recyclerView = binding.orderListRV
        recyclerView.layoutManager = LinearLayoutManager(context)

        orderAdapter = OrderListAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = orderAdapter
        // Observe the orders LiveData from the ViewModel

        lifecycleScope.launchWhenStarted {
            viewModel.orders.collect { orders ->
                // Log the size of orders
                Log.d("OrderFragment", "Orders collected: ${orders.size}")

                // Prepare the data with dates
                val preparedData = prepareData(orders)

                // Log the prepared data to check date and order information
//                for (item in preparedData) {
//                    when (item) {
//                        is String -> Log.d("OrderFragment", "Date Header: $item")
//                        is Order -> Log.d("OrderFragment", "Order Item: ${item.orderId}, ${item.address}, ${item.address?.fullName ?: "No Address"}")
//                    }
//                }

                // Update the adapter with prepared data
                orderAdapter.updateItems(preparedData)
                // Set up search field
                binding.searchAppCompatEditText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        searchQuery = s.toString()
                        updateDataWithFilter()
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })

                orderAdapter.notifyDataSetChanged()
            }
        }


        // Fetch orders when the fragment is created or based on your specific logic
        viewModel.fetchOrders()
        return binding.root
    }
    private fun updateDataWithFilter(dateFilter: String? = null) {
        lifecycleScope.launch {
            viewModel.orders.collect { orders ->
                val filteredOrders = orders
                    .filter { order ->
                        val matchesDateFilter = dateFilter?.let {
                            val orderDate = parseDate(order.date)
                            val filterDate = parseDate(dateFilter)
                            orderDate == filterDate
                        } ?: true

                        val matchesSearchQuery = searchQuery.isEmpty() || order.date?.contains(searchQuery, ignoreCase = true) == true ||
                                order.address?.fullName?.contains(searchQuery, ignoreCase = true) == true

                        matchesDateFilter && matchesSearchQuery
                    }

                val preparedData = prepareData(filteredOrders)
                orderAdapter.updateItems(preparedData)
                orderAdapter.notifyDataSetChanged()
            }
        }
    }

    fun prepareData(orders: List<Order>): List<Any> {
        val data = mutableListOf<Any>()
        var lastDate: String? = null

        for (order in orders) {
            val orderDate = order.date
//            val orderDate = parseDate(order.date)
            if (orderDate != lastDate) {
                data.add(orderDate ?: "Unknown Date") // Add date header if it’s different
                lastDate = orderDate
            }
            data.add(order)
        }

        return data
    }
    fun parseDate(dateString: String?, format: String = "dd-MM-yyyy"): Date? {
        if (dateString.isNullOrEmpty()) return null
        return try {
            val formatter = SimpleDateFormat(format, Locale.getDefault())
            formatter.parse(dateString)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }

}