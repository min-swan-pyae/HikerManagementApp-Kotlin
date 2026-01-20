package com.example.hikermanagementapp.ui.observation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hikermanagementapp.R
import com.example.hikermanagementapp.data.Observation
import java.text.SimpleDateFormat
import java.util.*

class ObservationAdapter(
    private val onEdit: (Observation) -> Unit,
    private val onDelete: (Observation) -> Unit
) : ListAdapter<Observation, ObservationAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<Observation>() {
        override fun areItemsTheSame(oldItem: Observation, newItem: Observation) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Observation, newItem: Observation) = oldItem == newItem
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivThumb: ImageView = itemView.findViewById(R.id.ivThumb)
        val tvObservation: TextView = itemView.findViewById(R.id.tvObservation)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvComments: TextView = itemView.findViewById(R.id.tvComments)
        val btnEdit: Button = itemView.findViewById(R.id.btnEditObs)
        val btnDelete: Button = itemView.findViewById(R.id.btnDeleteObs)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_observation, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.tvObservation.text = item.observation
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        holder.tvTime.text = df.format(Date(item.timestamp))
        if (!item.comments.isNullOrBlank()) {
            holder.tvComments.visibility = View.VISIBLE
            holder.tvComments.text = item.comments
        } else {
            holder.tvComments.visibility = View.GONE
        }
        if (!item.photoUri.isNullOrBlank()) {
            holder.ivThumb.visibility = View.VISIBLE
            holder.ivThumb.setImageURI(item.photoUri.toUri())
            holder.ivThumb.contentDescription = holder.itemView.context.getString(R.string.cd_observation_photo)
        } else {
            holder.ivThumb.visibility = View.GONE
        }
        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }
}
