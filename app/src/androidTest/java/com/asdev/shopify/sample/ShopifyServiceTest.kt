package com.asdev.shopify.sample

import android.support.test.runner.AndroidJUnit4
import io.reactivex.schedulers.Schedulers

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class ShopifyServiceTest {

    @Test
    fun getCollections() {
        val collections = ShopifyService.getCollections()
            .toList()
            .subscribeOn(Schedulers.io())
            .blockingGet()

        assertTrue("No items were returned", collections.size > 0)
        println(collections)
    }

    @Test
    fun getCollectionProducts() {
        val allCollections = ShopifyService.getCollections()
            .toList()
            .subscribeOn(Schedulers.io())
            .blockingGet()

        val products = ShopifyService.getCollectionProducts(allCollections[0].id)
            .toList()
            .subscribeOn(Schedulers.io())
            .blockingGet()

        assertTrue("No items were returned", products.size > 0)
        println(products)
    }

    @Test
    fun getProducts() {
        val allCollections = ShopifyService.getCollections()
            .toList()
            .subscribeOn(Schedulers.io())
            .blockingGet()

        val products = ShopifyService.getProducts(allCollections[0].id)
            .toList()
            .subscribeOn(Schedulers.io())
            .blockingGet()

        assertTrue("No items were returned", products.size > 0)
        println(products)
    }

}
