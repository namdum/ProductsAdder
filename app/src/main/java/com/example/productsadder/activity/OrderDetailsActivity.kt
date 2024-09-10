package com.example.productsadder.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.productsadder.OrderFragment
import com.example.productsadder.R
import com.example.productsadder.adapter.OrderDetailAdapter
import com.example.productsadder.adapter.OrderListAdapter
import com.example.productsadder.adapter.ProductAdapter
import com.example.productsadder.adapter.StatusSpinnerAdapter
import com.example.productsadder.data.Order
import com.example.productsadder.data.Product
import com.example.productsadder.databinding.ActivityEditeProductBinding
import com.example.productsadder.databinding.ActivityOrderDetailsBinding
import com.example.productsadder.util.OrderStatus
import com.example.productsadder.util.VerticalItemDecoration
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class OrderDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderDetailsBinding
    private lateinit var orderDetailAdapter: OrderDetailAdapter
    private  var selectedOrderStatus=""
    private var order: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        order = intent.getParcelableExtra("order")
        val products = order?.products ?: emptyList()
        Log.d("MyTesting", "11:${products}")
        selectedOrderStatus= order?.orderStatus.toString()
        binding.rvProducts.apply {
            orderDetailAdapter = OrderDetailAdapter(mutableListOf())
            adapter = orderDetailAdapter
            layoutManager =
                LinearLayoutManager(this@OrderDetailsActivity, RecyclerView.VERTICAL, false)
            addItemDecoration(VerticalItemDecoration())
        }

        orderDetailAdapter.updateItems(products)
        orderDetailAdapter.notifyDataSetChanged()
        binding.imageClose.setOnClickListener { finish() }
        binding.totalPrice.text = "Total:- " + "$${String.format("%.2f", order?.totalPrice)}"

        setupStatusSpinner()

    }
    private fun setupStatusSpinner() {
        val firestore = FirebaseFirestore.getInstance()
        val orderId: Long = order?.orderId!!.toLong()// Replace with actual orderId
        firestore.collection("orders").document(orderId.toString())
            .get()
            .addOnSuccessListener { document ->
    //            val currentStatusString = document.getString("orderStatus") ?: "Order Confirmed"
                val currentStatusString = selectedOrderStatus
                val currentStatus = OrderStatus.values().find { it.status == currentStatusString }
                    ?: OrderStatus.ORDER_CONFIRMED

                val statuses = OrderStatus.values().toList()

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
                            id: Long
                        ) {
                            val selectedStatus = statuses[position]
                            if (selectedStatus != currentStatus && adapter.isEnabled(position)) {
                                updateOrderStatus(orderId, selectedStatus)
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            // Handle case where nothing is selected if necessary
                        }
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreUpdate", "Error fetching order status", e)
            }


        firestore.collection("user").document(order?.address?.userId ?: "").collection("orders").whereEqualTo("orderId", orderId)
            .get()
            .addOnSuccessListener { document ->
                Log.e("FirestoreUpdate", "Document info: " + document.documents)
                for(doc in document.documents) {
                    firestore.collection("user").document(order?.address?.userId ?: "").collection("orders").document(doc.id)
                        .update("orderStatus", selectedOrderStatus)
                        .addOnSuccessListener {
                            Log.d("FirestoreUpdate", "Order status updated successfully.")
                        }
                        .addOnFailureListener { exception ->
                            Log.e(
                                "FirestoreUpdate",
                                "Error updating order status",
                                exception
                            )
                        }
                }

            }
            .addOnFailureListener { e ->
                Log.e("FirestoreUpdate", "Error fetching order status", e)
            }
    }
    private fun updateOrderStatus(orderId: Long, selectedStatus: OrderStatus) {
        val db = FirebaseFirestore.getInstance()
        Log.d("FirestoreUpdate", "Querying for orderId: $orderId")

        // Querying for the specific order
        db.collection("orders")
            .whereEqualTo("orderId", orderId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirestoreUpdate", "isSuccessful..........:-${task.result}")
                    val documents = task.result
                    if (documents != null && !documents.isEmpty) {
                        // Assuming there is only one document with this orderId
                        for (document in documents) {
                            // Update the order status
                            db.collection("orders").document(document.id)
                                .update("orderStatus", selectedStatus.status)
                                .addOnSuccessListener {
                                    Log.d("FirestoreUpdate", "Order status updated successfully.")
                                }
                                .addOnFailureListener { exception ->
                                    Log.e(
                                        "FirestoreUpdate",
                                        "Error updating order status",
                                        exception
                                    )
                                }
                        }
                    } else {
                        Log.d("FirestoreUpdate", "No documents found for orderId: $orderId")
                    }
                } else {
                    Log.e("FirestoreUpdate", "Error getting documents: ", task.exception)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreUpdate", "Error fetching document", exception)
            }
    }
}