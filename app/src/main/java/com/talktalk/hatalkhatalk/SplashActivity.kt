package com.talktalk.hatalkhatalk

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log

class SplashActivity : AppCompatActivity() {

    var SPLASH_TIME_OUT:Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val userData = PreferenceManager.getDefaultSharedPreferences(this)

        Handler().postDelayed({

            if(userData.getString("gender", "none") == "none"){

                startActivity(Intent(this, GenderActivity::class.java))
                finish()

            }else{

                Log.d("TAG", userData.getString("gender", "none"))

                startActivity(Intent(this, ChatActivity::class.java))
                finish()

            }

        }, SPLASH_TIME_OUT)

    }
}
