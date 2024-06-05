package com.hoofit.app.editInfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.hoofit.app.HoofitApp
import com.hoofit.app.MainActivity
import com.hoofit.app.ui.TrailFragment
import com.hoofit.app.adapter.CoordinateAdapter
import com.hoofit.app.data.Coordinate
import com.hoofit.app.data.Reserve
import com.hoofit.app.data.Trail
import com.hoofit.app.databinding.FragmentEditTrailBinding

class EditTrailFragment : Fragment() {
    lateinit var binding: FragmentEditTrailBinding
    private var trail: Trail? = null
    lateinit var reserve: Reserve
    private lateinit var adapter: CoordinateAdapter
    private var isNewTrail = false
    private lateinit var reservesRef: DatabaseReference
    private lateinit var trailsRef: DatabaseReference
    private lateinit var trails: MutableList<Trail>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditTrailBinding.inflate(layoutInflater)
        initBundle()
        initDatabaseReferences()
        initData()
        initUI()
        return binding.root
    }

    private fun initBundle() {
        val bundle = arguments
        if (bundle != null) {
            trail = bundle.getSerializable("trail") as Trail?
            reserve = bundle.getSerializable("reserve") as Reserve
            if (trail == null) {
                trail = Trail()
                isNewTrail = true
            } else {
                binding.deleteButton.visibility = View.VISIBLE
            }
        }
    }

    private fun initDatabaseReferences() {
        reservesRef = FirebaseDatabase.getInstance().getReference("reserves")
        trailsRef = reserve.id?.let { reservesRef.child(it).child("trails") }!!
    }

    private fun initData() {

        trails = reserve.trails ?: mutableListOf()

        if (isNewTrail) {
            val trailId = reservesRef.push().key
            trail?.id = trailId
        }
        // Предположим, что у вас есть класс адаптера, который принимает Context и список координат
        adapter = trail?.coordinatesList?.let { coordinates ->
            context?.let { ctx ->
                CoordinateAdapter(ctx, coordinates)
            }
        } ?: CoordinateAdapter(requireContext(), mutableListOf())

    }

    private fun initUI() {
        binding.apply {
            editTextDescription.setText(trail?.description)
            editTextName.setText(trail?.name)
            editTextDifficulty.setText(trail?.difficulty)
            editTextLength.setText(trail?.length?.toString() ?: "")
            editTextTimeRequired.setText(trail?.timeRequired)

            saveButton.setOnClickListener { saveTrail() }

            deleteButton.setOnClickListener { deleteTrail() }

            buttonAdd.setOnClickListener { adapter.addCoordinate(Coordinate(0.0, 0.0)) }

            buttonRemove.setOnClickListener { adapter.removeCoordinate() }

            recyclerViewCoordinates.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                adapter = this@EditTrailFragment.adapter
            }
        }
    }

    private fun saveTrail() {
        val name = binding.editTextName.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val difficulty = binding.editTextDifficulty.text.toString().trim()
        val length = binding.editTextLength.text.toString().trim()
        val timeRequired = binding.editTextTimeRequired.text.toString().trim()
        val coordinates = adapter.getCoordinates()

        if (isValidInput(name, description, difficulty, length, timeRequired, coordinates)) {
            trail?.apply {
                this.name = name
                this.description = description
                this.difficulty = difficulty
                this.length = length.toDouble()
                this.timeRequired = timeRequired
                this.coordinatesList = coordinates
            }
            reserve.trails = trails

            if (isNewTrail) {
                trails.add(trail!!)
            }
            trailsRef.setValue(trails)

            HoofitApp.interestings.filter { it.trail == trail }.forEach { interesting ->
                interesting.trail = trail
                val interestingRef = FirebaseDatabase.getInstance().getReference("interesting")
                interesting.id?.let { interestingRef.child(it).setValue(interesting) }
            }

            Toast.makeText(context, "Тропа успешно добавлена", Toast.LENGTH_SHORT).show()

            val fragment = TrailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("reserve", reserve)
                }
            }
            val transaction = parentFragmentManager.beginTransaction()
            MainActivity.makeTransaction(transaction, fragment)
        }
    }

    fun isValidInput(
        name: String,
        description: String,
        difficulty: String,
        length: String,
        timeRequired: String,
        coordinates: List<Coordinate>
    ): Boolean {
        return when {
            name.isEmpty() -> {
                Toast.makeText(context, "Не введены данные в поле Имя", Toast.LENGTH_SHORT).show()
                false
            }
            description.isEmpty() -> {
                Toast.makeText(context, "Не введены данные в поле Описание", Toast.LENGTH_SHORT).show()
                false
            }
            difficulty.isEmpty() -> {
                Toast.makeText(context, "Не введены данные в поле Сложность", Toast.LENGTH_SHORT).show()
                false
            }
            length.isEmpty() -> {
                Toast.makeText(context, "Не введены данные в поле Длина", Toast.LENGTH_SHORT).show()
                false
            }
            timeRequired.isEmpty() -> {
                Toast.makeText(context, "Не введены данные в поле Время", Toast.LENGTH_SHORT).show()
                false
            }
            coordinates.size < 2 -> {
                Toast.makeText(context, "Добавьте координаты для тропы", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun deleteTrail() {
        trail?.let { reserve.trails!!.remove(it) }
        trailsRef.setValue(trails)
        HoofitApp.user!!.likedTrails.remove(trail)
        HoofitApp.interestings.filter { it.trail == trail }.forEach { interesting ->
            val interestingRef = FirebaseDatabase.getInstance().getReference("interesting")
            interesting.id?.let { interestingRef.child(it).removeValue() }
            HoofitApp.interestings.remove(interesting)
        }
        val users = FirebaseDatabase.getInstance().getReference("Users")
        HoofitApp.user!!.id?.let { users.child(it).child("likedTrails").setValue(HoofitApp.user!!.likedTrails) }

        val fragment = TrailFragment().apply {
            arguments = Bundle().apply {
                putSerializable("reserve", reserve)
            }
        }
        val transaction = parentFragmentManager.beginTransaction()
        MainActivity.makeTransaction(transaction, fragment)
    }
}
