package com.hoofit.app.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.hoofit.app.HoofitApp
import com.hoofit.app.MainActivity
import com.hoofit.app.data.User
import com.hoofit.app.databinding.FragmentSignInBinding

class SignInFragment : Fragment() {
    private var binding: FragmentSignInBinding? = null
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var auth: FirebaseAuth // для авторизации
    private lateinit var db: FirebaseDatabase
    private lateinit var users: DatabaseReference // таблица в БД

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        users = db.getReference("Users")

        binding?.apply {
            editTextPassword.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    if (editTextPassword.text.toString().isEmpty()) {
                        textInputLayoutPassword.hint = "Введите пароль"
                    }
                } else {
                    textInputLayoutPassword.hint = ""
                }
            }

            buttonLogin.setOnClickListener {
                email = editTextEmail.text.toString()
                if (email.isEmpty()) {
                    Toast.makeText(context, "Введите email", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener // Прерываем выполнение метода, если email пустой
                }
                password = editTextPassword.text.toString()
                if (password.isEmpty()) {
                    Toast.makeText(context, "Введите пароль", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener // Прерываем выполнение метода, если пароль пустой
                }
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        // Успешная аутентификация
                        users.get().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val dataSnapshot = task.result
                                if (dataSnapshot.exists()) {
                                    for (snapshot in dataSnapshot.children) {
                                        val user = snapshot.getValue(User::class.java)
                                        if (user?.email == email) {
                                            HoofitApp.user = user
                                            break
                                        }
                                    }
                                    startActivity(Intent(context, MainActivity::class.java))
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        // Обработка ошибок аутентификации
                        when (e) {
                            is FirebaseAuthInvalidUserException -> {
                                Toast.makeText(context, "Неверный email или пароль", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(context, "Ошибка аутентификации: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            }

            textViewToRegistration.setOnClickListener {
                val fragment = RegisterFragment()
                val transaction = parentFragmentManager.beginTransaction()
                MainActivity.makeTransaction(transaction, fragment)
            }
        }
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
