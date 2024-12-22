package com.example.mybyk

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class UserPackageAdapter(
    private val packageList: List<Package>

) : RecyclerView.Adapter<UserPackageAdapter.PackageViewHolder>() {
    private var selectedPosition = RecyclerView.NO_POSITION



    class PackageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val packageName: TextView = itemView.findViewById(R.id.tvPackageName)
        val packageDesc: TextView = itemView.findViewById(R.id.tvPackageDesc)
        val packagePrice: TextView = itemView.findViewById(R.id.tvPackagePrice)
        val packageValidity: TextView = itemView.findViewById(R.id.tvPackageValidity)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_package1, parent, false)

        return PackageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
        val packageItem = packageList[position]

        holder.packageName.text = packageItem.packageName
        holder.packageDesc.text = packageItem.packageDesc
        holder.packagePrice.text = "₹${packageItem.packagePrice} per day"
        holder.packageValidity.text = packageItem.packageValidity

        // Set visibility of the delete button based on the showIcons flag
        holder.itemView.isSelected = (selectedPosition == position)

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)


            val intent = Intent(holder.itemView.context, payment::class.java)
            intent.putExtra("PACKAGE_DETAILS", packageItem) // Pass the package item
            holder.itemView.context.startActivity(intent)
        }

        // Click listener for the delete button


    }

    override fun getItemCount(): Int = packageList.size
}
