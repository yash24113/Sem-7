package com.example.mybyk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class PackageAdapter(
    private val packageList: List<Package>,
    private val onItemClickListener: OnItemClickListener,

) : RecyclerView.Adapter<PackageAdapter.PackageViewHolder>() {
    private var selectedPosition = RecyclerView.NO_POSITION

    interface OnItemClickListener {
        fun onDeleteClick(packageItem: Package)
        fun onEditClick(packageItem: Package)
    }

    class PackageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val packageId: TextView = itemView.findViewById(R.id.tvPackageId)
        val packageName: TextView = itemView.findViewById(R.id.tvPackageName)
        val packageDesc: TextView = itemView.findViewById(R.id.tvPackageDesc)
        val packagePrice: TextView = itemView.findViewById(R.id.tvPackagePrice)
        val packagePrice1: TextView = itemView.findViewById(R.id.tvPackagePrice1)
        val packageValidity: TextView = itemView.findViewById(R.id.tvPackageValidity)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_package, parent, false)

        return PackageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
        val packageItem = packageList[position]
        holder.packageId.text = packageItem.packageId
        holder.packageName.text = packageItem.packageName
        holder.packageDesc.text = packageItem.packageDesc
        holder.packagePrice.text = "₹${packageItem.packagePrice} per day"
        holder.packagePrice1.text = "₹${packageItem.packagePrice1}"
        holder.packageValidity.text = packageItem.packageValidity

        // Set visibility of the delete button based on the showIcons flag
        holder.itemView.isSelected = (selectedPosition == position)

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
        }

        // Click listener for the delete button
        holder.btnDelete.setOnClickListener {
            onItemClickListener.onDeleteClick(packageItem)
        }
        holder.btnEdit.setOnClickListener {
            onItemClickListener.onEditClick(packageItem)
        }

    }

    override fun getItemCount(): Int = packageList.size
}
