package com.farmbuy.buyer.ui

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.farmbuy.R
import com.farmbuy.auth.LoginActivity
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_buyers.*

class BuyersActivity : AppCompatActivity() {
    private var PRIVATE_MODE = 0
    private val PREF_NAME = "farm_buy"
    private lateinit var  sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyers)

       sharedPref  = getSharedPreferences(PREF_NAME, PRIVATE_MODE)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        bottom_nav.setupWithNavController(nav_host_fragment_container.findNavController())

    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                val editor = sharedPref.edit()
                editor.clear()
                editor.apply()
                val it = Intent(this, LoginActivity::class.java)
                startActivity(it)
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

//    private var doubleBackToExitPressedOnce = false
//    override fun onBackPressed() {
//        if (doubleBackToExitPressedOnce) {
//
//            finishAffinity()
//
////            super.onBackPressed()
//        }
//
//        this.doubleBackToExitPressedOnce = true
//        Toast.makeText(this, "Please Click BACK again to exit", Toast.LENGTH_SHORT).show()
//
//        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
//    }

}