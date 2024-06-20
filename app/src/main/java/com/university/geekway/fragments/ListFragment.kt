package com.university.geekway.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.university.geekway.models.Categories
import com.university.geekway.databinding.FragmentListBinding

class ListFragment : Fragment() {

    lateinit var binding: FragmentListBinding
    private lateinit var categoryArrayList: ArrayList<Categories>
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListBinding.inflate(LayoutInflater.from(context), container, false)

        setupWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        return binding.root
    }

    private fun setupWithViewPagerAdapter(viewpager: ViewPager) {
        viewPagerAdapter = ViewPagerAdapter(
            parentFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            requireContext()
        )
        categoryArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                val modelAll = Categories("01","Все", "", "", 1,"",false)
                categoryArrayList.add(modelAll)
                viewPagerAdapter.addFragment(
                    PlacesUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.categoryname}",
                        "${modelAll.uid}"
                    ), modelAll.categoryname
                )
                viewPagerAdapter.notifyDataSetChanged()
                for (ds in snapshot.children) {
                    val model = ds.getValue(Categories::class.java)
                    categoryArrayList.add(model!!)
                    viewPagerAdapter.addFragment(
                        PlacesUserFragment.newInstance(
                            "${model.id}",
                            "${model.categoryname}",
                            "${model.uid}"
                        ), model.categoryname
                    )
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        binding.viewPager.adapter = viewPagerAdapter
    }

    class ViewPagerAdapter(fm: FragmentManager, behavior: Int, context: Context): FragmentPagerAdapter(fm, behavior){
        private val fragmentsList: ArrayList<PlacesUserFragment> = ArrayList()
        private val fragmentTitleList: ArrayList<String> = ArrayList()
        private val context: Context
        init {
            this.context = context
        }
        override fun getCount(): Int {
            return fragmentsList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentsList[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }

        public fun addFragment(fragment: PlacesUserFragment, placename: String) {
            fragmentsList.add(fragment)
            fragmentTitleList.add(placename)
        }
    }
}