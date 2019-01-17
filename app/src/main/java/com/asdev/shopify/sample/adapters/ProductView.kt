package com.asdev.shopify.sample.adapters

import android.os.Build
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.asdev.shopify.sample.R
import com.asdev.shopify.sample.objects.DCollection
import com.asdev.shopify.sample.objects.DProduct
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import java.text.NumberFormat

class ProductView(v: View): RecyclerView.ViewHolder(v) {

    private val title = v.findViewById<TextView>(R.id.product_title)
    private val desc = v.findViewById<TextView>(R.id.product_desc)
    private val image = v.findViewById<ImageView>(R.id.product_img)
    private val price = v.findViewById<TextView>(R.id.product_price)
    private val collection = v.findViewById<TextView>(R.id.product_collection)
    private val quantity = v.findViewById<TextView>(R.id.product_quantity)
    private val styles = v.findViewById<ImageView>(R.id.product_styles)

    fun bind(p: DProduct, collection: DCollection? = null) {
        title.text = p.title

        if(p.variants?.size?:0 > 1) {
            styles.visibility = View.VISIBLE
        } else {
            styles.visibility = View.GONE
        }

        if (collection != null) {
            this.collection.visibility = View.VISIBLE
            this.collection.text = collection.title
        } else {
            this.collection.visibility = View.GONE
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            desc.text = Html.fromHtml(p.body, Html.FROM_HTML_MODE_COMPACT)
        } else {
            desc.text = Html.fromHtml(p.body)
        }

        val quantityNum = p.calculateQuantity()
        val overflowAmt = quantity.context.resources.getInteger(R.integer.quantity_overflow_amt)
        if(quantityNum >= overflowAmt) {
            quantity.text = quantity.context.getString(R.string.quantity_max_formatted, overflowAmt)
        } else {
            quantity.text = quantity.context.getString(R.string.quantity_formatted, p.calculateQuantity())
        }

        // is there isn't a consistent price, show "From $X.XX", else show "$X.XX"
        // use numberformat to format it properly
        val priceStr = NumberFormat.getCurrencyInstance().format(p.calculateMinPrice())

        if(!p.isConsistentPrice()) {
            price.text = price.context.getString(R.string.price_consistent_formatted, priceStr)
        } else {
            price.text = price.context.getString(R.string.price_inconsistent_formatted, priceStr)
        }

        if(p.images != null && p.images.isNotEmpty()) {
            Glide.with(image)
                .load(p.images[0])
                .apply(
                    RequestOptions()
                        .centerInside()
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                )
                .transition(DrawableTransitionOptions().crossFade(200))
                .into(image)
        }
    }

}