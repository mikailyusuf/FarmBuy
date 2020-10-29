package com.farmbuy.buyer.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.farmbuy.R
import com.farmbuy.adapters.OnClick
import com.farmbuy.adapters.OnUserClick
import com.farmbuy.adapters.OrdersAdapter
import com.farmbuy.adapters.ProductsAdapter
import com.farmbuy.datamodel.Products
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_buyers_orders.*

class BuyersOrdersFragment : Fragment(),OnClick {
    private lateinit var recyclerView: RecyclerView
    private lateinit var productsList: MutableList<Products>
    var dbRef = Firebase.firestore.collection("Orders")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_buyers_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        productsList = mutableListOf()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
            setHasFixedSize(true)

        }

        getProducts()


    }

    private fun getProducts()
    {
        val buyerId = FirebaseAuth.getInstance().currentUser?.uid
        dbRef.whereEqualTo("buyerId",buyerId)
            .addSnapshotListener{ value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                error?.let {
                    Toast.makeText(activity,"Sorry cant get Products at this time", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                value?.let {
                    for(documents in value.documents)
                    {
                        val products = documents.toObject<Products>()
                        if (products != null) {
                            productsList.add(products)
                            val adapter = OrdersAdapter(productsList, this)
                            recyclerView.adapter = adapter
                            adapter.notifyDataSetChanged()
                        }
                        else{
                            noproduct.visibility = View.VISIBLE
                        }

                    }
                }
            }
    }

    override fun onUserClick(products: Products, position: Int) {

//        val bundle = Bundle().apply {
//            putSerializable("product",products)
//        }
//        findNavController().navigate(R.id.action_buyersOrdersFragment_to_orderActivity,bundle)

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(R.id.productsFragment)
        }
    }

}