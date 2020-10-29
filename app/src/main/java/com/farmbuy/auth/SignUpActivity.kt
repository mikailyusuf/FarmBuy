package com.farmbuy.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.farmbuy.utils.Internet
import com.farmbuy.R
import com.farmbuy.buyer.ui.BuyersActivity
import com.farmbuy.datamodel.User
import com.farmbuy.farmer.FarmersActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.activity_sign_up.image
import kotlinx.android.synthetic.main.activity_sign_up.progressBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class SignUpActivity : AppCompatActivity() {

    private var imageUri: Uri? = null
    private val imageRef = Firebase.storage.reference
    private var imageUrl = ""
    private var PRIVATE_MODE = 0
    private val PREF_NAME = "farm_buy"
    private var userRef = Firebase.firestore.collection("Users")
    private var mAuth: FirebaseAuth? = null
    var mchoice: String = ""
    lateinit var sharedPref: SharedPreferences
    private val REQUEST_CODE_IMAGE_PICK = 200
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        sharedPref = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        mAuth = FirebaseAuth.getInstance()
        val choice = resources.getStringArray(R.array.choice)

        if (spinner != null) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, choice)
            adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    mchoice = parent.getItemAtPosition(position).toString()

                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Another interface callback
                }
            }

            image.setOnClickListener {
                Intent(Intent.ACTION_GET_CONTENT).also {
                    it.type = "image/*"
                    startActivityForResult(
                        it,
                        REQUEST_CODE_IMAGE_PICK
                    )
                }
            }
        }

        signUpBtn.setOnClickListener {

            if (username.text.isNullOrEmpty()) {
                username.error = "Username is Required"
                username.requestFocus()
                return@setOnClickListener
            }
            if (email.text.isNullOrEmpty()) {
                email.error = "Email is Required"
                email.requestFocus()
                return@setOnClickListener
            }

            if (phone.text.isNullOrEmpty()) {
                phone.error = "Password is Required"
                phone.requestFocus()
                return@setOnClickListener
            }
            if (address.text.isNullOrEmpty()) {
                address.error = "Address id Required"
                address.requestFocus()
                return@setOnClickListener
            }
            if (password.text.isNullOrEmpty()) {
                password.error = "Password is required"
                password.requestFocus()
                return@setOnClickListener
            }

            if (Internet.isNetworkConnected(this)) {
                progressBar.visibility = View.VISIBLE
                val mUseranme = username.text?.trim().toString()
                val mEmail = email.text?.trim().toString()
                val mPassword = password?.text?.trim().toString()
                val address = address.text.toString()
                val phone_number = phone.text.toString()
                if (imageUrl != "" && mchoice != "") {
                    register(mEmail, mPassword, mUseranme, mchoice, phone_number, address, imageUrl)
                } else {
                    progressBar.visibility = View.INVISIBLE

                    Toast.makeText(this, "Please Uplaod Your Photo or Choose a User Type", Toast.LENGTH_LONG).show()

                }
            } else {
                Toast.makeText(
                    this,
                    "Sorry You do not have an Internet Connection",
                    Toast.LENGTH_LONG
                ).show()
                progressBar.visibility = View.INVISIBLE

            }


        }
    }

    private fun register(
        email: String,
        password: String,
        username: String,
        userType: String,
        phone_number: String,
        addess: String,
        profileUrl: String
    ) {
        progressBar.visibility = View.VISIBLE
        mAuth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener(this) {

            if (it.isSuccessful) {
                val id = FirebaseAuth.getInstance().currentUser?.uid
                val user = id?.let { it1 ->
                    User(
                        it1,
                        username,
                        userType,
                        email,
                        phone_number,
                        addess,
                        profileUrl
                    )
                }
                if (user != null) {
                    registerUserToDb(user)
                }
            } else {
                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun registerUserToDb(user: User) = CoroutineScope(Dispatchers.IO).launch {

        try {

            userRef.add(user).await()
            withContext(Dispatchers.Main)
            {
                progressBar.visibility = View.INVISIBLE
                if (user.userTpe == "Farmer") {
                    val editor = sharedPref.edit()
                    editor.putString(PREF_NAME, "farmer")
                    editor.apply()
                    val intent = Intent(this@SignUpActivity, FarmersActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                } else {
                    val editor = sharedPref.edit()
                    editor.putString(PREF_NAME, "buyer")
                    editor.apply()
                    val intent = Intent(this@SignUpActivity, BuyersActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
            }

        } catch (e: Exception) {
            progressBar.visibility = View.INVISIBLE

            Toast.makeText(this@SignUpActivity, e.message, Toast.LENGTH_SHORT).show()
        }
    }


    private fun uploadImage() {
        progressBar.visibility = View.VISIBLE
        signUpBtn.visibility = View.INVISIBLE

        if (imageUri != null) {
            progressBar.visibility = View.VISIBLE
            val ref = imageRef.child("uploads/" + UUID.randomUUID().toString())
            val uploadTask = ref.putFile(imageUri!!)

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    progressBar.visibility = View.INVISIBLE
                    signUpBtn.visibility = View.VISIBLE
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressBar.visibility = View.INVISIBLE
                    signUpBtn.visibility = View.VISIBLE
                    imageUrl = task.result.toString()

                } else {
                    // Handle failures
                }
            }.addOnFailureListener {
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()

            }
        } else {
            Toast.makeText(this, "Please Upload an Image", Toast.LENGTH_SHORT).show()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_IMAGE_PICK) {
            data?.data?.let {
                imageUri = it
                image.setImageURI(it)
                uploadImage()
                progressBar.visibility = View.INVISIBLE
            }
        }
    }
}