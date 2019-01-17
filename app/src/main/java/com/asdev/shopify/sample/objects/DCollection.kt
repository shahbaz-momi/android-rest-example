package com.asdev.shopify.sample.objects

import java.io.Serializable

data class DCollection(val id: String,
                       val handle: String,
                       val title: String,
                       val body: String,
                       val imageUrl: String): Serializable