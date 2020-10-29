package com.farmbuy.farmer.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.farmbuy.utils.Internet
import com.farmbuy.R
import com.farmbuy.datamodel.Products
import com.farmbuy.datamodel.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_approve_order.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class ApproveOrderFragment : Fragment() {
    private val args: ApproveOrderFragmentArgs by navArgs()
    private var mchoice = ""
    private var dbRef = Firebase.firestore.collection("Orders")
    private var userRef = Firebase.firestore.collection("Users")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_approve_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val products = args.product
        val choice = resources.getStringArray(R.array.request)

        val id = products.buyerId

        units.text = products.units
        etProductName.text = products.productName
        Picasso.get().load(products.imageUrl).into(product_image)
        getUser(id)


        if (spinner != null) {
            val adapter =
                activity?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, choice) }
            adapter?.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
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
        }

        submit.setOnClickListener {

            if (activity?.let { it1 -> Internet.isNetworkConnected(it1) }!!)
            {
                val map = mutableMapOf<String, Any>()
                if (mchoice != "")
                {
                    map["status"] = mchoice
                    approveOrder(products,map)
                    Toast.makeText(activity,"Success",Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.farmerOrdersFragment)
                }
                else{
                    Toast.makeText(activity,"Please Approve or Decline Order",Toast.LENGTH_SHORT).show()
                }
            }

            else{
                Toast.makeText(activity,"Sorry You do not have an Internet Connection",Toast.LENGTH_LONG).show()

            }



        }

    }


    private fun approveOrder(products: Products, newProduct: Map<String, Any>) =
        CoroutineScope(Dispatchers.IO).launch {
            val productQuery = dbRef.whereEqualTo("productId", products.productId)
                .whereEqualTo("farmersId", FirebaseAuth.getInstance().currentUser?.uid)
                .get()
                .await()
            if (productQuery.documents.isNotEmpty()) {
                for (document in productQuery) {
                    try {
                        dbRef.document(document.id).set(newProduct, SetOptions.merge()).await()
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                activity,
                                e.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        activity,
                        "Sorry An Error Occured",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    private fun getUser(id:String) {
        userRef.whereEqualTo("id",id)
            .addSnapshotListener{ value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                error?.let {
                    Toast.makeText(activity,"Sorry can't get User at this time", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                value?.let {
                    for(documents in value.documents)
                    {
                        val user = documents.toObject<User>()
                        user?.let {

                            Picasso.get().load(user.profileImage).into(userImage)
                            username.text = user.username
                            email.text = user.email
                            phone.text = user.phone_number
                            address.text = user.address


                        }


                    }
                }


            }
    }

}