package com.asdev.shopify.sample.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asdev.shopify.sample.R
import com.asdev.shopify.sample.objects.DCollection
import io.reactivex.subjects.Subject

class CollectionsAdapter(private val uiSubject: Subject<Pair<DCollection, View>>) : RecyclerView.Adapter<CollectionsView>() {

    private var collection = listOf<DCollection>()

    fun setItems(it: List<DCollection>) {
        if(collection.isEmpty()) {
            collection = it
            notifyItemRangeInserted(0, collection.size)
        } else {
            collection = it
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): CollectionsView {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.item_collection, parent, false)
        return CollectionsView(layout, uiSubject)
    }

    override fun getItemCount() = collection.size

    override fun onBindViewHolder(holder: CollectionsView, position: Int) {
        holder.bindToItem(collection[position])
    }

}