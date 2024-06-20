package com.university.geekway.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekway.adapters.AdapterPlaceUser
import com.university.geekway.models.Places
import com.university.geekway.databinding.FragmentPlacesUserBinding
import java.lang.Exception

class PlacesUserFragment : Fragment {

    private lateinit var binding: FragmentPlacesUserBinding

    public companion object {
        private const val TAG = "PLACES_USER_TAG"
        public fun newInstance(categoryId: String, categoryname: String, uid: String): PlacesUserFragment {
            val fragment = PlacesUserFragment()
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("categoryname", categoryname)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var categoryname = ""
    private var uid = ""

    private lateinit var placesArrayList: ArrayList<Places>
    private lateinit var adapterPlaceUser: AdapterPlaceUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null) {
            categoryId = args.getString("categoryId")!!
            categoryname = args.getString("categoryname")!!
            uid = args.getString("uid")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPlacesUserBinding.inflate(LayoutInflater.from(context), container, false)

        //Поле "Поиск места"
        binding.editPlaceName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPlaceUser.filter!!.filter(s)
                }
                catch (e: Exception) {
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        if (categoryname == "Все") {
            loadAllPlaces()

        } else {
            loadCategorizedPlaces()
        }

        binding.editPlaceName.addTextChangedListener { object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPlaceUser.filter.filter(s)
                }
                catch (e: Exception) {
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        } }

        return binding.root
    }

    private fun loadAllPlaces() {
        placesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                placesArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(Places::class.java)
                    placesArrayList.add(model!!)
                }
                adapterPlaceUser = AdapterPlaceUser(context!!, placesArrayList)
                binding.placesRv.adapter = adapterPlaceUser

            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun loadCategorizedPlaces() {
        placesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Places")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                placesArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(Places::class.java)
                    placesArrayList.add(model!!)
                }
                adapterPlaceUser = AdapterPlaceUser(context!!, placesArrayList)
                binding.placesRv.adapter = adapterPlaceUser
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}