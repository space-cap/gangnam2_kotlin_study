package com.ezlevup.my.day251205

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

// 게임 캐릭터 도메인 모델
class GameCharacter(
    private val maxHp: Int = 100
) {
    // 1. 현재 HP 상태를 나타내는 StateFlow (항상 최신 HP 하나를 들고 있음)
    private val _hp = MutableStateFlow(maxHp) // 내부에서만 값 변경 가능
    val hp: StateFlow<Int> = _hp              // 외부에는 읽기 전용 StateFlow로 노출

    // 2. 단발성 이벤트(피격, 레벨업, 사망 등)를 위한 SharedFlow
    private val _events = MutableSharedFlow<GameEvent>() // emit 전용
    val events: SharedFlow<GameEvent> = _events          // collect 전용으로 공개

    private var level = 1

    // 공격을 받아 HP가 줄어드는 함수
    suspend fun hit(damage: Int) {
        // HP를 감소시키되 0보다 작아지지 않게 보정
        val newHp = (_hp.value - damage).coerceAtLeast(0)
        _hp.value = newHp                         // StateFlow의 상태 값 업데이트

        // "피격을 당했다"는 단발성 이벤트 발행
        _events.emit(GameEvent.Hit(damage, newHp))

        // HP가 0이 되면 사망 이벤트 발행
        if (newHp == 0) {
            _events.emit(GameEvent.Dead)
        }
    }

    // 경험치를 얻어서 레벨이 오르는 함수 (예시는 바로 레벨업만 처리)
    suspend fun gainExp(exp: Int) {
        // 실제로는 exp 누적/필요량 계산 등을 하겠지만 예제라 바로 레벨 +1
        level += 1
        // "레벨업 했다"는 단발성 이벤트 발행
        _events.emit(GameEvent.LevelUp(level))
    }
}

// UI에서 처리하기 좋은 형태의 단발성 이벤트 모델
sealed class GameEvent {
    // 피격 이벤트: 얼마만큼 피해를 받았는지, 현재 HP가 얼마인지 포함
    data class Hit(val damage: Int, val currentHp: Int) : GameEvent()

    // 레벨업 이벤트: 새 레벨 값 포함
    data class LevelUp(val newLevel: Int) : GameEvent()

    // 사망 이벤트: 추가 정보는 없고 "죽었다"는 사실만 알리면 됨
    object Dead : GameEvent()
}
