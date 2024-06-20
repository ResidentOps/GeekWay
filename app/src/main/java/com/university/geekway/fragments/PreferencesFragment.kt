package com.university.geekway.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekway.*
import com.university.geekway.adapters.AdapterCategoryPreference
import com.university.geekway.databinding.FragmentPreferencesBinding
import com.university.geekway.models.Categories
import java.lang.Exception

class PreferencesFragment : Fragment() {

    private lateinit var binding: FragmentPreferencesBinding
    private lateinit var categoriesArrayList: ArrayList<Categories>
    private lateinit var adapterCategoryPreferences: AdapterCategoryPreference
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPreferencesBinding.inflate(LayoutInflater.from(context), container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        loadPereferences()

        binding.buttonCancelPreferences.setOnClickListener {
            HOME.navController.navigate(R.id.action_preferencesFragment_to_profileFragment)
        }

        //Поле "Поиск категории"
        binding.editCategoryName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterCategoryPreferences.filter!!.filter(s)
                }
                catch (e: Exception) {
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
        return binding.root
    }

    private fun loadPereferences() {
        categoriesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("users")
        ref.child(firebaseAuth.uid!!).child("Preferences")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    categoriesArrayList.clear()
                    for (ds in snapshot.children) {
                        val categoryId = "${ds.child("categoryId").value}"
                        val modelCategories = Categories()
                        modelCategories.id = categoryId
                        categoriesArrayList.add(modelCategories)
                    }
                    adapterCategoryPreferences = AdapterCategoryPreference(HOME, categoriesArrayList)
                    binding.preferencesRv.adapter = adapterCategoryPreferences
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}