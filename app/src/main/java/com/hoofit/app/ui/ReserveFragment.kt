package com.hoofit.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hoofit.app.HoofitApp
import com.hoofit.app.MainActivity
import com.hoofit.app.adapter.ReserveAdapter
import com.hoofit.app.data.Reserve
import com.hoofit.app.databinding.FragmentReserveBinding
import com.hoofit.app.editInfo.EditReserveFragment


class ReserveFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentReserveBinding = FragmentReserveBinding.inflate(layoutInflater)
        if (HoofitApp.reserves!!.reserves == null) {
            Toast.makeText(context, "Нет заповедников", Toast.LENGTH_SHORT).show()
        } else {
            val adapter = context?.let { ReserveAdapter(it, HoofitApp.reserves!!) }
            adapter!!.setOnItemClickListener(object : ReserveAdapter.OnItemClickListener {
                override fun onItemClick(reserve: Reserve) {
                    val fragment = InfoReserveFragment()
                    val bundle = Bundle()
                    bundle.putSerializable("reserve", reserve)
                    fragment.arguments = bundle
                    val transaction = parentFragmentManager.beginTransaction()
                    MainActivity.makeTransaction(transaction, fragment)
                }
            })
            if (HoofitApp.user!!.admin) {
                adapter.setOnItemLongClickListener(object : ReserveAdapter.OnItemLongClickListener {
                    override fun onItemLongClick(reserve: Reserve) {
                        val fragment = EditReserveFragment()
                        val bundle = Bundle()
                        bundle.putSerializable("reserve", reserve)
                        fragment.arguments = bundle
                        val transaction = parentFragmentManager.beginTransaction()
                        MainActivity.makeTransaction(transaction, fragment)
                    }
                })
            }
            binding.listReserve.setHasFixedSize(true)
            binding.listReserve.setLayoutManager(LinearLayoutManager(context))
            binding.listReserve.setAdapter(adapter)
        }
        if (!HoofitApp.user!!.admin) {
            binding.buttonAddReserve.setVisibility(View.INVISIBLE)
        }
        binding.buttonAddReserve.setOnClickListener(View.OnClickListener {
            val fragment = EditReserveFragment()
            val transaction = parentFragmentManager.beginTransaction()
            MainActivity.makeTransaction(transaction, fragment)
        })
        return binding.getRoot()
    }
}