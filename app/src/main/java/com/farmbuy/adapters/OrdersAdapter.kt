package com.farmbuy.adapters

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.farmbuy.R
import com.farmbuy.datamodel.Products
import kotlinx.android.synthetic.main.orders_layout.view.*
import kotlinx.android.synthetic.main.product_layout.view.etLocation
import kotlinx.android.synthetic.main.product_layout.view.etProductName
import kotlinx.android.synthetic.main.product_layout.view.units


class OrdersAdapter(private val ItemsList: List<Products>, val listener: OnClick) :
    RecyclerView.Adapter<OrdersAdapter.RecyclerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {

        val items = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.orders_layout, parent
                , false
            )

        return RecyclerViewHolder(items)

    }

    override fun getItemCount(): Int {
        return ItemsList.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {

        holder.bind(ItemsList[position], listener)
    }

    class RecyclerViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        private val productName: TextView = itemview.etProductName
        private val image: ImageView = itemview.product_image
        private val location: TextView = itemview.etLocation
        private val units: TextView = itemview.units
        private val status:TextView = itemview.status


        fun bind(products: Products, listener: OnClick) {
            productName.text = products.productName
            location.text = products.farmersLoc
            units.text =  "${products.units} units available "
            if (products.status == "Approve")
            {
                status.text = products.status
                status.setTextColor(Color.parseColor("#50C155"))
            }
            else if (products.status == "Decline")
            {
                status.text = products.status
                status.setTextColor(Color.parseColor("#DD1717"))
            }
            else{
                status.text = "Pending"
                status.setTextColor(Color.parseColor("#CDDC39"))
            }


            val uri: Uri = Uri.parse(products.imageUrl)
            val draweeView =image as SimpleDraweeView
            draweeView.setImageURI(uri)



            itemView.setOnClickListener {
                listener.onUserClick(products, adapterPosition)
            }


        }
    }
}


interface OnClick {
    fun onUserClick(products: Products, position: Int)
}