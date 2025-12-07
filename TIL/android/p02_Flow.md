# Flow

- 데이터 스트림을 표현하기 위한 구조
- LiveData는 안드로이드의 Life cycle 과 깊에 연관되어 있는 반면 순수 코틀린 API
- 코틀린 공식 비동기 API
- 코루틴에서 데이터 수집이 가능함.

### 단일 값 vs 여러 값

```kotlin
// 단일 값 반환
suspend fun getUser(): User

// 여러 값을 순차적으로 방출
fun getUpdates(): Flow<Update>
```

- Flow는 비동기적으로 계산되는 데이터 스트림
- 코루틴을 기반으로 동작
- 여러 값을 순차적으로 방출할 수 있음.
- Cold Stream 방식(수집하는 시점에 데이터 생성)

### flow() 빌드

```kotlin
// 게임 캐릭터의 상태 변화를 Flow로 표현
class GameCharacter(val name: String) {

    // 레벨업 이벤트를 발생시키는 Flow
    fun experienceFlow() = flow {
        var level = 1
        while (level <= 5) {
            delay(timeMillis = 1000) // 1초마다 레벨업
            emit(level++)
        }
    }

    // HP 변화를 발생시키는 Flow
    fun hpFlow() = flow {
        var hp = 100
        while (hp > 0) {
            delay(timeMillis = 500) // 0.5초마다 HP 감소
            hp -= 10
            emit(hp)
        }
    }
}

```

- flow 빌더는 코루틴을 사용하여 비동기 스트림을 생성하는데 사용됨
- emit(): Flow 내에서 값을 방출할 때 사용.
- flow 빌더 내에서 suspend 함수 사용 가능

### launchIn()

- Flow 를 새로운 코루틴에서 수집하기 위한 함수. 주로 ViewModel에서 많이 사용됨.

```kotlin
fun main() = runBlocking {
    val hero = GameCharacter("용사")

    // ---------------- 레벨업 관찰 ----------------
    hero.experienceFlow()
        // Flow에서 emit되는 각 level 값을 관찰할 때마다 실행
        .onEach { level ->
            println("${hero.name}가 ${level}레벨이 되었습니다!")
        }
        // 별도의 코루틴을 만들어 이 Flow를 수집 시작
        .launchIn(this)

    // ---------------- HP 변화 관찰 ----------------
    hero.hpFlow()
        // emit될 때마다 현재 HP를 출력
        .onEach { hp ->
            println("${hero.name}의 현재 HP: $hp")
        }
        // hp 값이 50 미만일 때만 아래 연산이 이어지도록 필터링
        .filter { hp -> hp < 50 }        // HP가 50 미만일 때만 통과
        // 필터를 통과한 hp에 대해 “위험” 메시지 출력
        .onEach {
            println("${hero.name}가 위험합니다!")
        }
        // 이 Flow 체인을 현재 runBlocking 스코프에서 실행 시작
        .launchIn(this)

    // 메인 코루틴을 5초 동안 유지해서 위 Flow들이 동작할 시간 확보
    delay(5000) // 5초 동안 관찰
}

```

```kotlin
suspend fun main() {
    // 1. 기본 Flow 생성과 수집
    println("\n1. 기본 Flow")

    // Int 값을 순서대로 내보내는 기본 Flow 생성
    val basicFlow = flow {
        // 1부터 3까지 숫자를 차례대로 emit
        for (i in 1..3) {
            delay(100)      // 0.1초씩 간격을 두고
            println("Emitting $i")     // 지금 내보내는 값 로그 출력
            emit(i)            // 실제로 Flow로 값 i를 발행
        }
    }

    // 위에서 만든 Flow를 수집(collect)해서 값 처리
    basicFlow.collect { value ->
        // Flow에서 emit된 값을 하나씩 꺼내와 출력
        println("Collected: $value")
    }
}

```

```text
결과값
1. 기본 Flow
Emitting 1
Collected: 1
Emitting 2
Collected: 2
Emitting 3
Collected: 3

```

