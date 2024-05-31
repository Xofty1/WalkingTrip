package com.hoofit.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.hoofit.app.R
import com.hoofit.app.data.Reserve
import com.hoofit.app.data.ReserveData

class ReserveAdapter(private val context: Context, private val reserves: ReserveData) :
    RecyclerView.Adapter<ReserveAdapter.ViewHolder>() {
    private var itemClickListener: OnItemClickListener? = null
    private var onItemLongClickListener: OnItemLongClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        onItemLongClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.reserve_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClick(reserves.reserves!![position])
        }
        holder.itemView.setOnLongClickListener {
            onItemLongClickListener?.onItemLongClick(reserves.reserves!![position])
            true
        }

        holder.textName.text = reserves.reserves!![position].name
        var description = reserves.reserves!![position].description
        if (description!!.length > 50) {
            description = description.substring(0, 47) + "..."
        }
        holder.textDescription.text = description

        val storageRef = FirebaseStorage.getInstance().getReference()
        val imageRef = storageRef.child("images/" + reserves.reserves!![position].id)
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(context)
                .load(uri)
                .into(holder.image)
        }.addOnFailureListener { exception ->
            // Обработка ошибок при загрузке изображения
        }
    }

    override fun getItemCount(): Int {
        return reserves.reserves?.size ?: 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var textName: TextView
        lateinit var textDescription: TextView
        lateinit var image: ImageView

        init {
            textName = itemView.findViewById(R.id.text_name)
            textDescription = itemView.findViewById(R.id.text_description)
            image = itemView.findViewById(R.id.imageView)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(reserve: Reserve)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(reserve: Reserve)
    }
}