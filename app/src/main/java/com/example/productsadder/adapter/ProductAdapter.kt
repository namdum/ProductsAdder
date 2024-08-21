//package com.example.productsadder.adapter
//
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.appcompat.widget.AppCompatButton
//import androidx.appcompat.widget.AppCompatEditText
//import androidx.appcompat.widget.AppCompatTextView
//import androidx.recyclerview.widget.RecyclerView
//import com.example.productsadder.R
//import com.example.productsadder.data.Category
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//
//class ProductAdapter(val categories: MutableList<Category>) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {
//
//    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val categoryNameTextView: AppCompatTextView = itemView.findViewById(R.id.productAppCompatTextView)
//        val editText: AppCompatEditText = itemView.findViewById(R.id.editText)
//        val editButton: AppCompatButton = itemView.findViewById(R.id.editbtn)
//        val saveButton: AppCompatButton = itemView.findViewById(R.id.savebtn)
//        val deleteButton: AppCompatButton = itemView.findViewById(R.id.deletebtn)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val category = categories[position]
//        holder.categoryNameTextView.text = category.category
//
//        holder.editButton.setOnClickListener {
//            holder.editButton.visibility = View.GONE
//            holder.saveButton.visibility = View.VISIBLE
//            holder.categoryNameTextView.visibility = View.GONE
//            holder.editText.visibility = View.VISIBLE
//            holder.editText.setText(category.category)
//        }
//
//        holder.saveButton.setOnClickListener {
//            holder.saveButton.visibility = View.GONE
//            holder.editButton.visibility = View.VISIBLE
//            holder.categoryNameTextView.visibility = View.VISIBLE
//            holder.editText.visibility = View.GONE
//
//            val newCategoryName = holder.editText.text.toString()
//            val oldCategoryName = category.category
//            val firestore = FirebaseFirestore.getInstance()
//            val userId = FirebaseAuth.getInstance().uid!!
//
//            category.category = newCategoryName
//
//            firestore.collection("user").document(userId).collection("Category")
//                .whereEqualTo("category", oldCategoryName)
//                .get()
//                .addOnSuccessListener { querySnapshot ->
//                    if (querySnapshot.documents.isNotEmpty()) {
//                        val document = querySnapshot.documents[0]
//                        document.reference.update("category", newCategoryName)
//                            .addOnSuccessListener {
//                                categories[position] = category
//                                notifyDataSetChanged()
//                                holder.categoryNameTextView.text = newCategoryName
//                            }
//                            .addOnFailureListener { exception ->
//                                Log.e("Error", "Error updating category: $exception")
//                            }
//                    }
//                }
//                .addOnFailureListener { exception ->
//                    Log.e("Error", "Error getting category: $exception")
//                }
//        }
//
//        holder.deleteButton.setOnClickListener {
//            deleteCategory(category, position, holder.itemView)
//        }
//    }
//
//    private fun deleteCategory(category: Category, position: Int, itemView: View) {
//        val context = itemView.context
//        val alertDialog = android.app.AlertDialog.Builder(context)
//
//        alertDialog.setTitle("Delete Category")
//        alertDialog.setMessage("Are you sure you want to delete this category?")
//
//        alertDialog.setPositiveButton("Yes") { _, _ ->
//            val firestore = FirebaseFirestore.getInstance()
//            val userId = FirebaseAuth.getInstance().uid!!
//
//            firestore.collection("user").document(userId).collection("Category")
//                .whereEqualTo("category", category.category)
//                .get()
//                .addOnSuccessListener { querySnapshot ->
//                    if (querySnapshot.documents.isNotEmpty()) {
//                        val documentId = querySnapshot.documents[0].id
//                        firestore.collection("user").document(userId).collection("Category").document(documentId)
//                            .delete()
//                            .addOnSuccessListener {
//                                categories.removeAt(position)
//                                notifyDataSetChanged()
//                            }
//                            .addOnFailureListener { exception ->
//                                Log.e("Error", "Error deleting category: $exception")
//                            }
//                    }
//                }
//                .addOnFailureListener { exception ->
//                    Log.e("Error", "Error getting category: $exception")
//                }
//        }
//
//        alertDialog.setNegativeButton("No") { _, _ -> }
//
//        alertDialog.show()
//    }
//
//    override fun getItemCount(): Int = categories.size
//}