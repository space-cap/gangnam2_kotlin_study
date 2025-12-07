package com.ezlevup.my.day251205


import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking


@OptIn(FlowPreview::class)
fun main(): Unit = runBlocking {
    flow {
        emit("A")
        delay(100)
        emit("B")
        delay(90)
        emit("C")
        delay(110)
        emit("D")
    }.debounce(100)
        .collect { println("Debounced: $it") }

}


