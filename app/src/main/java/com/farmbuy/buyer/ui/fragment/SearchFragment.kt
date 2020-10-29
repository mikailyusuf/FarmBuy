package com.farmbuy.buyer.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.farmbuy.R
import com.farmbuy.adapters.OnUserClick
import com.farmbuy.adapters.ProductsAdapter
import com.farmbuy.datamodel.Products
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchFragment : Fragment(), OnUserClick {
    private lateinit var recyclerView: RecyclerView
    private lateinit var productsList: MutableList<Products>
    var dbRef = Firebase.firestore.collection("Products")
    lateinit var progressBar:ProgressBar


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerview)
        productsList = mutableListOf()
        progressBar = view.findViewById(R.id.progressBar)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
        }

        recyclerView.setHasFixedSize(true)

        var job: Job? = null


        searchQuery.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(200)
                editable?.let {
                    if(editable.toString().isNotEmpty()) {
                        getProducts(editable.toString())


                    }
                    else{
                        progressBar.visibility = View.INVISIBLE
                    }
                }
            }
        }



    }


    private fun getProducts(searchText:String)
    {
        progressBar.visibility = View.VISIBLE
        if (searchText.isNotEmpty()) {
            progressBar.visibility = View.VISIBLE
            dbRef.whereEqualTo("productName", searchText)
                .addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    error?.let {
                        progressBar.visibility = View.INVISIBLE
                        Toast.makeText(
                            activity,
                            "Sorry cant get Products at this time",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@addSnapshotListener
                    }

                    value?.let {
                        for (documents in value.documents) {
                            val products = documents.toObject<Products>()
                            if (products != null) {
                                productsList.clear()
                                productsList.add(products)
                                progressBar.visibility = View.INVISIBLE
                                val adapter = ProductsAdapter(
                                    productsList,
                                    this
                                )
                                recyclerView.adapter = adapter

                                progressBar.visibility = View.INVISIBLE

                                adapter.notifyDataSetChanged()
                            }
                            else{
                                progressBar.visibility = View.INVISIBLE

                            }
                        }
                    }
                }
        }
        else{
            progressBar.visibility = View.INVISIBLE

        }



    }

    override fun onUserClick(products: Products, position: Int) {
        val bundle = Bundle().apply {
            putSerializable("product",products)
        }
        findNavController().navigate(R.id.action_searchFragment_to_orderActivity,bundle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(R.id.productsFragment)
        }
    }
}