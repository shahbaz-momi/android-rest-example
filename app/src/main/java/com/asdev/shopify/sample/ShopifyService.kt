package com.asdev.shopify.sample

import com.asdev.shopify.sample.objects.DCollection
import com.asdev.shopify.sample.objects.DProduct
import com.rx2androidnetworking.Rx2AndroidNetworking
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

private const val NETWORK_TIMEOUT = 7000L
private const val GET_COLLECTIONS_URL =
    "https://shopicruit.myshopify.com/admin/custom_collections.json?page=1&access_token=c32313df0d0ef512ca64d5b336a0d7c6"
private const val GET_PRODUCTS_OF_COLLECTION =
    "https://shopicruit.myshopify.com/admin/collects.json?collection_id={id}&page=1&access_token=c32313df0d0ef512ca64d5b336a0d7c6"
private const val GET_PRODUCTS =
    "https://shopicruit.myshopify.com/admin/products.json?ids={ids}&page=1&access_token=c32313df0d0ef512ca64d5b336a0d7c6"

typealias ProductId = String

object ShopifyService {

    fun getCollections() =
        Observable.create<DCollection> { emitter ->
            val request = Rx2AndroidNetworking.get(GET_COLLECTIONS_URL)
                .build()
                .jsonObjectObservable
                .timeout(NETWORK_TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())

            val disposable = request.subscribeBy(
                onError = {
                    it.printStackTrace()
                    emitter.onError(it)
                },
                onComplete = {
                    // nothing to do
                },
                onNext = {
                    try {
                        val collectionsRaw = it.getJSONArray("custom_collections")
                        for (i in 0 until collectionsRaw.length()) {
                            val collectionRaw = collectionsRaw.getJSONObject(i)
                            val collectionItem = DCollection(
                                id = collectionRaw.getString("id"),
                                handle = collectionRaw.getString("handle"),
                                title = collectionRaw.getString("title"),
                                body = collectionRaw.getString("body_html"),
                                imageUrl = collectionRaw.getJSONObject("image").getString("src")
                            )

                            emitter.onNext(collectionItem)
                        }
                        emitter.onComplete()
                    } catch (e: Exception) {
                        emitter.onError(e)
                    }
                }
            )

            emitter.setDisposable(disposable)
        }

    fun getCollectionProducts(collectionId: String) =
        Observable.create<ProductId> { emitter ->
            val request = Rx2AndroidNetworking.get(GET_PRODUCTS_OF_COLLECTION)
                .addPathParameter("id", collectionId)
                .build()
                .jsonObjectObservable
                .timeout(NETWORK_TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())

            val disposable = request.subscribeBy(
                onError = {
                    it.printStackTrace()
                    emitter.onError(it)
                },
                onComplete = {
                    // nothing to do
                },
                onNext = {
                    try {
                        val productsRaw = it.getJSONArray("collects")
                        for (i in 0 until productsRaw.length()) {
                            val productRaw = productsRaw.getJSONObject(i)
                            emitter.onNext(productRaw.getString("product_id"))
                        }
                        emitter.onComplete()
                    } catch (e: Exception) {
                        emitter.onError(e)
                    }
                }
            )

            emitter.setDisposable(disposable)
        }

    fun getProducts(collectionId: String) =
        getCollectionProducts(collectionId)
            .toList()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .flatMapObservable { productIds ->
                Observable.create<DProduct> { emitter ->
                    val ids = productIds.joinToString(separator = ",")
                    val disposable = Rx2AndroidNetworking.get(GET_PRODUCTS)
                        .addPathParameter("ids", ids)
                        .build()
                        .jsonObjectObservable
                        .timeout(NETWORK_TIMEOUT, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
                        .subscribeBy(
                            onError = {
                                emitter.onError(it)
                            },
                            onNext = { obj ->
                                try {
                                    val arr = obj.getJSONArray("products")
                                    for (i in 0 until arr.length()) {
                                        val current = arr.getJSONObject(i)
                                        // parse all variants first
                                        val variantsRaw = current.getJSONArray("variants")
                                        val variants = mutableListOf<DProduct>()
                                        // variants really only require id, title, and quantity
                                        for (j in 0 until variantsRaw.length()) {
                                            val currentVariant = variantsRaw.getJSONObject(j)
                                            variants.add(
                                                DProduct(
                                                    id = currentVariant.getLong("id").toString(),
                                                    title = currentVariant.getString("title"),
                                                    quantity = currentVariant.getLong("inventory_quantity"),
                                                    price = currentVariant.getString("price").toDoubleOrNull(),
                                                    images = null,
                                                    variants = null,
                                                    body = null
                                                )
                                            )
                                        }


                                        val imagesRaw = current.getJSONArray("images")
                                        val images = mutableListOf<String>()
                                        for (j in 0 until imagesRaw.length()) {
                                            images.add(imagesRaw.getJSONObject(j).getString("src"))
                                        }

                                        val product = DProduct(
                                            id = current.getLong("id").toString(),
                                            title = current.getString("title"),
                                            body = current.getString("body_html"),
                                            price = null,
                                            quantity = null,
                                            images = images,
                                            variants = variants
                                        )

                                        emitter.onNext(product)
                                    }

                                    emitter.onComplete()
                                } catch (e: Exception) {
                                    emitter.onError(e)
                                }
                            },
                            onComplete = {
                                // already called emitter complete in onNext
                            }
                        )

                    emitter.setDisposable(disposable)
                }
            }
}