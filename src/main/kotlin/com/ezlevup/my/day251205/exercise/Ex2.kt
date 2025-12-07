package com.ezlevup.my.day251205.exercise

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    val flow1 = flowOf("A1", "A2", "A3")
    val flow2 = flowOf("B1", "B2", "B3", "B4")

    flow1.zip(flow2) { a, b -> "$a$b" }
        .collect { println("zip: $it") }

    flow1.combine(flow2) { number, letter ->
        "$number - $letter"
    }.collect { value ->
        println("combine: $value")
    }
}