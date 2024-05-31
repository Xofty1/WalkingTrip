package com.hoofit.app.editInfo

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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.hoofit.app.HoofitApp
import com.hoofit.app.MainActivity
import com.hoofit.app.data.Reserve
import com.hoofit.app.databinding.FragmentEditReserveBinding
import com.hoofit.app.ui.ReserveFragment

class EditReserveFragment : Fragment() {
    private lateinit var binding: FragmentEditReserveBinding
    private val PICK_IMAGE_REQUEST = 1
    private val REQUEST_EXTERNAL_STORAGE = 1
    private var filePath: Uri? = null
    private var reserve: Reserve? = null
    private lateinit var storageReference: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEditReserveBinding.inflate(inflater, container, false)
        storageReference = FirebaseStorage.getInstance().reference
        val bundle = arguments

        if (bundle != null) {
            binding.deleteButton.visibility = View.VISIBLE
            reserve = bundle.getSerializable("reserve") as? Reserve
            Toast.makeText(activity, "Uploaded", Toast.LENGTH_SHORT).show()

            binding.editTextDescription.setText(reserve?.description)
            binding.editTextName.setText(reserve?.name)
            binding.uploadButton.setOnClickListener {
                val reservesRef = FirebaseDatabase.getInstance().getReference("reserves")
                val name = binding.editTextName.text.toString()
                val description = binding.editTextDescription.text.toString()
                updateData(name, description, reservesRef)
                val fragment = ReserveFragment()
                val transaction = parentFragmentManager.beginTransaction()
                MainActivity.makeTransaction(transaction, fragment)
            }
        } else {
            binding.uploadButton.setOnClickListener {
                val reservesRef = FirebaseDatabase.getInstance().getReference("reserves")
                val name = binding.editTextName.text.toString()
                val description = binding.editTextDescription.text.toString()
                addData(name, description, reservesRef)
            }
        }
        binding.deleteImageButton.setOnClickListener {
            filePath = null
            binding.constrWrapper.visibility = View.INVISIBLE
        }
        binding.deleteButton.setOnClickListener {
            val reservesRef = FirebaseDatabase.getInstance().getReference("reserves")
            reservesRef.child(reserve?.id!!).removeValue()
            HoofitApp.reserves!!.reserves!!.remove(reserve!!)

            for (interesting in HoofitApp.interestings) {
                if (interesting.reserve != null && interesting.reserve == reserve) {
                    Toast.makeText(activity, "Найдено", Toast.LENGTH_SHORT).show()
                    val interestingRef = FirebaseDatabase.getInstance().getReference("interesting")
                    interesting.id?.let { it1 -> interestingRef.child(it1).removeValue() }
                    HoofitApp.interestings.remove(interesting)
                    break
                }
            }
            for (interesting in HoofitApp.interestings) {
                for (trail in reserve?.trails!!) {
                    if (interesting.trail != null && interesting.trail == trail) {
                        Toast.makeText(activity, "Найдено", Toast.LENGTH_SHORT).show()
                        val interestingRef = FirebaseDatabase.getInstance().getReference("interesting")
                        interesting.id?.let { it1 -> interestingRef.child(it1).removeValue() }
                        HoofitApp.interestings.remove(interesting)
                        break
                    }
                }
            }
            reserve?.trails?.let { trails ->
                for (trail in trails) {
                    HoofitApp.allTrails?.remove(trail)
                    HoofitApp.user?.likedTrails?.remove(trail)

                    val users = FirebaseDatabase.getInstance().getReference("Users")
                    HoofitApp.user?.id?.let { userId ->
                        users.child(userId).child("likedTrails").setValue(HoofitApp.user?.likedTrails)
                    }
                }
            }

            val ref = storageReference.child("images/" + reserve?.id)
            ref.delete()
            val fragment = ReserveFragment()
            val transaction = parentFragmentManager.beginTransaction()
            MainActivity.makeTransaction(transaction, fragment)
        }

        binding.selectImageButton.setOnClickListener {
            checkStoragePermission()
        }

        return binding.root
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            binding.imageView.setImageURI(filePath)
            binding.constrWrapper.visibility = View.VISIBLE
            binding.uploadButton.visibility = View.VISIBLE
        }
    }

    private fun updateData(name: String, description: String, reservesRef: DatabaseReference) {
        reserve?.description = description
        reserve?.name = name
        reservesRef.child(reserve?.id!!).setValue(reserve)
        for (interesting in HoofitApp.interestings) {
            if (interesting.reserve != null && interesting.reserve == reserve) {
                Toast.makeText(activity, "Найдено", Toast.LENGTH_SHORT).show()
                interesting.reserve = reserve
                val interestingRef = FirebaseDatabase.getInstance().getReference("interesting")
                interesting.id?.let { interestingRef.child(it).setValue(interesting) }
                break
            }
        }

        filePath?.let {
            val progressDialog = ProgressDialog(activity)
            progressDialog.setTitle("Загрузка...")
            progressDialog.show()
            val ref = storageReference.child("images/" + reserve?.id)
            ref.delete()
            ref.putFile(it)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(activity, "Uploaded", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(activity, "Failed " + e.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun addData(name: String, description: String, reservesRef: DatabaseReference) {
        filePath?.let {
            val progressDialog = ProgressDialog(activity)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()

            val id = reservesRef.push().key // Получаем уникальный ключ

            val newReserve = Reserve(id, name, description, null)
            reservesRef.child(id!!).setValue(newReserve)

            val ref = storageReference.child("images/" + newReserve.id)
            ref.putFile(it)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(activity, "Uploaded", Toast.LENGTH_SHORT).show()
                    val fragment = ReserveFragment()
                    val transaction = parentFragmentManager.beginTransaction()
                    MainActivity.makeTransaction(transaction, fragment)
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(activity, "Failed " + e.message, Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(activity, "Выберите изображение", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            // Разрешение на чтение внешнего хранилища не предоставлено, запрашиваем его
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_EXTERNAL_STORAGE)
        } else {
            openFileChooser()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение на чтение внешнего хранилища предоставлено
                openFileChooser()
            } else {
                Toast.makeText(activity, "Нет доступа к файлам", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
