package com.example.productsadder.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.productsadder.R

class ImageAdapter(var imageUris: MutableList<Uri>) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
private var imageString : MutableList<String> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_rv_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        println("imageString: " + imageString)
        if(imageString.isEmpty())
        {
            val imageUri = imageUris[position]
            holder.bindImage(imageUri) { pos ->
                imageUris.removeAt(pos)
                notifyDataSetChanged()
            }
        }
        else{
            val imageStrings = imageString[position]
            holder.bindImage(imageStrings) { pos ->
                imageString.removeAt(pos)
                notifyDataSetChanged()
            }
        }

    }

    override fun getItemCount(): Int = imageUris.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindImage(imageUri: Uri, onDelete: (Int) -> Unit) {
            val imageView = itemView.findViewById<ImageView>(R.id.image)
            Glide.with(itemView.context).load(imageUri).into(imageView)
            val imagePicked = itemView.findViewById<ImageView>(R.id.imagePicked)
            imagePicked.setOnClickListener {
                onDelete(adapterPosition)
            }
        }
        fun bindImage(imaString: String, onDelete: (Int) -> Unit) {
            val imageView = itemView.findViewById<ImageView>(R.id.image)
            Glide.with(itemView.context).load(imaString).into(imageView)
            val imagePicked = itemView.findViewById<ImageView>(R.id.imagePicked)
            imagePicked.setOnClickListener {
                onDelete(adapterPosition)
            }
        }
    }
    fun updateImageUris(newImageUris: MutableList<Uri>) {
        imageUris = newImageUris
        imageUris.addAll(newImageUris)
        notifyDataSetChanged()
    }

    fun updateImageString(newImageString: MutableList<String>) {

        println("downloadUrl.toString(): " + newImageString.toString())

        imageString = newImageString
        notifyDataSetChanged()
    }
    fun getImage(): List<Uri> {
        return imageUris.toList()
    }

}