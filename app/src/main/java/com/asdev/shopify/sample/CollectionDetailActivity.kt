package com.asdev.shopify.sample

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.animation.AnimationUtils
import com.asdev.shopify.sample.adapters.ProductsAdapter
import com.asdev.shopify.sample.objects.DCollection
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_collection_detail.*

class CollectionDetailActivity : AppCompatActivity() {

    private lateinit var collection: DCollection
    private val adapter = ProductsAdapter()
    private var disposables = CompositeDisposable()

    companion object {
        const val EXTRA_COLLECTION = "collection"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection_detail)

        val collection = intent?.extras?.getSerializable(EXTRA_COLLECTION) as? DCollection

        if(collection == null) {
            onBackPressed()
            return
        }

        this.collection = collection

        if(collection.body.trim().isEmpty()) {
            collection_desc.setTypeface(null, Typeface.ITALIC)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                collection_desc.text = Html.fromHtml(collection.body, Html.FROM_HTML_MODE_COMPACT)
            } else {
                collection_desc.text = Html.fromHtml(collection.body)
            }
        }

        Glide.with(applicationContext)
            .load(collection.imageUrl)
            .apply(
                RequestOptions()
                    .centerInside()
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
            )
            .transition(DrawableTransitionOptions().crossFade(200))
            .into(collection_image)

        toolbar.title = collection.title
        toolbar.setTitleTextColor(resources.getColor(R.color.colorAccent))
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }


        val lrecycler = recycler
        lrecycler.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        lrecycler.adapter = adapter
        lrecycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(applicationContext, R.anim.layout_anim_fade_in)

        swipe_refresh.setOnRefreshListener {
            load()
        }
        swipe_refresh.setColorSchemeColors(resources.getColor(R.color.colorAccent))
    }

    private fun load() {
        val subscription = ShopifyService.getProducts(collection.id)
            .toList()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onError = {
                    it.printStackTrace()
                    swipe_refresh.isRefreshing = false
                    Snackbar.make(swipe_refresh, R.string.error_collection_loading, Snackbar.LENGTH_LONG).show()
                },
                onSuccess = {
                    adapter.setItems(it, collection)
                    recycler.scheduleLayoutAnimation()
                    swipe_refresh.isRefreshing = false
                }
            )

        disposables.add(subscription)
    }

    override fun onBackPressed() {
        supportFinishAfterTransition()
    }

    override fun onResume() {
        super.onResume()

        // check if initial data loaded, if not, then load
        if(adapter.itemCount == 0) {
            swipe_refresh.isRefreshing = true
            load()
        }
    }

    override fun onPause() {
        super.onPause()

        disposables.dispose()
        disposables = CompositeDisposable()
    }
}
