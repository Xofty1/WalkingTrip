package com.hoofit.app.ui.profile

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.hoofit.app.AuthActivity
import com.hoofit.app.MainActivity
import com.hoofit.app.databinding.DialogTrailInfoBinding
import com.hoofit.app.databinding.FragmentSettingsBinding
import com.hoofit.app.databinding.RequestPasswordBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        mAuth = FirebaseAuth.getInstance()

        binding.buttonLogOut.setOnClickListener {
            showTrailInfoDialog()
        }

        binding.buttonEditData.setOnClickListener {
            val bindingPassword = RequestPasswordBinding.inflate(layoutInflater)
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(bindingPassword.root)

            val dialog = builder.create()

            bindingPassword.root.apply {
                scaleX = 0f
                scaleY = 0f
                animate()
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

            bindingPassword.editTextPassword.setOnFocusChangeListener { view, hasFocus ->
                if (!hasFocus) {
                    if (bindingPassword.editTextPassword.text.toString().isEmpty()) {
                        bindingPassword.textInputLayoutPassword.hint = "Введите пароль"
                    }
                } else {
                    bindingPassword.textInputLayoutPassword.hint = ""
                }
            }

            bindingPassword.buttonOk.setOnClickListener {
                val currentPassword = bindingPassword.editTextPassword.text.toString()
                val user = FirebaseAuth.getInstance().currentUser

                if (user != null) {
                    val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                    user.reauthenticate(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                dialog.dismiss()
                                val fragment = EditUserFragment()
                                val bundle = Bundle().apply {
                                    putParcelable("user", user)
                                }
                                fragment.arguments = bundle
                                val transaction = parentFragmentManager.beginTransaction()
                                MainActivity.makeTransaction(transaction, fragment)
                            } else {
                                Toast.makeText(context, "Invalid password", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "User not signed in", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return binding.root
    }

    private fun showTrailInfoDialog() {
        val bindingInfo = DialogTrailInfoBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(bindingInfo.root)

        val dialog = builder.create()

        bindingInfo.root.apply {
            scaleX = 0f
            scaleY = 0f
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .start()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        bindingInfo.buttonDismiss.setOnClickListener {
            mAuth.signOut()
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
        }

        bindingInfo.buttonToTrail.text = "Закрыть"
        bindingInfo.buttonDismiss.text = "Выйти"

        bindingInfo.buttonToTrail.setOnClickListener {
            dialog.dismiss()
        }
        bindingInfo.dialogTitle.text = "Вы точно хотите выйти?"
        bindingInfo.dialogMessage.text = ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
