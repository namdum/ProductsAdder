package com.example.productsadder.adapter

import android.database.Observable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.productsadder.R
import com.example.productsadder.activity.EditCategoryActivity
import com.example.productsadder.data.Category
import com.example.productsadder.data.CategoryState
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.subjects.PublishSubject


class CategoryAdapter(val categories: MutableList<Category>) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private val CategoryItemClicksSubject: PublishSubject<CategoryState> = PublishSubject.create()
    val categoryClicks: io.reactivex.Observable<CategoryState> = CategoryItemClicksSubject.hide()
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryNameTextView: AppCompatTextView = itemView.findViewById(R.id.categoryAppCompatTextView)
        val categoryAppCompatImageView : AppCompatImageView = itemView.findViewById(R.id.categoryAppCompatImageView)
        val editButton: AppCompatImageView = itemView.findViewById(R.id.editbtn)
        val deleteButton: AppCompatImageView = itemView.findViewById(R.id.deletebtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryNameTextView.text = category.category
        Glide.with(holder.itemView.context)
            .load(category.image)
            .placeholder(R.drawable.chair)
            .into(holder.categoryAppCompatImageView)

        holder.editButton.setOnClickListener {
//            holder.itemView.context.startActivity(EditCategoryActivity.getIntent(holder.itemView.context, category))
            CategoryItemClicksSubject.onNext(CategoryState.EditCategoryClick(category))

        }

        holder.deleteButton.setOnClickListener {
//            deleteCategory(category, position, holder.itemView)
            CategoryItemClicksSubject.onNext(CategoryState.DeleteCategoryClick(category))
        }
    }

    private fun deleteCategory(category: Category, position: Int, itemView: View) {
        val context = itemView.context
        val alertDialog = android.app.AlertDialog.Builder(context)

        alertDialog.setTitle("Delete Category")
        alertDialog.setMessage("Are you sure you want to delete this category?")

        alertDialog.setPositiveButton("Yes") { _, _ ->
            val firestore = FirebaseFirestore.getInstance()

            firestore.collection("Category")
                .whereEqualTo("category", category.category)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.documents.isNotEmpty()) {
                        val documentId = querySnapshot.documents[0].id
                        firestore.collection("Category").document(documentId)
                            .delete()
                            .addOnSuccessListener {
                                categories.removeAt(position)
                                notifyDataSetChanged()
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Error", "Error deleting category: $exception")
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Error", "Error getting category: $exception")
                }

        }

        alertDialog.setNegativeButton("No") { _, _ -> }

        alertDialog.show()
    }

    override fun getItemCount(): Int = categories.size
}
