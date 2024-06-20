package com.university.geekway.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.university.geekway.adapters.AdapterCityUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekway.adapters.AdapterCity
import com.university.geekway.databinding.FragmentMainBinding
import com.university.geekway.models.Cities
import java.lang.Exception

class MainFragment : Fragment() {

    //Объявление виджетов, текстов и кнопок
    private lateinit var citiesArrayList: ArrayList<Cities>
    private lateinit var adapterCity: AdapterCity
    private lateinit var binding: FragmentMainBinding
    private lateinit var adapterCityUser: AdapterCityUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(LayoutInflater.from(context), container, false)

        //Поле "Поиск города"
        binding.editCityName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterCityUser.filter!!.filter(s)
                }
                catch (e: Exception) {
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        //Загрузка и отображение городов
        loadCities()

        return binding.root
    }

    //Загрузка и отображение городов
    private fun loadCities() {
        citiesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Cities")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                citiesArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(Cities::class.java)
                    citiesArrayList.add(model!!)
                }
                adapterCityUser = AdapterCityUser(context!!, citiesArrayList)
                binding.citiesRv.adapter = adapterCityUser
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}