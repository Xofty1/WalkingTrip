package com.hoofit.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.location.LocationRequest
import com.hoofit.app.databinding.ActivityMainBinding
import com.hoofit.app.mainMenu.OnFragmentInteractionListener
import com.hoofit.app.ui.MainFragment

class MainActivity : AppCompatActivity(), OnFragmentInteractionListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var transaction: FragmentTransaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        transaction = supportFragmentManager.beginTransaction()
        setContentView(binding.root)
        replaceFragment(MainFragment())
        binding.navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.main -> replaceFragment(MainFragment())
                R.id.map -> replaceFragment(MapFragment())
                R.id.profile -> replaceFragment(ProfileFragment())
            }
            true
        }
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    override fun onFragmentChanged(itemId: Int) {
        val navItemId = when (itemId) {
            R.id.main -> R.id.main
            R.id.map -> R.id.map
            R.id.profile -> R.id.profile
            else -> R.id.main
        }
        binding.navView.selectedItemId = navItemId
    }

    companion object {
        fun makeTransaction(transaction: FragmentTransaction, fragment: Fragment) {
            transaction.replace(R.id.fragment_container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            addToBackStack(null)
            commit()
        }
    }
}
