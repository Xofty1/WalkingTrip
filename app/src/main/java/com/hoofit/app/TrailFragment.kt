package com.hoofit.app

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hoofit.app.adapter.TrailAdapter
import com.hoofit.app.data.Reserve
import com.hoofit.app.data.Trail
import com.hoofit.app.databinding.FragmentTrailBinding
import com.hoofit.app.mainMenu.OnFragmentInteractionListener

class TrailFragment : Fragment() {
    private var _binding: FragmentTrailBinding? = null
    private val binding get() = _binding!!
    private var listener: OnFragmentInteractionListener? = null
    private var reserve: Reserve? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (context is OnFragmentInteractionListener) {
            context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleBundleArguments(arguments)

//        setupAddButton()
    }

    private fun handleBundleArguments(bundle: Bundle?) {
        val adapter: TrailAdapter
        if (HoofitApp.allTrails == null) {
            Toast.makeText(context, "Нет троп", Toast.LENGTH_SHORT).show()
        } else {
            val trails: MutableList<Trail>? = if (bundle != null) {
                reserve = bundle.getSerializable("reserve") as? Reserve
                if (reserve == null) {
                    bundle.getSerializable("trails") as MutableList<Trail>
                } else {
                    reserve!!.trails
                }
            } else {
                HoofitApp.allTrails
            }
            adapter = TrailAdapter(requireContext(), trails)
            if (reserve == null || !HoofitApp.user!!.admin) {
                binding.buttonAddTrail.visibility = View.INVISIBLE
            }
            setupRecyclerView(adapter)
//            if (HoofitApp.user!!.admin && reserve != null) {
//                setupItemLongClickListener(adapter)
//            }
            setupItemClickListener(adapter)
        }
    }

    private fun setupRecyclerView(adapter: TrailAdapter) {
        binding.listTrail.setHasFixedSize(true)
        binding.listTrail.layoutManager = LinearLayoutManager(context)
        binding.listTrail.adapter = adapter
    }

//    private fun setupItemLongClickListener(adapter: TrailAdapter) {
//        adapter.setOnItemLongClickListener { trail ->
//            val fragment = EditTrailFragment()
//            val bundle = Bundle()
//            bundle.putSerializable("reserve", reserve)
//            bundle.putSerializable("trail", trail)
//            fragment.arguments = bundle
//            val transaction = parentFragmentManager.beginTransaction()
//            MainActivity.makeTransaction(transaction, fragment)
//        }
//    }

    private fun setupItemClickListener(adapter: TrailAdapter) {
        adapter.setOnItemClickListener(object : TrailAdapter.OnItemClickListener {
            override fun onItemClick(trail: Trail) {
                val fragment = InfoTrailFragment()
                val bundle = Bundle().apply {
                    putSerializable("trail", trail)
                }
                fragment.arguments = bundle
                val transaction = parentFragmentManager.beginTransaction()
                MainActivity.makeTransaction(transaction, fragment)
            }
        })
    }

//    private fun setupAddButton() {
//        if (!HoofitApp.user!!.admin) {
//            binding.buttonAddTrail.visibility = View.INVISIBLE
//        }
//        binding.buttonAddTrail.setOnClickListener {
//            val fragment = EditTrailFragment()
//            val bundle = Bundle()
//            bundle.putSerializable("reserve", reserve)
//            fragment.arguments = bundle
//            val transaction = parentFragmentManager.beginTransaction()
//            MainActivity.makeTransaction(transaction, fragment)
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}