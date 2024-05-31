package com.hoofit.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.hoofit.app.HoofitApp
import com.hoofit.app.MainActivity
import com.hoofit.app.data.Reserve
import com.hoofit.app.databinding.FragmentInfoReserveBinding
import com.hoofit.app.editInfo.EditTrailFragment
import java.io.Serializable

class InfoReserveFragment : Fragment() {
    private lateinit var binding: FragmentInfoReserveBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInfoReserveBinding.inflate(inflater, container, false)
        val bundle = arguments
        if (bundle != null) {
            val reserve = bundle.getSerializable("reserve") as Reserve
            binding.buttonToTrails.setOnClickListener {
                if (reserve.trails == null) {
                    Toast.makeText(context, "У этого заповедника пока что нет троп", Toast.LENGTH_SHORT).show()
                    if (HoofitApp.user!!.admin) {
                        val bundleTrail = Bundle().apply {
                            putSerializable("reserve", reserve)
                        }
                        val fragment = EditTrailFragment().apply {
                            arguments = bundleTrail
                        }
                        val transaction = parentFragmentManager.beginTransaction()
                        MainActivity.makeTransaction(transaction, fragment)
                    }
                } else {
                    val bundleTrail = Bundle().apply {
                        putSerializable("trails", reserve.trails as Serializable)
                        putSerializable("reserve", reserve)
                    }
                    val trailFragment = TrailFragment().apply {
                        arguments = bundleTrail
                    }
                    val transaction = parentFragmentManager.beginTransaction()
                    MainActivity.makeTransaction(transaction, trailFragment)
                }
            }
            binding.textName.text = reserve.name
            binding.textDescription.text = reserve.description
        }
        return binding.root
    }
}
