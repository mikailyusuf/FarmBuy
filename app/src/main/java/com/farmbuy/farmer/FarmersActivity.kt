package com.farmbuy.farmer

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.farmbuy.R
import com.farmbuy.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_farmers.*
import kotlin.system.exitProcess

class FarmersActivity : AppCompatActivity() {

    private var PRIVATE_MODE = 0
    private val PREF_NAME = "farm_buy"
    lateinit var sharedPref: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_farmers)

        sharedPref = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        bottomNavigationView.setupWithNavController(fragment_container.findNavController())
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


}