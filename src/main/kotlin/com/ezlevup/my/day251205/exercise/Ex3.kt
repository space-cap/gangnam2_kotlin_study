package com.ezlevup.my.day251205.exercise

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking

@OptIn(FlowPreview::class)
fun main(): Unit = runBlocking {
    val loadingFlow = flow {
        println("[loading emit] thread = ${Thread.currentThread().name}")
        emit("데이터 로딩 중...")
        delay(500)
        emit("로딩 완료!")
    }
        .flowOn(Dispatchers.IO)
        .flowOn(Dispatchers.Default)

    println("=== 1) 로딩 메시지 Flow ===")
    loadingFlow.collect { msg ->
        println("[collect] thread = ${Thread.currentThread().name}, value = $msg")
    }

    println("\n=== 2) 키보드 입력 + debounce(300ms) ===")

    flow {
        emit("A")
        delay(100)
        emit("B")
        delay(90)
        emit("C")
        delay(110)
        emit("D")
    }.debounce(300)
        .collect { println("Debounced: $it") }
}
