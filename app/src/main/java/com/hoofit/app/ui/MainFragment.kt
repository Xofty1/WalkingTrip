package com.hoofit.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import com.hoofit.app.HoofitApp
import com.hoofit.app.MainActivity
import com.hoofit.app.R
import com.hoofit.app.adapter.InterestingAdapter
import com.hoofit.app.data.Interesting
import com.hoofit.app.data.Reserve
import com.hoofit.app.databinding.FragmentMainBinding
import com.hoofit.app.editInfo.EditInterestingFragment
import com.hoofit.app.editInfo.EditReserveFragment

class MainFragment : Fragment() {
    private var binding: FragmentMainBinding? = null
    private lateinit var fTrans: FragmentTransaction

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        val reserves: CardView = binding!!.root.findViewById(R.id.reserves)
        val trails: CardView = binding!!.root.findViewById(R.id.trails)

        if (!HoofitApp.user?.admin!!) {
            binding!!.buttonAddInteresting.visibility = View.INVISIBLE
        }
        binding!!.buttonAddInteresting.setOnClickListener {
            val fragment = EditInterestingFragment()
            val transaction = parentFragmentManager.beginTransaction()
            MainActivity.makeTransaction(transaction, fragment)
        }
        reserves.setOnClickListener {
            fTrans = parentFragmentManager.beginTransaction()
            fTrans.replace(R.id.fragment_container, ReserveFragment())
            fTrans.addToBackStack(null)
            fTrans.commit()
        }
        trails.setOnClickListener {
            fTrans = parentFragmentManager.beginTransaction()
            fTrans.replace(R.id.fragment_container, TrailFragment())
            fTrans.addToBackStack(null)
            fTrans.commit()
        }
        val adapter = context?.let { InterestingAdapter(it, reverseList(HoofitApp.interestings)) }
        adapter!!.setOnItemClickListener(object : InterestingAdapter.OnItemClickListener {
            override fun onItemClick(interesting: Interesting) {
                val fragment = InterestingFragment()
                val bundle = Bundle().apply {
                    putSerializable("interesting", interesting)
                }
                fragment.arguments = bundle

                val transaction = parentFragmentManager.beginTransaction()
                MainActivity.makeTransaction(transaction, fragment)
            }
        })
        if (HoofitApp.user!!.admin) {
            adapter.setOnItemLongClickListener(object : InterestingAdapter.OnItemLongClickListener {

                override fun onItemLongClick(interesting: Interesting) {
                    val fragment = EditInterestingFragment()
                    val bundle = Bundle().apply {
                        putSerializable("interesting", interesting)
                    }
                    fragment.arguments = bundle

                    val transaction = parentFragmentManager.beginTransaction()
                    MainActivity.makeTransaction(transaction, fragment)
                }
            })
        }
        else{
            adapter.setOnItemLongClickListener(object : InterestingAdapter.OnItemLongClickListener {

                override fun onItemLongClick(interesting: Interesting) {
                    val fragment = EditInterestingFragment()
                    val bundle = Bundle().apply {
                        putSerializable("interesting", interesting)
                    }
                    fragment.arguments = bundle

                    val transaction = parentFragmentManager.beginTransaction()
                    MainActivity.makeTransaction(transaction, fragment)
                }
            })
        }
        binding!!.recyclerViewInteresting.adapter = adapter
        binding!!.recyclerViewInteresting.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 2)
        }
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        fun reverseList(list: List<Interesting>): MutableList<Interesting> {
            return list.asReversed().toMutableList()
        }

    }
}
