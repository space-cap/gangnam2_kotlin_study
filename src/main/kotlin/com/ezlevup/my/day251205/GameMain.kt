package com.ezlevup.my.day251205

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val character = GameCharacter()

    // HP 상태(StateFlow) 구독
    val hpJob = launch {
        // collectLatest: 새 값이 오면 이전 값 처리 중이던 것을 취소하고 최신 값부터 처리
        character.hp.collectLatest { hp ->
            println("HP 상태 변경: $hp")
        }
    }

    // 이벤트(SharedFlow) 구독
    val eventJob = launch {
        // SharedFlow는 emit될 때마다 이벤트를 그대로 전달
        character.events.collect { event ->
            when (event) {
                is GameEvent.Hit ->
                    println("이벤트: ${event.damage} 피해, 남은 HP=${event.currentHp}")

                is GameEvent.LevelUp ->
                    println("이벤트: 레벨업! 현재 레벨=${event.newLevel}")

                GameEvent.Dead ->
                    println("이벤트: 캐릭터 사망")
            }
        }
    }

    // ---------- 상태/이벤트 실제 발생시키기 ----------
    character.hit(30)       // HP 감소 + Hit 이벤트 + (필요 시) Dead 이벤트
    delay(100)

    character.gainExp(100)  // LevelUp 이벤트
    delay(100)

    character.hit(80)       // HP 0이 되면서 Hit + Dead 이벤트
    delay(300)

    // 코루틴 정리
    hpJob.cancel()
    eventJob.cancel()
}

