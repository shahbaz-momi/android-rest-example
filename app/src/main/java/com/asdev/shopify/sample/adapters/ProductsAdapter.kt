package com.asdev.shopify.sample.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.asdev.shopify.sample.R
import com.asdev.shopify.sample.objects.DCollection
import com.asdev.shopify.sample.objects.DProduct

class ProductsAdapter: RecyclerView.Adapter<ProductView>() {

    private var items = listOf<DProduct>()
    private var collection: DCollection? = null

    fun setItems(p: List<DProduct>, collection: DCollection? = null) {
        this.collection = collection
        if(items.isEmpty()) {
            items = p
            notifyItemRangeInserted(0, p.size)
        } else {
            items = p
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): ProductView {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.item_collection_detail, parent, false)
        return ProductView(layout)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ProductView, position: Int) {
        holder.bind(items[position], collection)
    }

}