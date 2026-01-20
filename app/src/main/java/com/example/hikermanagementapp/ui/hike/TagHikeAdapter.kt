package com.example.hikermanagementapp.ui.hike

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
import com.example.hikermanagementapp.data.Hike
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.Locale
import android.view.ContextThemeWrapper

class TagHikeAdapter(
    private val onClick: (Hike) -> Unit,
    private val onEdit: (Hike) -> Unit,
    private val onDelete: (Hike) -> Unit
) : ListAdapter<Hike, TagHikeAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<Hike>() {
        override fun areItemsTheSame(oldItem: Hike, newItem: Hike) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Hike, newItem: Hike) = oldItem == newItem
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivHikePhoto: ImageView? = view.findViewById(R.id.ivHikePhoto)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val cgAttrs: ChipGroup? = view.findViewById(R.id.cgAttributes)
        val tvDesc: TextView? = view.findViewById(R.id.tvDescriptionPreview)
        val tvLocationDate: TextView? = view.findViewById(R.id.tvLocationDate)
        val tvMeta: TextView? = view.findViewById(R.id.tvMeta)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    private fun hasTagLayout(parent: ViewGroup): Boolean = try {
        parent.context.resources.getLayout(R.layout.item_hike_tags); true
    } catch (_: Exception) { false }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val layoutId = if (hasTagLayout(parent)) R.layout.item_hike_tags else R.layout.item_hike
        return VH(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))
    }

    private fun formatRating(r: Float?): String? = when {
        r == null || r <= 0f -> null
        r % 1f == 0f -> r.toInt().toString()
        else -> String.format(Locale.getDefault(), "%.1f", r)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val ctx = holder.itemView.context

        // Display photo if available
        holder.ivHikePhoto?.let { imageView ->
            item.photoUri?.let { uriString ->
                try {
                    val uri = uriString.toUri()
                    imageView.setImageURI(uri)
                    imageView.visibility = View.VISIBLE
                } catch (_: Exception) {
                    imageView.visibility = View.GONE
                }
            } ?: run {
                imageView.visibility = View.GONE
            }
        }

        holder.tvName.text = item.name
        val ratingStr = formatRating(item.rating)
        val elev = item.elevationGainM?.takeIf { it > 0 }
        val desc = item.description?.trim()
        val longLocationView: TextView? = holder.itemView.findViewById(R.id.tvLongLocation)
        val isLongLocation = item.location.length > 35

        fun makeChip(label: String, cd: String): Chip {
            return Chip(ContextThemeWrapper(ctx, R.style.HikeAttributeChip)).apply {
                text = label
                contentDescription = cd
                isClickable = false
                isCheckable = false
            }
        }

        if (holder.cgAttrs != null) {
            holder.cgAttrs.removeAllViews()
            if (isLongLocation) {
                longLocationView?.visibility = View.VISIBLE
                longLocationView?.text = item.location
                longLocationView?.contentDescription = ctx.getString(R.string.cd_location, item.location)
            } else {
                longLocationView?.visibility = View.GONE
                holder.cgAttrs.addView(makeChip(item.location, ctx.getString(R.string.cd_location, item.location)))
            }
            holder.cgAttrs.addView(makeChip(item.date, ctx.getString(R.string.cd_date, item.date)))
            val lengthLabel = if (item.lengthKm % 1.0 == 0.0) ctx.getString(R.string.chip_length_int, item.lengthKm.toInt()) else ctx.getString(R.string.chip_length_float, item.lengthKm)
            val lengthCd = if (item.lengthKm % 1.0 == 0.0) ctx.resources.getQuantityString(R.plurals.cd_length_kilometers, item.lengthKm.toInt(), item.lengthKm.toInt()) else ctx.getString(R.string.cd_length_float, item.lengthKm)
            holder.cgAttrs.addView(makeChip(lengthLabel, lengthCd))
            holder.cgAttrs.addView(makeChip(ctx.getString(R.string.chip_difficulty, item.difficulty), ctx.getString(R.string.cd_difficulty, item.difficulty)))
            val parkingLabel = if (item.parkingAvailable) ctx.getString(R.string.chip_parking_available) else ctx.getString(R.string.chip_parking_unavailable)
            val parkingCd = if (item.parkingAvailable) ctx.getString(R.string.cd_parking_available) else ctx.getString(R.string.cd_parking_unavailable)
            holder.cgAttrs.addView(makeChip(parkingLabel, parkingCd))
            elev?.let {
                val elevCd = ctx.resources.getQuantityString(R.plurals.cd_elevation_gain_meters, it, it)
                holder.cgAttrs.addView(makeChip(ctx.getString(R.string.chip_elevation_gain, it), elevCd))
            }
            ratingStr?.let {
                holder.cgAttrs.addView(makeChip(ctx.getString(R.string.chip_rating, it), ctx.getString(R.string.cd_rating, it)))
            }
            holder.tvDesc?.let { tv ->
                if (!desc.isNullOrEmpty()) { tv.visibility = View.VISIBLE; tv.text = desc } else tv.visibility = View.GONE
            }
        } else {
            holder.tvLocationDate?.text = ctx.getString(R.string.location_date_combined, item.location, item.date)
            val parkingCd = if (item.parkingAvailable) ctx.getString(R.string.cd_parking_available) else ctx.getString(R.string.cd_parking_unavailable)
            val metaText = buildString {
                append(lengthLabelForLegacy(item, ctx)).append(" • ")
                append(item.difficulty).append(" • ")
                append(parkingCd)
                elev?.let { append(" • ").append(ctx.getString(R.string.chip_elevation_gain, it)) }
                ratingStr?.let { append(" • ").append(ctx.getString(R.string.chip_rating, it)) }
            }
            holder.tvMeta?.text = metaText
        }

        holder.itemView.setOnClickListener { onClick(item) }
        holder.btnEdit.setOnClickListener { onEdit(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    private fun lengthLabelForLegacy(item: Hike, ctx: android.content.Context): String =
        if (item.lengthKm % 1.0 == 0.0) ctx.getString(R.string.chip_length_int, item.lengthKm.toInt()) else ctx.getString(R.string.chip_length_float, item.lengthKm)
}

