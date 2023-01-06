package com.example.usercore.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

val objectMapper = jacksonObjectMapper()

fun <K, V> Any.toMap(): Map<K, V>{
    return objectMapper.convertValue(this, Map::class.java) as Map<K, V>
}

fun Any.toJson(): String{
    return objectMapper.writeValueAsString(this)
}
