package com.hoofit.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.hoofit.app.MainActivity
import com.hoofit.app.data.User
import com.hoofit.app.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {
    private lateinit var auth: FirebaseAuth // для авторизации
    private lateinit var db: FirebaseDatabase
    private lateinit var users: DatabaseReference // таблица в БД
    private var binding: FragmentRegisterBinding? = null
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var name: String
    private lateinit var username: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
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

            buttonRegistration.setOnClickListener {
                email = editTextEmail.text.toString()
                if (email.isEmpty()) {
                    Toast.makeText(context, "Введите email", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener // Прерываем выполнение метода, если email пустой
                }
                password = editTextPassword.text.toString()
                if (password.isEmpty()) {
                    Toast.makeText(context, "Введите пароль", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener // Прерываем выполнение метода, если email пустой
                }
                if (password.length <= 5) {
                    Toast.makeText(context, "Пароль должен быть больше 5 символов", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener // Прерываем выполнение метода, если email пустой
                }
                name = editTextName.text.toString()
                if (name.isEmpty()) {
                    Toast.makeText(context, "Введите имя", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener // Прерываем выполнение метода, если email пустой
                }

                username = editTextUsername.text.toString()
                if (username.isEmpty()) {
                    Toast.makeText(context, "Введите логин", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener // Прерываем выполнение метода, если email пустой
                }
                if (username.length <= 4) {
                    Toast.makeText(context, "Логин должен быть длинее 4 символов", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener // Прерываем выполнение метода, если email пустой
                }
                if (username.length >= 15) {
                    Toast.makeText(context, "Логин должен быть короче 16 символов", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener // Прерываем выполнение метода, если email пустой
                }

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        val id = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnSuccessListener
                        val user = User(id, name, username, email, ArrayList(), false)
                        users.child(id)
                            .setValue(user)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Все круто", Toast.LENGTH_LONG).show()
                                startActivity(Intent(context, MainActivity::class.java))
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Что-то пошло не так", Toast.LENGTH_LONG).show()
                    }
            }

            textViewToLogin.setOnClickListener {
                val fragment = SignInFragment()
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
