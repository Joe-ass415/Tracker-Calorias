package com.calorietracker.shared.util

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object JsonConfig {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
        prettyPrint = false
    }

    val prettyJson = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
        prettyPrint = true
    }
}

inline fun <reified T> T.toJsonByteArray(): ByteArray {
    return JsonConfig.json.encodeToString(this).toByteArray(Charsets.UTF_8)
}

inline fun <reified T> ByteArray.decodeFromJsonByteArray(): T {
    val stringContent = this.toString(Charsets.UTF_8)
    return JsonConfig.json.decodeFromString<T>(stringContent)
}

inline fun <reified T> String.decodeFromJsonString(): T {
    return JsonConfig.json.decodeFromString<T>(this)
}

inline fun <reified T> T.toJsonString(): String {
    return JsonConfig.json.encodeToString(this)
}

