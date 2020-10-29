package com.farmbuy.farmer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.navArgs
import com.facebook.drawee.view.SimpleDraweeView
import com.farmbuy.utils.Internet
import com.farmbuy.R
import com.farmbuy.datamodel.Products
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_edit_farmer_order.*
import kotlinx.android.synthetic.main.activity_edit_farmer_order.etContact
import kotlinx.android.synthetic.main.activity_edit_farmer_order.etDescription
import kotlinx.android.synthetic.main.activity_edit_farmer_order.etLocation
import kotlinx.android.synthetic.main.activity_edit_farmer_order.etPrice
import kotlinx.android.synthetic.main.activity_edit_farmer_order.etProductName
import kotlinx.android.synthetic.main.activity_edit_farmer_order.etUnits
import kotlinx.android.synthetic.main.activity_edit_farmer_order.image
import kotlinx.android.synthetic.main.confirm_dialog.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*


private const val REQUEST_CODE_IMAGE_PICK = 100

class UpdateOrderActivity : AppCompatActivity() {
    private val args: UpdateOrderActivityArgs by navArgs()
    private var dbRef = Firebase.firestore.collection("Products")
    private var imageUri: Uri? = null
    private val imageRef = Firebase.storage.reference
    private var imageUrl = ""
    lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_farmer_order)

        progressBar = findViewById(R.id.progressBar)
        val products = args.product

        etProductName.setText(products.productName)
        etDescription.setText(products.description)
        etContact.setText(products.phone)
        etPrice.setText(products.price)
        etUnits.setText(products.units)
        etLocation.setText(products.farmersLoc)

        val uri: Uri = Uri.parse(products.imageUrl)
        val draweeView = image as SimpleDraweeView
        draweeView.setImageURI(uri)

        delete.setOnClickListener {

            if (Internet.isNetworkConnected(this))
            {
                delete.visibility = View.INVISIBLE
                deleteDialog(products)

            }
            else{

                Toast.makeText(this,"Sorry You do not have an Internet Connection",Toast.LENGTH_LONG).show()
            }

        }

        update.setOnClickListener {

            if (Internet.isNetworkConnected(this))
            {
                validateInputs()
                update.visibility = View.INVISIBLE
                val newProduct = newData()
                updateProduct(products, newProduct)

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

    private fun deleteProduct(products: Products) = CoroutineScope(Dispatchers.IO).launch {


        val productQuery = dbRef.whereEqualTo("productId", products.productId)
            .whereEqualTo("farmersId", FirebaseAuth.getInstance().currentUser?.uid)
            .get()
            .await()
        if (productQuery.documents.isNotEmpty()) {
            for (document in productQuery) {
                try {

                    dbRef.document(document.id).delete().await()
                    withContext(Dispatchers.Main)
                    {
                        delete.visibility = View.VISIBLE
                        val intent = Intent(this@UpdateOrderActivity, FarmersActivity::class.java)
                        startActivity(intent)
                        successDialog()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@UpdateOrderActivity,
                            e.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@UpdateOrderActivity,
                    "Sorry An Error Occured",
                    Toast.LENGTH_SHORT
                ).show()
            }
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

    private fun newData(): Map<String, Any> {
        val productName = etProductName.text.toString()
        val description = etDescription.text.toString()
        val price = etPrice.text.toString()
        val units = etUnits.text.toString()
        val address = etLocation.text.toString()
        val contact = etContact.text.toString()
        val map = mutableMapOf<String, Any>()

        if (productName.isNotEmpty()) {
            map["productName"] = productName
        }

        if (description.isNotEmpty()) {
            map["description"] = description
        }

        if (price.isNotEmpty()) {
            map["price"] = price
        }

        if (units.isNotEmpty()) {
            map["units"] = units
        }
        if (address.isNotEmpty()) {
            map["address"] = address
        }
        if (contact.isNotEmpty()) {
            map["contact"] = contact
        }
        if (imageUrl != "") {
            map["imageUrl"] = imageUrl
        }

        return map

    }

    private fun updateProduct(products: Products, newProduct: Map<String, Any>) =
        CoroutineScope(Dispatchers.IO).launch {

            val productQuery = dbRef.whereEqualTo("productId", products.productId)
                .whereEqualTo("farmersId", FirebaseAuth.getInstance().currentUser?.uid)
                .get()
                .await()
            if (productQuery.documents.isNotEmpty()) {
                for (document in productQuery) {
                    try {
                        dbRef.document(document.id).set(newProduct, SetOptions.merge()).await()
                        withContext(Dispatchers.Main)
                        {
                            update.visibility = View.VISIBLE
                            val intent = Intent(this@UpdateOrderActivity, FarmersActivity::class.java)
                            startActivity(intent)
                            successDialog()

                        }


                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@UpdateOrderActivity,
                                e.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@UpdateOrderActivity,
                        "Sorry An Error Occured",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


    private fun uploadImage() {
        if (imageUri != null) {
            progressBar.visibility = View.VISIBLE
            delete.visibility = View.INVISIBLE
            update.visibility = View.INVISIBLE
            val ref = imageRef.child("uploads/" + UUID.randomUUID().toString())
            val uploadTask = ref.putFile(imageUri!!)

            val urlTask =
                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation ref.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        progressBar.visibility = View.INVISIBLE
                        delete.visibility = View.VISIBLE
                        update.visibility = View.VISIBLE
                        imageUrl = task.result.toString()
                    } else {
                        // Handle failures
                    }
                }.addOnFailureListener {
                    delete.visibility = View.VISIBLE
                    update.visibility = View.VISIBLE
                    Toast.makeText(this, "Sorry an Error Occured", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.INVISIBLE
                }
        } else {
            Toast.makeText(this, "Please Upload an Image", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.INVISIBLE
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


    fun deleteDialog(products: Products) {
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.delete_dialog_layout, null)
        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)

        //show dialog
        val mAlertDialog = mBuilder.show()
        //login button click of custom layout
        mDialogView.confirm.setOnClickListener {
            deleteProduct(products)
        }



        mDialogView.create_btn.setOnClickListener {
            mAlertDialog?.dismiss()
        }

    }

    private fun successDialog() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.success_layout, null)
        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)

        //show dialog
        val mAlertDialog = mBuilder.show()
    }
}


