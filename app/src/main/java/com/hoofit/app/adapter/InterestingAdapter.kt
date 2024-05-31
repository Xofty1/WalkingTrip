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
import com.hoofit.app.data.Interesting
import com.hoofit.app.data.Trail

class InterestingAdapter(
    private val context: Context,
    private val interestings: List<Interesting>
) : RecyclerView.Adapter<InterestingAdapter.ViewHolder>() {

    private var onItemLongClickListener: OnItemLongClickListener? = null
    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        this.onItemLongClickListener = listener
    }

    fun setOnItemClickListener(itemClickListener: OnItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.interesting_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val interesting = interestings[position]

        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClick(interesting)
        }

            holder.itemView.setOnLongClickListener {
                onItemLongClickListener?.onItemLongClick(interesting)
                true
            }

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/${interesting.id}")
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(context)
                .load(uri)
                .into(holder.image)
        }.addOnFailureListener { exception ->
            // Обработка ошибок при загрузке изображения
        }

        var description = interesting.description
        if (description!!.length > 30) {
            description = description.substring(0, 27) + "..."
        }

        holder.textName.text = interesting.name
        holder.textDescription.text = description
    }

    override fun getItemCount(): Int {
        return interestings.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.textName)
        val textDescription: TextView = itemView.findViewById(R.id.textDescription)
        val image: ImageView = itemView.findViewById(R.id.image)
    }

    interface OnItemClickListener {
        fun onItemClick(interesting: Interesting)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(interesting: Interesting)
    }
}
