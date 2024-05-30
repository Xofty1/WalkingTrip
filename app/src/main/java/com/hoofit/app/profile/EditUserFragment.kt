package com.hoofit.app.profile

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.hoofit.app.HoofitApp
import com.hoofit.app.MainActivity
import com.hoofit.app.databinding.FragmentEditUserBinding

class EditUserFragment : Fragment() {
    private lateinit var binding: FragmentEditUserBinding
    private lateinit var user: FirebaseUser
    private var filePath: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    private val REQUEST_EXTERNAL_STORAGE = 1
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        user = bundle?.getParcelable<FirebaseUser>("user")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditUserBinding.inflate(inflater, container, false)
        binding.editTextName.setText(HoofitApp.user!!.name)
        storageReference = FirebaseStorage.getInstance().getReference()
        binding.textEmail.text = "Email: " + HoofitApp.user!!.email
        binding.editTextUsername.setText(HoofitApp.user!!.username)

        binding.selectImageButton.setOnClickListener {
            checkStoragePermission()
        }

        binding.textViewDeleteCurrent.setOnClickListener {
            val ref = storageReference.child("images/" + HoofitApp.user!!.id)
            ref.delete()
            Toast.makeText(activity, "Текущая аватарка удалена", Toast.LENGTH_SHORT).show()
        }

        binding.buttonSaveChanges.setOnClickListener {
            val name = binding.editTextName.text.toString()
            val username = binding.editTextUsername.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (name.isEmpty()) {
                Toast.makeText(context, "Введите имя", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (username.isEmpty()) {
                Toast.makeText(context, "Введите логин", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (username.length <= 4) {
                Toast.makeText(context, "Логин должен быть длинее 4 символов", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            if (username.length >= 15) {
                Toast.makeText(context, "Логин должен быть короче 16 символов", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            if (!password.isEmpty()) {
                changeUserPassword(user, password)
            }
            HoofitApp.user!!.username = username
            HoofitApp.user!!.name = name

            val users = FirebaseDatabase.getInstance().getReference("Users")
            HoofitApp.user!!.id?.let { it1 -> users.child(it1).setValue(HoofitApp.user) }

            if (filePath != null) {
                val progressDialog = ProgressDialog(activity)
                progressDialog.setTitle("Загрузка...")
                progressDialog.show()

                val ref = storageReference.child("images/" + HoofitApp.user!!.id)
                ref.delete()
                ref.putFile(filePath!!)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(activity, "Uploaded", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(activity, "Failed " + e.message, Toast.LENGTH_SHORT).show()
                    }

            }
            val fragment = SettingsFragment()
            val transaction = parentFragmentManager.beginTransaction()
            MainActivity.makeTransaction(transaction, fragment)
        }

        binding.editTextPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.editTextPassword.text.toString().isEmpty()) {
                    binding.textInputLayoutPassword.setHint("Введите пароль")
                }
            } else {
                binding.textInputLayoutPassword.setHint("")
            }
        }

        binding.deleteImageButton.setOnClickListener {
            filePath = null
            binding.constrWrapper.visibility = View.INVISIBLE
        }

        return binding.root
    }

    private fun changeUserPassword(user: FirebaseUser, newPassword: String) {
        user.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(context, "Failed to update password", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_EXTERNAL_STORAGE
            )
        } else {
            openFileChooser()
        }
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
            && data != null && data.data != null
        ) {
            filePath = data.data
            binding.imageView.setImageURI(filePath)
            binding.constrWrapper.visibility = View.VISIBLE
        }
    }
}
