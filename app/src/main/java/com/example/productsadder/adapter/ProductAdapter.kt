package com.example.productsadder.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.productsadder.R
import com.example.productsadder.activity.EditeProductActivity
import com.example.productsadder.data.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProductAdapter(val products: MutableList<Product>) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: AppCompatTextView = itemView.findViewById(R.id.productName)
        val categoryNameTextView: AppCompatTextView = itemView.findViewById(R.id.category)
        val descriptionTextView: AppCompatTextView = itemView.findViewById(R.id.description)
        val priceTextView: AppCompatTextView = itemView.findViewById(R.id.price)
        val offerPercentageTextView: AppCompatTextView = itemView.findViewById(R.id.offerpercentage)
        val sizeTextView: AppCompatTextView = itemView.findViewById(R.id.size)
//        val colorLayout: RecyclerView = itemView.findViewById(R.id.color)
//        val imageLayout: LinearLayout = itemView.findViewById(R.id.image)
        val editButton: AppCompatImageView = itemView.findViewById(R.id.editImageView)
        val deleteButton: AppCompatImageView = itemView.findViewById(R.id.deleteImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.nameTextView.text = product.name
        holder.categoryNameTextView.text = product.category
        holder.descriptionTextView.text = product.description
        holder.priceTextView.text = product.price.toString()
        holder.offerPercentageTextView.text = product.offerPercentage.toString()
        holder.sizeTextView.text = product.sizes?.joinToString(", ")

//        holder.colorLayout.removeAllViews()
//        product.colors?.forEach { color ->
//            val colorView = View(holder.itemView.context)
//            colorView.setBackgroundColor(color)
//            holder.colorLayout.addView(colorView)
//        }
//
//        holder.imageLayout.removeAllViews()
//        product.images.forEach { image ->
//            val imageView = AppCompatImageView(holder.itemView.context)
//            imageView.setImageResource(R.drawable.chair)
//            Glide.with(holder.itemView.context).load(image).into(imageView)
//            holder.imageLayout.addView(imageView)
//        }

        holder.editButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, EditeProductActivity::class.java)
            holder.itemView.context.startActivity(intent)
        }

        holder.deleteButton.setOnClickListener {
            deleteProduct(product, position, holder.itemView)
        }
    }

    private fun deleteProduct(product: Product, position: Int, itemView: View) {
        val context = itemView.context
        val alertDialog = android.app.AlertDialog.Builder(context)

        alertDialog.setTitle("Delete Product")
        alertDialog.setMessage("Are you sure you want to delete this product?")

        alertDialog.setPositiveButton("Yes") { _, _ ->
            val firestore = FirebaseFirestore.getInstance()
            val userId = FirebaseAuth.getInstance().uid!!

            firestore.collection("Product")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.documents.isNotEmpty()) {
                        val documentId = querySnapshot.documents[0].id
                        firestore.collection("user").document(userId).collection("Product").document(documentId)
                            .delete()
                            .addOnSuccessListener {
                                products.removeAt(position)
                                notifyDataSetChanged()
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Error", "Error deleting product: $exception")
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Error", "Error getting product: $exception")
                }
        }

        alertDialog.setNegativeButton("No") { _, _ -> }

        alertDialog.show()
    }

    override fun getItemCount(): Int = products.size
}