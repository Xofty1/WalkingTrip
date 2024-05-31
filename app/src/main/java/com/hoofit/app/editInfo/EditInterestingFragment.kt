package com.hoofit.app.editInfo

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.hoofit.app.HoofitApp
import com.hoofit.app.MainActivity
import android.Manifest
import com.hoofit.app.R
import com.hoofit.app.data.Interesting
import com.hoofit.app.databinding.FragmentEditInterestingBinding
import com.hoofit.app.ui.MainFragment

class EditInterestingFragment : Fragment() {
    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 1
        private const val PICK_IMAGE_REQUEST = 1
    }

    private lateinit var binding: FragmentEditInterestingBinding
    private lateinit var interesting: Interesting
    private var isNewInteresting = false
    private var filePath: Uri? = null
    private lateinit var storageReference: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditInterestingBinding.inflate(layoutInflater)
        val bundle = arguments
        storageReference = FirebaseStorage.getInstance().reference

        if (bundle != null) {
            interesting = bundle.getSerializable("interesting") as Interesting
            binding.editTextDescription.setText(interesting.description)
            binding.editTextName.setText(interesting.name)
            binding.editTextResource.setText(
                interesting.trail?.name ?: interesting.reserve?.name ?: interesting.uri
            )
        } else {
            interesting = Interesting()
            isNewInteresting = true
        }

        val interestingRef = FirebaseDatabase.getInstance().getReference("interesting")
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.options_array, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter

        if (!isNewInteresting) {
            binding.deleteButton.visibility = View.VISIBLE
        }

        binding.deleteButton.setOnClickListener {
            interesting.id?.let { it1 -> interestingRef.child(it1).removeValue() }
            HoofitApp.interestings.remove(interesting)
            val interest = storageReference.child("images/${interesting.id}")
            interest.delete()
            val fragment = MainFragment()
            val transaction = parentFragmentManager.beginTransaction()
            MainActivity.makeTransaction(transaction, fragment)
        }

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        interesting.type = "RESERVE"
                        binding.editTextResource.hint = "Введите название заповедника"
                    }
                    1 -> {
                        interesting.type = "TRAIL"
                        binding.editTextResource.hint = "Введите название тропы"
                    }
                    2 -> {
                        interesting.type = "RESOURCE"
                        binding.editTextResource.hint = "Введите ссылку"
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Действие, если ничего не выбрано (можно оставить пустым)
            }
        }

        if (!HoofitApp.user!!.admin) {
            binding.saveButton.visibility = View.INVISIBLE
        }

        binding.saveButton.setOnClickListener {
            val resource = binding.editTextResource.text.toString()
            var find = false

            when (interesting.type) {
                "RESERVE" -> {
                    for (reserve in HoofitApp.reserves!!.reserves!!) {
                        if (reserve.name == resource) {
                            interesting.reserve = reserve
                            interesting.trail = null
                            interesting.uri = null
                            find = true
                            break
                        }
                    }
                    if (!find) {
                        Toast.makeText(context, "Заповедника $resource нет", Toast.LENGTH_SHORT).show()
                    }
                }
                "TRAIL" -> {
                    for (trail in HoofitApp.allTrails!!) {
                        if (trail.name == resource) {
                            interesting.trail = trail
                            interesting.reserve = null
                            interesting.uri = null
                            find = true
                            break
                        }
                    }
                    if (!find) {
                        Toast.makeText(context, "Такой тропы нет", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    interesting.uri = resource
                    interesting.reserve = null
                    interesting.trail = null
                    find = true
                }
            }

            if (find) {
                var isCorrect = true
                interesting.name = binding.editTextName.text.toString()
                if (interesting.name!!.isEmpty()) {
                    Toast.makeText(context, "Введите название", Toast.LENGTH_SHORT).show()
                    isCorrect = false
                }
                interesting.description = binding.editTextDescription.text.toString()
                if (interesting.description!!.isEmpty()) {
                    Toast.makeText(context, "Введите описание", Toast.LENGTH_SHORT).show()
                    isCorrect = false
                }
                if (isCorrect) {
                    if (isNewInteresting) {
                        val id = interestingRef.push().key
                        interesting.id = id!!
                        HoofitApp.interestings.add(interesting)
                    }
                    interesting.id?.let { it1 -> interestingRef.child(it1).setValue(interesting) }

                    updateData()

                    val fragment = MainFragment()
                    val transaction = parentFragmentManager.beginTransaction()
                    MainActivity.makeTransaction(transaction, fragment)
                }
            }
        }

        binding.deleteImageButton.setOnClickListener {
            filePath = null
            binding.constrWrapper.visibility = View.INVISIBLE
        }

        binding.selectImageButton.setOnClickListener {
            checkStoragePermission()
        }

        return binding.root
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
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

    private fun updateData() {
        filePath?.let {
            val progressDialog = ProgressDialog(activity).apply {
                setTitle("Загрузка...")
                show()
            }
            val interest = storageReference.child("images/${interesting.id}")
            interest.delete()
            interest.putFile(it)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(activity, "Загружено", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(activity, "Failed ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
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
