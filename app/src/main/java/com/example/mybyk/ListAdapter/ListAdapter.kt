// ListAdapter.kt
package com.example.mybyk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListAdapter(private val dataList: ArrayList<Product>, private val itemClickListener: (Product) -> Unit) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView1: ImageView = itemView.findViewById(R.id.imageView1)
        val textView: TextView = itemView.findViewById(R.id.textView)
        val imageView2: ImageView = itemView.findViewById(R.id.imageView)

        init {
            itemView.setOnClickListener {
                itemClickListener(dataList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_design, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = dataList[position]
        holder.imageView1.setImageResource(product.imageResId1)
        holder.textView.text = product.title
        holder.imageView2.setImageResource(product.imageResId2)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}
