package com.example.productsadder

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.productsadder.activity.EditCategoryActivity
import com.example.productsadder.activity.OrderDetailsActivity
import com.example.productsadder.adapter.OrderListAdapter
import com.example.productsadder.data.Order
import com.example.productsadder.databinding.FragmentOredrBinding
import com.example.productsadder.network.extension.subscribeAndObserveOnMainThread
import com.example.productsadder.viewmodel.OrderListViewModelFactory
import com.example.productsadder.viewmodel.OrderListViewModel
import com.example.productsadder.viewmodel.OrderListViewState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class OrderFragment : Fragment() {
    private lateinit var binding: FragmentOredrBinding
    private lateinit var orderAdapter: OrderListAdapter
    private lateinit var viewModel: OrderListViewModel
    private var searchQuery: String = ""
    private lateinit var ordersList:List<Order>

    companion object {
        @JvmStatic
        fun newInstance() = OrderFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOredrBinding.inflate(inflater, container, false)

        val viewModelFactory = OrderListViewModelFactory(FirebaseFirestore.getInstance())
        viewModel = ViewModelProvider(this, viewModelFactory)[OrderListViewModel::class.java]

        listenToViewEvent()
        listenToViewModel()

        return binding.root
    }

    private fun listenToViewEvent() {
        val recyclerView = binding.orderListRV
        recyclerView.layoutManager = LinearLayoutManager(context)

        orderAdapter = OrderListAdapter(emptyList()).apply {
            orderListItemItemClicks.subscribeAndObserveOnMainThread { messageInfo ->
                requireContext().startActivity(OrderDetailsActivity.getIntent(requireContext(), messageInfo))
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = orderAdapter

        binding.apply {
           searchAppCompatEditText.setOnClickListener {
                showDatePickerDialog()
            }
           sortAppCompatImageView.setOnClickListener {
               searchAppCompatEditText.text=""
                orderAdapter.toggleSorting(ordersList)
                if (orderAdapter.isAscending) {
                   sortAppCompatImageView.setBackgroundResource(R.drawable.ic_desc)
                } else {
                   sortAppCompatImageView.setBackgroundResource(R.drawable.ic_asce)
                }
            }
        }
    }

    private fun listenToViewModel() {
        viewModel.orderListState.subscribeAndObserveOnMainThread {
            when(it){
                is OrderListViewState.LoadingState->{
                    binding.progressbar.isVisible=true
                }
                is OrderListViewState.FetchOrderList->{
                    binding.progressbar.isVisible=false
                    orderAdapter.isAscending=true
                orderAdapter.toggleSorting(it.fetchOrderList)
                ordersList=it.fetchOrderList
                orderAdapter.notifyDataSetChanged()
                }
                is OrderListViewState.ErrorMessage->{
                    binding.progressbar.isVisible=false
                }
            }
        }
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
                val filteredOrders = ordersList.filter { order ->
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

    fun parseDate(dateString: String?, pattern: String = "yyyy-MM-dd"): Date? {
        return try {
            val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
            dateString?.let { dateFormat.parse(it) }
        } catch (e: Exception) {
            null
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchOrders()
    }

}