package com.university.geekway.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.university.geekway.*
import com.university.geekway.R
import com.university.geekway.models.Users

class ProfileFragment : Fragment() {

    //Объявление виджетов, текстов и кнопок
    private lateinit var userName: TextView
    private lateinit var userShowName: TextView
    private lateinit var userEmail: TextView
    private lateinit var userShowEmail: TextView
    private lateinit var btnCategoriesUser: Button
    private lateinit var btnPreferencesUser: Button
    private lateinit var btnFavoritesUser: Button
    private lateinit var btnSettingsUser: Button
    private lateinit var btnExitUser: Button
    private lateinit var textReadme: TextView

    //Объявление компонентов Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private lateinit var db: FirebaseDatabase
    var refUsers: DatabaseReference? = null
    var fbUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)

        //Инициализация виджетов, текстов и кнопок
        userName = root.findViewById(R.id.textNameUser)
        userShowName = root.findViewById(R.id.user_name)
        userEmail = root.findViewById(R.id.textEmailUser)
        userShowEmail = root.findViewById(R.id.user_email)
        btnCategoriesUser = root.findViewById(R.id.buttonCategoriesFragment)
        btnPreferencesUser = root.findViewById(R.id.buttonPreferencesFragment)
        btnFavoritesUser = root.findViewById(R.id.buttonFavoritesFragment)
        btnSettingsUser = root.findViewById(R.id.buttonUserSettingsFragment)
        btnExitUser = root.findViewById(R.id.buttonExitUserFragment)
        textReadme = root.findViewById(R.id.textReadmeFragment)

        //Инициализация компонентов Firebase
        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        db = FirebaseDatabase.getInstance()
        fbUser = auth.currentUser
        refUsers = db.reference.child("users").child(fbUser!!.uid)

        //Текст "О приложении" (открывает ReadmeFragment)
        textReadme.setOnClickListener {
            HOME.navController.navigate(R.id.action_profileFragment_to_readmeFragment)
        }

        //Кнопка "Выйти" (выходит из профиля и открывает EnterActivity)
        btnExitUser.setOnClickListener {
            auth.signOut()
            val enterActivity = Intent(this. requireContext(), EnterActivity::class.java)
            startActivity(enterActivity)
        }

        //Кнопка "Категории" (открывает Categories)
        btnCategoriesUser.setOnClickListener {
            HOME.navController.navigate(R.id.action_profileFragment_to_categoriesUserActivity)
        }

        //Кнопка "Предпочтения" (открывает PreferencesFragment)
        btnPreferencesUser.setOnClickListener {
            HOME.navController.navigate(R.id.action_profileFragment_to_preferencesFragment)
        }

        //Кнопка "Избранное" (открывает FavoritesFragment)
        btnFavoritesUser.setOnClickListener {
            HOME.navController.navigate(R.id.action_profileFragment_to_favoritesFragment)
        }

        //Кнопка "Настройки" (открывает SettingsFragment)
        btnSettingsUser.setOnClickListener {
            HOME.navController.navigate(R.id.action_profileFragment_to_settingsFragment)
        }

        //Отображение данных пользователя
        showData()

        return root
    }

    private fun showData() {
        refUsers!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user: Users? = p0.getValue(Users::class.java)
                    userShowName.text = user!!.getName()
                    userShowEmail.text = user!!.getEmail()
                }
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
}