1, 2, 3 실행이 되고 1, 2, 3 인 줄 알았는데
하나씩 주고받고, 주고받고, ... 하네.

그리고 collect 호출을 하지 않으면 flow가 실행이 안 되네.

- collect(): Flow의 값을 소비하기 위해 사용되는 함수
- Flow는 collect() 되는 시점에 값이 방출 됨(Cold)

### 3. Flow 연산자 예제

```kotlin
val numbers = (1..10).asFlow() // 1부터 10까지를 Flow로

numbers
    .map { it * 2 }            // 각 값을 2배로 변환
    .filter { it % 4 == 0 }    // 4의 배수만 통과
    .take(3)                   // 앞에서부터 3개까지만 받고 Flow 취소
    .collect { value ->
        println("결과: $value")
    }

```

## onEach, debounce (로그·검색창 예)

```kotlin
val searchFlow = userInputFlow()    // 예: 텍스트 변경을 Flow로 만들어 둔 상태

searchFlow
    .onEach { query ->
        println("입력: $query")     // 디버깅용 로그
    }
    .debounce(300)                  // 300ms 동안 추가 입력 없을 때만 통과
    .filter { it.length >= 2 }      // 글자 수 2 이상일 때만
    .collect { keyword ->
        search(keyword)             // API 검색 호출
    }
```

- `onEach`는 값은 그대로 흘려보내면서, 사이드 이펙트(로그, 이벤트 등)를 추가할 때 사용한다.
- `debounce`는 짧은 시간 동안 여러 번 emit될 때 마지막 것만 통과시켜서, 검색 API 호출 같은 것을 줄이는 데 많이 쓴다.

## transform으로 복잡한 변환

```kotlin
val flow = (1..3).asFlow()

flow
    .transform { value ->
        emit("값 $value 시작")  // 하나의 입력에서 여러 값 emit 가능
        emit("제곱: ${value * value}")
    }
    .collect { println(it) }
```

- `transform`은 한 입력을 여러 출력으로 바꾸거나, `emit` 타이밍을 더 자유롭게 제어하고 싶을 때 쓰는 “고급 map 느낌”의 연산자다.

## flowOn, buffer로 스레드·성능 제어

```kotlin
val heavyFlow = flow {
    emit(doHeavyWork())            // 무거운 연산
}

heavyFlow
    .flowOn(Dispatchers.Default)   // 위쪽(emit, map 등)을 백그라운드에서 실행
    .buffer()                      // emit 쪽과 collect 쪽을 비동기로 분리해 버퍼링
    .collect { result ->
        println("받은 값: $result")
    }
```

- `flowOn`은 emit·중간 연산이 실행되는 코루틴 컨텍스트(디스패처)를 바꿔서, UI 스레드를 막지 않게 한다.
- `buffer`는 emit 속도와 collect 속도가 다를 때 중간 버퍼를 둬서 백프레셔를 완화한다.

### 4. Flow 합치기: Zip 연산자

```kotlin
val flow1 = flowOf("A", "B", "C")
val flow2 = flowOf(1, 2, 3)

flow1.zip(flow2) { a, b -> "$a$b" }
    .collect { println("Zipped: $it") }

```

```text
결과
Zipped: A1
Zipped: B2
Zipped: C3
```

### 4-1. Flow 합치기 : Combine 연산자

```kotlin
    val flow1 = flowOf(1, 2, 3).onEach { delay(100) }     // 0.1초 대기
val flow2 = flowOf("A", "B", "C").onEach { delay(200) } // 0.2초 대기

flow1.combine(flow2) { number, letter ->
    "$number - $letter"
}.collect { value ->
    println(value)   // 1 - A, 2 - A, 3 - A, 3 - B, 3 - C 출력
}
```

- combine
    - 하나의 Flow가 값을 방출할 때마다 가장 최근의 값을 사용하여 새로운 값을 생성
    - 방출 순서 보장 못 함
    - 방출될 때마다 다른 Flow의 최신 값을 사용하고 싶을 때
    - 실시간 검색에서 사용자 입력과 필터를 결합하고자 할 때

