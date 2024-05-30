package com.hoofit.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hoofit.app.data.Interesting
import com.hoofit.app.data.Reserve
import com.hoofit.app.data.ReserveData
import com.hoofit.app.data.Trail
import com.hoofit.app.data.User
import com.hoofit.app.auth.RegisterFragment

class AuthActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    companion object {
        private var isPersistenceEnabled = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
//        replaceFragment(LoaderFragment())
        mAuth = FirebaseAuth.getInstance()

        if (!isPersistenceEnabled) {
            database.setPersistenceEnabled(true)
            isPersistenceEnabled = true
        }

        val reserveRef = database.getReference("reserves")
        val interestingRef = database.getReference("interesting")
        val rev = mutableListOf<Reserve>()
        HoofitApp.reserves = ReserveData()
        HoofitApp.interestings = mutableListOf()

        reserveRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                if (dataSnapshot.exists()) {
                    val trails = mutableListOf<Trail>()
                    for (snapshot in dataSnapshot.children) {
                        val reserve = snapshot.getValue(Reserve::class.java)
                        reserve?.let {
                            it.trails?.let { trailsList ->
                                trails.addAll(trailsList)
                            }
                            rev.add(it)
                        }
                    }
                    HoofitApp.reserves!!.reserves = rev
                    HoofitApp.allTrails = trails
                } else {
                    Toast.makeText(this, "Троп пока что нет", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Проверьте подключение к Интернету", Toast.LENGTH_SHORT).show()
            }
        }

        reserveRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val rev = mutableListOf<Reserve>()
                    val trails = mutableListOf<Trail>()
                    for (snapshot in dataSnapshot.children) {
                        val reserve = snapshot.getValue(Reserve::class.java)
                        reserve?.let {
                            it.trails?.let { trailsList ->
                                trails.addAll(trailsList)
                            }
                            rev.add(it)
                        }
                    }
                    HoofitApp.reserves!!.reserves = rev
                    HoofitApp.allTrails = trails
                } else {
                    Toast.makeText(this@AuthActivity, "Троп пока что нет", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@AuthActivity, "Ошибка при получении данных: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })

        interestingRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                if (dataSnapshot.exists()) {
                    val interestings = mutableListOf<Interesting>()
                    for (snapshot in dataSnapshot.children) {
                        val interesting = snapshot.getValue(Interesting::class.java)
                        interesting?.let {
                            interestings.add(it)
                        }
                    }
                    HoofitApp.interestings = interestings
                } else {
                    Toast.makeText(this, "Новостей пока нет", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Проверьте подключение к Интернету", Toast.LENGTH_SHORT).show()
            }
        }

        interestingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val interestings = mutableListOf<Interesting>()
                    for (snapshot in dataSnapshot.children) {
                        val interesting = snapshot.getValue(Interesting::class.java)
                        interesting?.let {
                            interestings.add(it)
                        }
                    }
                    HoofitApp.interestings = interestings
                } else {
                    Toast.makeText(this@AuthActivity, "Новостей пока что нет", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@AuthActivity, "Ошибка при получении данных: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    override fun onStart() {
        super.onStart()
        val user = mAuth.currentUser

        if (user != null) {
            val userId = user.uid
            val userRef = database.getReference("Users").child(userId)
            val adminRef = userRef.child("admin")

            adminRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (HoofitApp.user == null) HoofitApp.user = User()
                        HoofitApp.user!!.admin = dataSnapshot.getValue(Boolean::class.java) ?: false
                        Toast.makeText(this@AuthActivity, "Admin status updated", Toast.LENGTH_SHORT).show()
                    } else {
                        HoofitApp.user!!.admin = false
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("FirebaseDatabase", "Failed to read admin field", databaseError.toException())
                }
            })

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        HoofitApp.user = dataSnapshot.getValue(User::class.java) ?: User()
                        Toast.makeText(this@AuthActivity, "Email ${HoofitApp.user!!.email}", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                        finish()
                    } else {
                        replaceFragment(RegisterFragment())
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("FirebaseDatabase", "Failed to read user data", databaseError.toException())
                }
            })
        } else {
            replaceFragment(RegisterFragment())
        }
    }
}
