package com.jw.railstatistics

import java.io.Serializable

data class Station(
    val name: String,
    val county: String,
    val trainOperator: String,
    val visitedDate: String,
    val visitStatus: String,
    val extraData: List<String> = emptyList()
) : Serializable