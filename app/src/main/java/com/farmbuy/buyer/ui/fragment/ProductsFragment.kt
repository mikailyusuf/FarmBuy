package com.farmbuy.buyer.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.farmbuy.R
import com.farmbuy.adapters.OnClick
import com.farmbuy.adapters.OnUserClick
import com.farmbuy.adapters.OrdersAdapter
import com.farmbuy.adapters.ProductsAdapter
import com.farmbuy.datamodel.Products
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_products.*


class ProductsFragment : Fragment(), OnUserClick {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productsList: MutableList<Products>
    private var dbRef = Firebase.firestore.collection("Products")
    private  var doubleBackToExitPressedOnce = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {

            if (doubleBackToExitPressedOnce) {
                activity?.let { ActivityCompat.finishAffinity(it) }
            }
            doubleBackToExitPressedOnce = true

            Toast.makeText(activity, "Please Click BACK again to exit", Toast.LENGTH_SHORT).show()

                Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)}



    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        recyclerView = view.findViewById(R.id.recyclerview)
        productsList = mutableListOf()

        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
        }
        getProducts()
        recyclerView.setHasFixedSize(true)


    }



    private fun getProducts()
    {
        progressBar.visibility = View.VISIBLE
        dbRef.addSnapshotListener{value: QuerySnapshot?, error: FirebaseFirestoreException? ->
            error?.let {
                progressBar.visibility = View.INVISIBLE
            Toast.makeText(activity,"Sorry cant get Products at this time",Toast.LENGTH_SHORT).show()
            return@addSnapshotListener
        }

            value?.let {
                for(documents in value.documents)
                {
                    val products = documents.toObject<Products>()
                    if (products != null) {
                        productsList.add(products)
                        progressBar.visibility = View.INVISIBLE
                        val adapter = ProductsAdapter(
                            productsList,this)
                        recyclerView.adapter = adapter
                        // progressBar.visibility = View.GONE
                        adapter.notifyDataSetChanged()

                    }

                }
            }

        }
    }

    override fun onUserClick(products: Products, position: Int) {

        val bundle = Bundle().apply {
            putSerializable("product",products)
        }
        findNavController().navigate(R.id.action_productsFragment_to_orderActivity,bundle)
    }

}