package com.farmbuy.ui

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.farmbuy.R
import com.farmbuy.auth.AuthActivity
import com.farmbuy.buyer.ui.BuyersActivity
import com.farmbuy.farmer.FarmersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class OnboardingActivity : AppCompatActivity() {

    private var PRIVATE_MODE = 0
    private val PREF_NAME = "farm_buy"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)


        val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        val fUser = FirebaseAuth.getInstance().currentUser
        if (fUser !== null) {

            if (sharedPref.getString(PREF_NAME,null) == "farmer") {
                val intent = Intent(this, FarmersActivity::class.java)
                startActivity(intent)
            }
            else if (sharedPref.getString(PREF_NAME, null) == "buyer") {

                val intent = Intent(this, BuyersActivity::class.java)
                startActivity(intent)
            } else {
                startActivity(Intent(this, AuthActivity::class.java))
            }

        } else {

            Handler().postDelayed({

                startActivity(Intent(this, AuthActivity::class.java))
            }, 2000)
        }
    }


}