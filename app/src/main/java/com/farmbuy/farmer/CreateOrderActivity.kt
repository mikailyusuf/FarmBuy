package com.farmbuy.farmer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.farmbuy.utils.Internet
import com.farmbuy.R
import com.farmbuy.datamodel.Products
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_create_order.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


private const val REQUEST_CODE_IMAGE_PICK = 100

class CreateOrderActivity : AppCompatActivity() {

    private var imageUri: Uri? = null
    private val imageRef = Firebase.storage.reference
    private var dbRef = Firebase.firestore.collection("Products")
    private var imageUrl = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_order)

        val farmerId = FirebaseAuth.getInstance().currentUser?.uid

        val productId = UUID.randomUUID().toString()

        val currentDate = SimpleDateFormat("EEE, d MMM yyyy ", Locale.getDefault()).format(Date())

        create_btn.setOnClickListener {
            validateInputs()

            if (etProductName.text.isEmpty()) {
                etProductName.error = "This field is required"
                etProductName.requestFocus()
                return@setOnClickListener
            }
            if (etContact.text.isEmpty()) {
                etContact.error = "This field is required"
                etContact.requestFocus()
                return@setOnClickListener
            }
            if (etDescription.text.isEmpty()) {
                etDescription.error = "This field is required"
                etDescription.requestFocus()
                return@setOnClickListener
            }
            if (etLocation.text.isEmpty()) {
                etLocation.error = "This field is required"
                etLocation.requestFocus()
                return@setOnClickListener
            }
            if (etPrice.text.isEmpty()) {
                etPrice.error = "This field is required"
                etPrice.requestFocus()
                return@setOnClickListener
            }
            if (etUnits.text.isEmpty()) {
                etUnits.error = "This field is required"
                etUnits.requestFocus()
                return@setOnClickListener
            }


            if (Internet.isNetworkConnected(this))
            {
                if (imageUri == null) {
                    Toast.makeText(this, "Please Upload the Product Image First", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val productName = etProductName.text.toString()
                    val description = etDescription.text.toString()
                    val price = etPrice.text.toString()
                    val units = etUnits.text.toString()
                    val address = etLocation.text.toString()
                    val contact = etContact.text.toString()

                    val products = farmerId?.let { id ->
                        Products(
                            productName, description, units, price, address, imageUrl,
                            id, currentDate, contact, "", productId
                        )
                    }
                    if (products != null) {
                        progressBar.visibility = View.VISIBLE
                        createOrder(products)

                        val intent = Intent(this,FarmersActivity::class.java)
                        startActivity(intent)
//                    findNavController().navigate(R.id.)
//                    it.findNavController().navigate(R.id.farmersProductsFragment)
                    }


                }
            }
            else{
                Toast.makeText(this,"Sorry You do not have an Internet Connection",Toast.LENGTH_LONG).show()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_IMAGE_PICK) {
            data?.data?.let {
                imageUri = it
                image.setImageURI(it)
                progressBar.visibility = View.VISIBLE
                uploadImage()
                progressBar.visibility = View.INVISIBLE
            }
        }
    }

   private fun createOrder(products: Products) = CoroutineScope(Dispatchers.IO).launch {

        try {

            dbRef.add(products).await()
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.INVISIBLE
                Toast.makeText(this@CreateOrderActivity, "SUCCESS", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this@CreateOrderActivity, e.message, Toast.LENGTH_SHORT).show()
        }

    }

    private fun validateInputs() {
        if (etProductName.text.isEmpty()) {
            etProductName.error = "This field is required"
        }
        if (etContact.text.isEmpty()) {
            etContact.error = "This field is required"
        }
        if (etDescription.text.isEmpty()) {
            etDescription.error = "This field is required"
        }
        if (etLocation.text.isEmpty()) {
            etLocation.error = "This field is required"
        }
        if (etPrice.text.isEmpty()) {
            etPrice.error = "This field is required"
        }
        if (etUnits.text.isEmpty()) {
            etUnits.error = "This field is required"
        }

    }


    private fun uploadImage() {
        if (imageUri != null) {
            progressBar.visibility = View.VISIBLE
            create_btn.visibility = View.INVISIBLE
            val ref = imageRef.child("uploads/" + UUID.randomUUID().toString())
            val uploadTask = ref.putFile(imageUri!!)

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    progressBar.visibility = View.INVISIBLE
                    create_btn.visibility = View.VISIBLE


                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressBar.visibility = View.INVISIBLE
                    create_btn.visibility = View.VISIBLE


                    imageUrl = task.result.toString()
                } else {
                    // Handle failures
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Sorry an Error Occured", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please Upload an Image", Toast.LENGTH_SHORT).show()

        }
    }

}