package com.university.geekway

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler

class SplashActivity : BaseActivity() {

    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //Портретный режим
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        handler = Handler()

        handler.postDelayed( {
            //Проверка на авторизованного пользователя
            checkUserLogin()
            //Время сплэш-скрина - 1,5 секунды
        }, 1500)
    }
}