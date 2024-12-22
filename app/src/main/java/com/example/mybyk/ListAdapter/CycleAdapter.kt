package com.example.mybyk
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class CycleAdapter(
    private var cycleList: List<Cycle>,
    private val onScanClick: (Cycle) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_CYCLE = 1
    private val VIEW_TYPE_EMPTY = 0

    override fun getItemViewType(position: Int): Int {
        return if (cycleList.isEmpty()) VIEW_TYPE_EMPTY else VIEW_TYPE_CYCLE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_CYCLE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cycle, parent, false)

            CycleViewHolder(view)


        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_no_cycle, parent, false)
            EmptyViewHolder(view)
        }
    }

//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        if (holder is CycleViewHolder) {
//            holder.bind(cycleList[position])
//        }
//
//
//        // Set the click listener for the btnrigh
//
//
//    }

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CycleViewHolder) {
            // Bind your data to the view holder here

                holder.bind(cycleList[position])
            holder.itemView.isSelected = (selectedPosition == position)

            holder.itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = holder.adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
            }
            holder.btnrigh.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, userpackagedetails::class.java)

                // Optional: Pass any required data to the UserPackageDetails activity

                context.startActivity(intent)
            }
        // Call bind() to set the cycle data
            }
            // Set the click listener for btnrigh


    }

    override fun getItemCount(): Int {
        return if (cycleList.isEmpty()) 1 else cycleList.size
    }

    inner class CycleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lockNumberTextView: TextView = itemView.findViewById(R.id.tvLockNumber)
        private val scanButton: ImageView = itemView.findViewById(R.id.btnScan)
        val btnrigh: ImageView = itemView.findViewById(R.id.btnrigh)

        fun bind(cycle: Cycle) {
            lockNumberTextView.text = "Lock Number: ${cycle.cycleLockNumber}"

            // Set up the scan button click listener
            scanButton.setOnClickListener {
                onScanClick(cycle)
            }


        }
    }

    inner class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // This ViewHolder is for the "No Cycle Available" message
    }
}