### 에러 처리

```kotlin
suspend fun main() {
    flow {
        emit(1)
        throw RuntimeException("에러 발생!")
    }.catch { e ->
        println("에러 캐치: ${e.message}")
        emit(-1)
    }.onCompletion { cause ->
        println("완료${cause?.let { ": ${it.message}" } ?: ""}")
    }.collect {
        println("값: $it")
    }
}
```

```text
결과
값: 1
에러 캐치: 에러 발생!
값: -1
완료
```

### 6. StateFlow 예제

```kotlin

fun main() = runBlocking {
    val stateFlow = MutableStateFlow(0)

    // 이 runBlocking 스코프에서 코루틴을 하나 띄워서 collect 수행
    val job = launch {
        stateFlow.collect { value ->
            println("StateFlow: $value")
        }
    }

    delay(100)
    stateFlow.value = 1
    delay(100)
    stateFlow.value = 2
    delay(100)

    job.cancel()    // collect 중단
}

```

```text
StateFlow: 0
StateFlow: 1
StateFlow: 2

```

- StateFlow는 코루틴에서 제공하는 특별한 핫 스트림 Flow 임
- 상태 변화를 관찰하고, 최신 상태를 유지하는데 유용함. 안드로이드에서 주로 UI 상태 관리에 사용됨
- 항상 최신 상태를 유지 (소비되지 않고 마지막 값이 남아 있음)
- Thread Safe
- StateFlow 는 읽기 전용이며, 상태를 변경하려면 MutableStateFlow를 사용

### 7. 병렬 처리

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun main() {
    (1..5).asFlow()
        .flatMapMerge { num ->
            flow {
                delay(100)
                emit("처리된 $num")
            }
        }
        .collect { println(it) }
}

```

```text
처리된 4
처리된 5
처리된 3
처리된 1
처리된 2

```

- flatMapMerge : 여러 개의 Flow를 병합하여 단일 Flow로 만드는데 사용
- 병렬 처리를 지원하여, 여러 Flow 의 결과를 동시에 수집
- 방출된 값의 순서가 보장되지 않는다

### 8. 컨텍스트 전환

```kotlin

fun main(): Unit = runBlocking {
    println("runBlocking: ${Thread.currentThread().name}")

    launch(Dispatchers.Default) {
        flow {
            println("Flow:   ${Thread.currentThread().name}")
            emit(1)
        }.flowOn(Dispatchers.IO)
            .collect {
                println("Collect:${Thread.currentThread().name}")
                println("값: $it")
            }
    }
}

```

```text
runBlocking: main
Flow:   DefaultDispatcher-worker-2
Collect:DefaultDispatcher-worker-1
값: 1

```

### 9. debounce 예제

```kotlin

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

```

```text
Debounced: C
Debounced: D

```

## StateFlow

- StateFlow는 초기값이 필수, LiveData는 초기값이 선택적이다
- 구독자가 필요할 때 값을 얻는다
- 값이 변경될 때만 구독자에게 값을 전달함
- value 프로퍼티로 값을 get/set 할 수 있다
- Flow가 cold 스트림 이라면, StateFlow는 hot 스트림
- UI 작업을 위해서는 데이터 홀더로써 StateFlow 사용이 일반적이다
- LiveData 와 기본적으로 역할이 동일

```kotlin
class GameViewModel : ViewModel() {
    private val _hp = MutableStateFlow(100)      // 내부에서 변경
    val hp: StateFlow<Int> = _hp                 // 외부는 읽기 전용

    fun hit(damage: Int) {
        _hp.value -= damage                      // 상태 변경
    }
}

```

### SharedFlow

- 초기값을 가지지 않는다
- 데이터를 읽어 들이기 전에는 아무값도 없는 데이터에 적합하다
- repeat, extraBufferCapacity, onBufferOverflow 정책을 상황에 맞게 사용할 수 있다
- Flow 를 shareIn 으로 SharedFlow로 변환 가능하다
- 원타임 이벤트를 처리할 때 유용하다

