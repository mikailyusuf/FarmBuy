package com.farmbuy.buyer.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.navArgs
import com.facebook.drawee.view.SimpleDraweeView
import com.farmbuy.utils.Internet
import com.farmbuy.R
import com.farmbuy.datamodel.Products
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_create_order.*
import kotlinx.android.synthetic.main.activity_order.*
import kotlinx.android.synthetic.main.activity_order.etLocation
import kotlinx.android.synthetic.main.activity_order.etPrice
import kotlinx.android.synthetic.main.activity_order.etProductName
import kotlinx.android.synthetic.main.activity_order.image
import kotlinx.android.synthetic.main.confirm_dialog.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class OrderActivity : AppCompatActivity() {
    private var dbRef = Firebase.firestore.collection("Orders")
    val args: OrderActivityArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)
        val products = args.product

        val uri: Uri = Uri.parse(products.imageUrl)
        val draweeView = image as SimpleDraweeView
        draweeView.setImageURI(uri)

        etProductName.text = products.productName
        etLocation.text = products.farmersLoc
        etPrice.text = products.price
        val unitsAvailable = "${products.units}  Units Available "
        units.text = unitsAvailable
        description.text = products.description
        phone.text = products.phone
        date.text = products.dateUploaded
        val id = FirebaseAuth.getInstance().currentUser?.uid
        if (id != null) {
            products.buyerId = id
        }

            order.setOnClickListener {

                if (Internet.isNetworkConnected(this))
                {
                    showDialog(products)
//                    sendOrderToDb(products)
//                    successDialog()
//                    val intent = Intent(this@OrderActivity,BuyersActivity::class.java)
//                    startActivity(intent)
                }

                else{
                    Toast.makeText(this,"Sorry You do not have an Internet Connection",Toast.LENGTH_LONG).show()
                }
            }

        phone.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(products.phone)))
            startActivity(intent)
        }
    }
    private fun sendOrderToDb(products: Products) = CoroutineScope(Dispatchers.IO).launch {
        try {
            dbRef.add(products).await()
            withContext(Dispatchers.Main) {
                successDialog()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main)
            {
                Toast.makeText(this@OrderActivity, e.message, Toast.LENGTH_SHORT).show()
                errorDialog()

            }

//            Toast.makeText(this@OrderActivity, e.message, Toast.LENGTH_SHORT).show()
        }
    }


    private fun showDialog(products: Products) {
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.confirm_dialog, null)
        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)

        //show dialog
        val mAlertDialog = mBuilder.show()
        //login button click of custom layout
        mDialogView.confirm.setOnClickListener {
            //When Successfull
            mAlertDialog.dismiss()
            sendOrderToDb(products)
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

        val intent = Intent(this@OrderActivity,BuyersActivity::class.java)
        startActivity(intent)
        mAlertDialog.dismiss()

    }


    private fun errorDialog() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.failure_layout, null)
        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)

        //show dialog
        val mAlertDialog = mBuilder.show()
    }
}
