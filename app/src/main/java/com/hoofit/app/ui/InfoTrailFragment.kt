package com.hoofit.app.ui

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.FirebaseDatabase
import com.hoofit.app.HoofitApp
import com.hoofit.app.MainActivity
import com.hoofit.app.R
import com.hoofit.app.data.Trail
import com.hoofit.app.databinding.FragmentInfoTrailBinding

class InfoTrailFragment : Fragment() {
    private lateinit var binding: FragmentInfoTrailBinding
    private lateinit var viewModel: InfoTrailViewModel
    private var isLiked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(InfoTrailViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfoTrailBinding.inflate(inflater, container, false)

        val bundle = arguments
        if (bundle != null) {
            val trail = bundle.getSerializable("trail") as Trail?
            viewModel.setTrail(trail)
        }

        viewModel.trailLiveData.observe(viewLifecycleOwner) { trail ->
            trail?.let {
                binding.textName.text = it.name
                binding.textDescription.text = it.description
                binding.textDifficulty.text = "Cложность: ${it.difficulty}"
                binding.textLength.text = "Расстояние: ${it.length} км"
                binding.textTimeRequired.text = "Требуемое время: ${it.timeRequired}"
            }
        }

        binding.buttonToMap.setOnClickListener {
            viewModel.trailLiveData.value?.let { selectedTrail ->
                val fragment = MapFragment()
                val bundleToMap = Bundle().apply {
                    putSerializable("trail", selectedTrail)
                }
                fragment.arguments = bundleToMap
                val transaction = parentFragmentManager.beginTransaction()
                MainActivity.makeTransaction(transaction, fragment)
            }
        }

        val buttonLike = binding.buttonLike
        val currentId = viewModel.trailLiveData.value?.id
        var currentTrail: Trail? = null
        HoofitApp.user!!.likedTrails.forEach { trail ->
            if (trail.id == currentId) {
                currentTrail = trail
                buttonLike.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.orange),
                    PorterDuff.Mode.SRC_IN
                )
                isLiked = true
                return@forEach
            }
        }

        val users = FirebaseDatabase.getInstance().getReference("Users")
        val finalCurrentTrail = currentTrail
        binding.buttonLike.setOnClickListener {
            val colorAnimator: ValueAnimator
            if (isLiked) {
                colorAnimator = ValueAnimator.ofArgb(
                    requireContext().getColor(R.color.orange),
                    Color.parseColor("#F0F3FF")
                )
                HoofitApp.user!!.likedTrails.remove(finalCurrentTrail)
            } else {
                colorAnimator = ValueAnimator.ofArgb(
                    Color.parseColor("#F0F3FF"),
                    requireContext().getColor(R.color.orange)
                )
                viewModel.trailLiveData.value?.let { HoofitApp.user!!.likedTrails.add(it) }
            }
            colorAnimator.duration = 200
            colorAnimator.addUpdateListener { valueAnimator ->
                val color = valueAnimator.animatedValue as Int
                buttonLike.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
            isLiked = !isLiked
            colorAnimator.start()
            HoofitApp.user!!.id?.let { it1 -> users.child(it1).child("likedTrails").setValue(
                HoofitApp.user!!.likedTrails) }
        }

        return binding.root
    }
}
