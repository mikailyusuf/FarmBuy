package com.farmbuy.farmer.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.facebook.drawee.view.SimpleDraweeView
import com.farmbuy.R
import com.farmbuy.adapters.ProductsAdapter
import com.farmbuy.datamodel.Products
import com.farmbuy.datamodel.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_farmer_profile.*


class FarmerProfileFragment : Fragment(R.layout.fragment_farmer_profile) {

    var dbRef = Firebase.firestore.collection("Users")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            dbRef.whereEqualTo("id", userId)
                .addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    error?.let {
                        Toast.makeText(
                            activity,
                            "Sorry cant get Products at this time",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@addSnapshotListener
                    }

                    value?.let {
                        for (documents in value.documents) {
                            val user = documents.toObject<User>()
                            if (user != null) {
                                username.text = user.username
                                email.text = user.email
                                phone.text = user.phone_number
                                address.text = user.address
                                if (user.profileImage != "") {

                                    val uri: Uri = Uri.parse(user.profileImage)
                                    val draweeView =profilePhoto as SimpleDraweeView
                                    draweeView.setImageURI(uri)


                                } else {
                                    profilePhoto.setImageResource(R.drawable.profile)
                                }


                            }

                        }
                    }
                }
        }
        else{
            Toast.makeText(activity,"Sorry cant load your  Profile at the Moment",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
           findNavController().navigate(R.id.farmersProductsFragment)
        }
    }
}