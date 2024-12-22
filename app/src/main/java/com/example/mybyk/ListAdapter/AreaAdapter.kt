package com.example.mybyk

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AreaAdapter(
    private val areaList: List<Area>,

    private val onItemClickListener: OnItemClickListener,
    private val showIcons: Boolean // This flag controls the visibility of the icons
) : RecyclerView.Adapter<AreaAdapter.AreaViewHolder>() {

    interface OnItemClickListener {
        fun onUpdateClick(area: Area)
        fun onDeleteClick(area: Area)
    }
    private var selectedPosition = RecyclerView.NO_POSITION

    class AreaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val areaName: TextView = itemView.findViewById(R.id.tvAreaName)
        val ivCycleImage: ImageView = itemView.findViewById(R.id.ivCycleImage)
        val btnUpdate: ImageView = itemView.findViewById(R.id.btnUpdate)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AreaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_area, parent, false) // Assuming you meant 'item_area_card'
        return AreaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AreaViewHolder, position: Int) {
        val area = areaList[position]



        holder.itemView.isSelected = (selectedPosition == position)

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
        }
        holder.areaName.text = area.areaName

        // Set visibility based on the flag
        if (showIcons) {
            holder.btnUpdate.visibility = View.VISIBLE
            holder.btnDelete.visibility = View.VISIBLE
        } else {
            holder.btnUpdate.visibility = View.GONE
            holder.btnDelete.visibility = View.GONE
        }

        // Set click listeners for the buttons
        holder.btnUpdate.setOnClickListener {
            onItemClickListener.onUpdateClick(area)
        }
        holder.btnDelete.setOnClickListener {
            onItemClickListener.onDeleteClick(area)
        }

        // Click listener for the cycle image to start a new activity
        holder.ivCycleImage.setOnClickListener {
            val intent = Intent(holder.itemView.context, CycleListActivity::class.java)
            intent.putExtra("areaDocumentId", area.documentId)
            intent.putExtra("areaName", area.areaName)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = areaList.size
}
