package com.asdev.shopify.sample

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.view.animation.AnimationUtils
import com.androidnetworking.AndroidNetworking
import com.asdev.shopify.sample.adapters.CollectionsAdapter
import com.asdev.shopify.sample.objects.DCollection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val uiSubject: Subject<Pair<DCollection, View>> = PublishSubject.create<Pair<DCollection, View>>()
    private val adapter = CollectionsAdapter(uiSubject)
    private var disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // setup android networking
        AndroidNetworking.initialize(applicationContext)

        // setup the recycler view
        val lrecycler = recycler
        lrecycler.layoutManager = GridLayoutManager(applicationContext, 2, GridLayoutManager.VERTICAL, false)
        lrecycler.adapter = adapter
        lrecycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(applicationContext, R.anim.layout_anim_fade_in)

        // setup swipe down refresh
        val refreshLayout = swipe_refresh
        refreshLayout.setOnRefreshListener {
            onRefresh()
        }
        refreshLayout.setColorSchemeResources(R.color.colorAccent)

    }

    override fun onResume() {
        super.onResume()

        // check if adapter has items
        // could get cancelled due to user leaving the app
        if(adapter.itemCount == 0) {
            // fire a refresh
            swipe_refresh.isRefreshing = true
            onRefresh()
        }

        // bind ui touch event subject
        val subscription = uiSubject
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    launchCollection(it.first, it.second)
                })

        disposables.add(subscription)
    }

    private fun launchCollection(collection: DCollection, transitionView: View) {
        val intent = Intent(this, CollectionDetailActivity::class.java)
        intent.putExtra(CollectionDetailActivity.EXTRA_COLLECTION, collection)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, transitionView, getString(R.string.collection_image_transition))

        startActivity(intent, options.toBundle())
    }

    private fun onRefresh() {
        // get the global list of collections
        val collections = ShopifyService.getCollections()

        val disposable = collections
            .toList()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onError = {
                    it.printStackTrace()
                    Snackbar.make(swipe_refresh, R.string.error_collection_loading, Snackbar.LENGTH_LONG).show()
                    swipe_refresh.isRefreshing = false
                },
                onSuccess = {
                    adapter.setItems(it)
                    recycler.scheduleLayoutAnimation()
                    swipe_refresh.isRefreshing = false
                }
            )
         disposables.add(disposable)
    }

    override fun onPause() {
        super.onPause()

        // dispose everything
        disposables.dispose()
        disposables = CompositeDisposable()
    }
}
