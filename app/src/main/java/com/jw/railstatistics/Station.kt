package com.jw.railstatistics

import java.io.Serializable

data class Station(
    val name: String,
    val country: String,
    val county: String,
    val trainOperator: String,
    val visitStatus: String,
    val visitedDate: String,
    val favorite: Boolean,
    val latitude: String,
    val longitude: String,
    val yearlyUsage: Map<Int, String> = mapOf()
) : Serializable