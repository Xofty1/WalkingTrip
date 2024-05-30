package com.hoofit.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hoofit.app.R
import com.hoofit.app.data.Trail

class TrailAdapter(private val context: Context, private val trails: MutableList<Trail>?) :
    RecyclerView.Adapter<TrailAdapter.ViewHolder>() {

    private var onItemLongClickListener: OnItemLongClickListener? = null
    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        onItemLongClickListener = listener
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.trail_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClick(trails!![position])
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClickListener?.onItemLongClick(trails!![position]) ?: false
        }

        holder.textName.text = trails!![position].name
        var description = trails[position].description
        if (description!!.length > 50) {
            description = description.substring(0, 47) + "..."
        }
        holder.textDescription.text = description
        holder.textLevel.text = "Сложность: ${trails[position].difficulty}"
    }

    override fun getItemCount(): Int {
        return trails!!.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.text_name)
        val textDescription: TextView = itemView.findViewById(R.id.text_description)
        val textLevel: TextView = itemView.findViewById(R.id.text_level)
    }

    interface OnItemClickListener {
        fun onItemClick(trail: Trail)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(trail: Trail): Boolean
    }
}
