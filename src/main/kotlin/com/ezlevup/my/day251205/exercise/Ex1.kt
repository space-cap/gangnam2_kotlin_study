package com.ezlevup.my.day251205.exercise

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    val numbers = (1..5).asFlow()

    numbers
        .filter { it % 2 == 0 }
        .map { it * 10 }
        .onEach { delay(100) }
        .collect { value ->
            println("결과: $value")
        }
}
