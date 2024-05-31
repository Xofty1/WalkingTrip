package com.hoofit.app.ui

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.hoofit.app.HoofitApp
import com.hoofit.app.MainActivity
import com.hoofit.app.databinding.FragmentProfileBinding
import com.hoofit.app.databinding.RequestPasswordBinding
import com.hoofit.app.ui.profile.EditUserFragment
import com.hoofit.app.ui.profile.HelpFragment
import com.hoofit.app.ui.profile.SettingsFragment
import java.io.Serializable

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/${HoofitApp.user?.id}")
        imageRef.downloadUrl
            .addOnSuccessListener { uri ->
                // Загружаем изображение в ImageView
                Glide.with(requireContext())
                    .load(uri)
                    .into(binding!!.imageView)
            }
            .addOnFailureListener { exception ->
                // Обработка ошибок при загрузке изображения
            }

        binding?.buttonSettings?.setOnClickListener {
            val fragment = SettingsFragment()
            val transaction = parentFragmentManager.beginTransaction()
            MainActivity.makeTransaction(transaction, fragment)
        }

        binding?.textViewEditData?.setOnClickListener {
            val bindingPassword = RequestPasswordBinding.inflate(layoutInflater)
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(bindingPassword.root)
            val dialog = builder.create()

            // Установка начального масштаба на 0
            bindingPassword.root.scaleX = 0f
            bindingPassword.root.scaleY = 0f
            bindingPassword.editTextPassword.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    if (binding?.textViewEditData?.text.toString().isEmpty()) {
                        bindingPassword.textInputLayoutPassword.hint = "Введите пароль"
                    }
                } else {
                    bindingPassword.textInputLayoutPassword.hint = ""
                }
            }

            dialog.setOnShowListener {
                // Анимация масштабирования при появлении диалогового окна
                bindingPassword.root.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .start()
            }

            dialog.show()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            bindingPassword.buttonDismiss.setOnClickListener {
                dialog.dismiss()
            }

            bindingPassword.buttonOk.setOnClickListener {
                val currentPassword = bindingPassword.editTextPassword.text.toString()
                // Check if the password is correct
                val user = FirebaseAuth.getInstance().currentUser
                user?.let {
                    val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                    user.reauthenticate(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Password is correct, navigate to EditUserFragment
                                dialog.dismiss()
                                val fragment = EditUserFragment()
                                val bundle = Bundle().apply {
                                    putParcelable("user", user)
                                }
                                fragment.arguments = bundle
                                val transaction = parentFragmentManager.beginTransaction()
                                MainActivity.makeTransaction(transaction, fragment)
                            } else {
                                Toast.makeText(requireContext(), "Invalid password", Toast.LENGTH_SHORT).show()
                            }
                        }
                } ?: run {
                    Toast.makeText(requireContext(), "User not signed in", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding?.textUsername?.text = HoofitApp.user!!.username

        binding?.buttonHelp?.setOnClickListener {
            val fragment = HelpFragment()
            val transaction = parentFragmentManager.beginTransaction()
            MainActivity.makeTransaction(transaction, fragment)
        }

        binding?.buttonSavedTrails?.setOnClickListener {
            if (HoofitApp.user!!.likedTrails.isEmpty()) {
                Toast.makeText(requireContext(), "У вас нет сохраненных троп", Toast.LENGTH_SHORT).show()
            } else {
                val fragment = TrailFragment()
                val bundleTrail = Bundle().apply {
                    putSerializable("trails", HoofitApp.user!!.likedTrails as Serializable)
                }
                fragment.arguments = bundleTrail

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
