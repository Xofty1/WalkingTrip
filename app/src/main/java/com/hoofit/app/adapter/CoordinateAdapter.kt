package com.hoofit.app.adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.hoofit.app.R
import com.hoofit.app.data.Coordinate

class CoordinateAdapter(
    private val context: Context,
    private val coordinates: MutableList<Coordinate> = mutableListOf()
) : RecyclerView.Adapter<CoordinateAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.coordinate_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val coordinate = coordinates[position]
        holder.bind(coordinate)
    }

    override fun getItemCount(): Int = coordinates.size

    fun addCoordinate(coordinate: Coordinate) {
        coordinates.add(coordinate)
        notifyItemInserted(coordinates.size - 1)
    }

    fun removeCoordinate() {
        if (coordinates.isNotEmpty()) {
            coordinates.removeAt(coordinates.size - 1)
            notifyItemRemoved(coordinates.size)
        }
    }

    fun getCoordinates(): MutableList<Coordinate> = coordinates

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textLongitude: EditText = itemView.findViewById(R.id.editTextLongitude)
        private val textLatitude: EditText = itemView.findViewById(R.id.editTextLatitude)

        init {
            textLatitude.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    try {
                        coordinates[adapterPosition].latitude = s.toString().toDouble()
                    } catch (e: NumberFormatException) {
                        // Handle the case where the input is not a valid double
                    }
                }
            })

            textLongitude.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    try {
                        coordinates[adapterPosition].longitude = s.toString().toDouble()
                    } catch (e: NumberFormatException) {
                        // Handle the case where the input is not a valid double
                        Log.d("FFF", "Invalid longitude value")
                    }
                }
            })
        }

        fun bind(coordinate: Coordinate) {
            textLatitude.setText(coordinate.latitude.toString())
            textLongitude.setText(coordinate.longitude.toString())
        }
    }
}
