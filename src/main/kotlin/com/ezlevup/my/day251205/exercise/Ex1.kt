package com.ezlevup.my.day251205.exercise

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

fun ex1Numbers(): Flow<Int> =
    (1..5).asFlow()
        .filter { it % 2 == 0 }
        .map { it * 10 }
        .onEach { delay(100) }

fun main(): Unit = runBlocking {
    ex1Numbers().collect {
        println(it)
    }
}
