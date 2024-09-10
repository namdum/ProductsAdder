package com.example.productsadder

import android.app.DatePickerDialog
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
import com.example.productsadder.viewmodel.OrderListViewModelFactory
import com.example.productsadder.viewmodel.OrderListViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class OrderFragment : Fragment(R.layout.fragment_oredr) {
    private lateinit var binding: FragmentOredrBinding
    private lateinit var orderAdapter: OrderListAdapter
    private lateinit var viewModel: OrderListViewModel
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
                orderAdapter.isAscending=true
                orderAdapter.toggleSorting(orders)
                // Prepare the data with dates

                // Set up search field
                binding.searchAppCompatEditText.setOnClickListener {
                    showDatePickerDialog()
                }
                binding.sortAppCompatImageView.setOnClickListener {
                    binding.searchAppCompatEditText.text=""
                    orderAdapter.toggleSorting(orders)
                    // Optionally update button text based on sorting order
                    if (orderAdapter.isAscending) {
                        binding.sortAppCompatImageView.setBackgroundResource(R.drawable.ic_desc)
                    } else {
                        binding.sortAppCompatImageView.setBackgroundResource(R.drawable.ic_asce)
                    }
                }
                orderAdapter.notifyDataSetChanged()
            }
        }


        // Fetch orders when the fragment is created or based on your specific logic
        viewModel.fetchOrders()
        return binding.root
    }
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                // Create a Calendar object and set the selected date
                calendar.set(year, month, dayOfMonth)
                // Format the date to "10 June 2024"
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(calendar.time)
                // Set the formatted date to the EditText and update the search query
                binding.searchAppCompatEditText.setText(formattedDate)
                searchQuery = formattedDate
                updateDataWithFilter(searchQuery)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun updateDataWithFilter(dateFilter: String? = null) {
        lifecycleScope.launch {
            viewModel.orders.collect { orders ->
                val filteredOrders = orders.filter { order ->
                    // Date Filter Matching
                    val matchesDateFilter = dateFilter?.let {
                        val orderDate = parseDate(order.date, "yyyy-MM-dd")
                        val filterDate = parseDate(dateFilter, "dd MMMM yyyy")

                        orderDate == filterDate
                    } ?: true

                    // Search Query Matching
                    val matchesSearchQuery = searchQuery.isEmpty() ||
                            order.date?.let {
                                val parsedOrderDate = parseDate(it, "yyyy-MM-dd")
                                val formattedOrderDate = parsedOrderDate?.let { date ->
                                    SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(date)
                                }

                                formattedOrderDate?.contains(searchQuery, ignoreCase = true) ?: false
                            } == true ||
                            order.address?.fullName?.contains(searchQuery, ignoreCase = true) == true

                    matchesDateFilter && matchesSearchQuery
                }

                // Group orders by date and update the adapter
                val groupedOrders = orderAdapter.groupOrdersByDate(filteredOrders)
                orderAdapter.updateItems(groupedOrders)
                orderAdapter.notifyDataSetChanged()
            }
        }
    }

    fun parseDate(dateString: String?, pattern: String = "yyyy-MM-dd"): Date? {
        return try {
            val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
            dateString?.let { dateFormat.parse(it) }
        } catch (e: Exception) {
            null
        }
    }


}