package com.hoofit.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.hoofit.app.MainActivity
import com.hoofit.app.data.Interesting
import com.hoofit.app.databinding.FragmentInterestingBinding

class InterestingFragment : Fragment() {
    private lateinit var binding: FragmentInterestingBinding
    private var interesting: Interesting? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInterestingBinding.inflate(inflater, container, false)

        val bundle = arguments
        if (bundle != null) {
            interesting = bundle.getSerializable("interesting") as Interesting?

            interesting?.let { interesting ->
                binding.textDescription.text = interesting.description
                binding.textName.text = interesting.name
                binding.buttonoResource.setOnClickListener {
                    when (interesting.type) {
                        "RESERVE" -> {
                            val fragment = InfoReserveFragment()
                            val bundleFragment = Bundle().apply {
                                putSerializable("reserve", interesting.reserve)
                            }
                            fragment.arguments = bundleFragment
                            val transaction = parentFragmentManager.beginTransaction()
                            MainActivity.makeTransaction(transaction, fragment)
                        }
                        "TRAIL" -> {
                            val fragment = InfoTrailFragment()
                            val bundleFragment = Bundle().apply {
                                putSerializable("trail", interesting.trail)
                            }
                            fragment.arguments = bundleFragment
                            val transaction = parentFragmentManager.beginTransaction()
                            MainActivity.makeTransaction(transaction, fragment)
                        }
                        else -> {
                            val intent = Intent(Intent.ACTION_VIEW)
                            try {
                                val uri = Uri.parse(interesting.uri)
                                intent.data = uri

                                if (intent.resolveActivity(requireActivity().packageManager) != null) {
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(context, "No app found to handle the link", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: IllegalArgumentException) {
                                Toast.makeText(context, "Invalid link", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

        return binding.root
    }
}
