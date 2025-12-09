### **습관 추적 앱 (Habit Tracker with Streaks)**

기능: 습관을 등록해서 하루에 한번 체크를 한다.

**핵심 기능:**

- **습관 등록/관리**: 목표, 카테고리, 빈도(매일/주 N회) 설정
- **체크인 시스템**: 오늘 습관 완료 체크, 스트릭(연속일) 표시
- **진행률 시각화**: 월별/주별 완성도, 칼렌더 뷰
- **통계**: 가장 오래 지속된 습관, 완성률 등
- **로컬 데이터베이스**: Room으로 오프라인 지원

**왜 좋은가:**

- 완성도 높은 소규모 앱 (스코프 조절 용이)
- Room Database, Jetpack Compose 모두 활용
- 일상적으로 사용하며 테스트 가능
- 포트폴리오에는 중간급 정도의 임팩트

**현재 구조:**

- ✅ 습관 등록 (한 번)
- ✅ 오늘의 습관 리스트 (메인 화면)
- ✅ 매일 체크인 (○ 클릭 → ✓)
- ✅ 삭제할 때까지 계속 (✕ 클릭)

**지금 앱이 이미 그렇게 작동합니다!**

**작동 흐름:**

1. 로그인 → 습관 등록 화면 (첫 습관 추가 옵션)
2. 메인 화면 (오늘의 습관 리스트)
3. **매일 ○ 클릭** → ✓로 변경 (오늘 완료!)
4. **내일 다시 방문** → ○로 초기화되어 다시 체크 가능
5. **습관 삭제** → ✕ 클릭 시 삭제

***

## 실제 Kotlin/Android 앱 개발할 때 주의사항:

**데이터 구조 (Room Database):**

```kotlin
// HabitEntity - 습관 정보 (생성 후 변하지 않음)
@Entity
data class HabitEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val category: String,
    val frequency: String,
    val createdAt: Long
)

// CheckInEntity - 매일의 체크인 기록
@Entity
data class CheckInEntity(
    @PrimaryKey val id: Long,
    val habitId: Long,      // 어떤 습관인지
    val date: Long,          // 어느 날짜인지 (시간 제외)
    val isCompleted: Boolean, // 완료했는지
    val createdAt: Long
)
```

**매일 초기화 로직:**

- 오늘 날짜의 CheckIn 기록이 없으면 → ○ (미완료)
- 오늘 날짜의 CheckIn 기록이 있고 completed=true → ✓ (완료)
- 내일이 되면 자동으로 새 CheckIn 레코드 생성

***
화면

<img width="589" height="824" alt="image" src="https://github.com/user-attachments/assets/8dbcf50e-b8bb-4b46-b2da-a5d0dd6da1f0" />

***

<img width="596" height="821" alt="image" src="https://github.com/user-attachments/assets/5f2d34cc-7ec9-4bdd-8129-7405a8a639fd" />



