package com.asdev.shopify.sample.adapters

import android.os.Build
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.asdev.shopify.sample.R
import com.asdev.shopify.sample.objects.DCollection
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import io.reactivex.subjects.Subject

class CollectionsView(v: View, private val uiSubject: Subject<Pair<DCollection, View>>): RecyclerView.ViewHolder(v) {

    private val title = v.findViewById<TextView>(R.id.item_title)
    private val desc = v.findViewById<TextView>(R.id.item_desc)
    private val image = v.findViewById<ImageView>(R.id.item_image)

    fun bindToItem(collection: DCollection) {
        title.text = collection.title
        if(collection.body.trim().isEmpty()) {
            desc.visibility = View.GONE
        } else {
            desc.visibility = View.VISIBLE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                desc.text = Html.fromHtml(collection.body, Html.FROM_HTML_MODE_COMPACT)
            } else {
                desc.text = Html.fromHtml(collection.body)
            }
        }

        Glide.with(image)
            .load(collection.imageUrl)
            .apply(
                RequestOptions()
                    .centerInside()
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
            )
            .transition(DrawableTransitionOptions().crossFade(200))
            .into(image)

        itemView.setOnClickListener {
            uiSubject.onNext(collection to image)
        }
    }
}