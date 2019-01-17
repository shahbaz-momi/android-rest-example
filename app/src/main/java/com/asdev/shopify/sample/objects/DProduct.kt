package com.asdev.shopify.sample.objects

import com.asdev.shopify.sample.ProductId
import kotlin.math.abs

data class DProduct(
    val id: ProductId,
    val title: String,
    val body: String?,
    val quantity: Long?,
    val price: Double?,
    val images: List<String>?,
    val variants: List<DProduct>?) {

    fun calculateQuantity(): Long {
        if(variants == null)
            return 0

        return variants.fold(0L) { acc, p -> acc + (p.quantity?: 0L) }
    }

    fun calculateMinPrice(): Double {
        return variants?.filter { it.price != null }?.minBy { it.price!! }?.price?: 0.0
    }

    fun isConsistentPrice(): Boolean {
        if(variants == null)
            return true

        val minPrice = calculateMinPrice()

        return variants.filter { it.price != null }.all { abs(it.price!! - minPrice) <= 0.01 }
    }
}