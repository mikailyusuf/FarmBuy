package com.farmbuy.adapters

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
import kotlinx.android.synthetic.main.product_layout.view.*


class ProductsAdapter(private val ItemsList: List<Products>, val listener: OnUserClick) :
    RecyclerView.Adapter<ProductsAdapter.RecyclerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {

        val items = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.product_layout, parent
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


        fun bind(products: Products, listener: OnUserClick) {
            productName.text = products.productName
            location.text = products.farmersLoc
            val unit = "${products.units}  units available".toUpperCase()
            units.text = unit

            val uri: Uri = Uri.parse(products.imageUrl)
            val draweeView =image as SimpleDraweeView
            draweeView.setImageURI(uri)



            itemView.setOnClickListener {
                listener.onUserClick(products, adapterPosition)
            }


        }
    }
}


interface OnUserClick {
    fun onUserClick(products: Products, position: Int)
}