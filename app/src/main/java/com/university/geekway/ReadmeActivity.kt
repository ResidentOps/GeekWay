package com.university.geekway

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Button

class ReadmeActivity : BaseActivity() {

    private lateinit var btnReadmeOk: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_readme)

        //Прверка подключения к Интернету
        checkNetworkConnection()

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        btnReadmeOk = findViewById(R.id.buttonReadmeOk)

        btnReadmeOk.setOnClickListener {
            onBackPressed()
        }
    }
}