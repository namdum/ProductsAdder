package com.example.productsadder.network.extension

import android.os.Bundle

fun getAPIBaseUrl(): String {
    return "https://api.shopmeangene.com/"
}

inline fun <reified T : Enum<T>> Bundle.getEnum(key: String, default: T): T {
    val found = getString(key)
    return if (found == null) { default } else enumValueOf(found)
}

