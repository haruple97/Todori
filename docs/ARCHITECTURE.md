# Todori Architecture

단일 `:app` 모듈, 패키지 단위 레이어 분리. Google 권장 구조(UI / Data, 필요 시 Domain, 단방향 데이터 흐름)와 Now in Android의 core/feature 경계를 따른다.

## 패키지 (`com.haruple97.todori`)
| 패키지 | 책임 |
|---|---|
| `core/common` | 순수 Kotlin 공통 유틸(시간 포맷, Dispatcher qualifier). Android 의존 금지 |
| `core/model` | 도메인 모델. Android 의존 금지 |
| `core/data` | Repository(interface+Impl), Room(`database`), DataStore |
| `core/designsystem` | 테마, 공통 컴포넌트 |
| `core/ui` | 토마토 캐릭터(`TomatoCharacter`), 파티클, 공용 버튼/칩 |
| `core/timer` | `TimerEngine`(종료 시각 기준 상태머신), 알람·알림(`TimerPlatform`), 영속화 인터페이스. 서비스 없이 AlarmManager로 완료 처리 |
| `core/di` | Hilt 모듈 |
| `feature/*` | 화면 단위: `Screen` + `ViewModel` + `UiState` (`timer`, `record`) |
| `navigation` | NavHost, 타입세이프 라우트 |

## 의존 규칙
- `feature/*` → `core/*` 만 허용. `feature` 간 직접 참조 금지(이동은 `navigation`이 담당).
- `core/*` → `feature/*` 참조 금지.
- ViewModel은 `StateFlow<UiState>` 하나만 노출, Compose는 `collectAsStateWithLifecycle`로 구독.
- Dispatcher는 `@IoDispatcher`/`@DefaultDispatcher`로 주입(하드코딩 금지).
- UseCase(`core/domain`)는 로직이 두 곳 이상에서 재사용될 때만 도입.

## 기록(토마토 달력)
- 집중이 끝나면 `TimerEngine`이 `SessionRepository`에 완료 기록을 남기고, `feature/record`가 그 Flow를 구독해 월 달력에 토마토를 채운다.
- 날짜 묶기·연속 일수·달력 격자는 `RecordLogic.kt`의 순수 함수(단위 테스트 있음)로 두고, ViewModel은 상태 조합만 한다.
- 타이머 화면 → 기록 화면은 `navigation/TodoriNavHost.kt`의 `RecordRoute`로 이동한다.
- 삭제: 기록 줄의 휴지통으로 즉시 삭제하고 되돌리기 스낵바를 띄운다(확인 창 없음). 그날 2개 이상이면 "모두 삭제"가 있고 이것만 확인 창을 거친다. 되돌리기는 같은 id로 다시 넣는다(`RecordViewModel.undoDelete`).
- 삭제·되돌리기도 완료 기록의 개수/마지막 시각 변화로 감지해 위젯이 갱신된다(`updateAll`은 각각 별도 코루틴으로 실행).

## 말풍선 문구
- 문구는 "상황"별 묶음이다. `BubbleMessages.kt`의 `bubbleKey()`(순수 함수, 테스트 있음)가 처음 사용 여부·오늘 횟수·시간대·고른 시간·진행 단계(시작/초반/중반/막바지/마지막 1분)·일시정지·완료(몇 번째, 긴 집중)로 묶음을 고르고, 문구는 `res/values{,-en}/bubble_messages.xml`에 있다.
- 새 문구는 해당 묶음에 `<item>`만 추가하면 된다(`{count}`는 오늘 횟수로 치환). 같은 상황에서는 `BubbleHistory`가 직전 문구를 피한다.

## 낮은 세로 화면
- 인사말(위)과 버튼(아래)은 고정하고 가운데(말풍선·토마토·시간)만 남는 높이에 맞춘다. 토마토는 `MIN_STAGE`(200dp) 아래로 줄지 않고, 그보다 낮으면 가운데가 스크롤된다.
- 토마토 위의 세로 드래그는 시간 조절이 우선이라, 스크롤은 토마토 밖(말풍선·시간 영역)을 끌어서 한다.

## 홈 화면 위젯
- `widget/`: Jetpack Glance(`glance-appwidget`)로 만든 "이번 주 토마토" 위젯. 일~토 7일에 토마토를 채워 보여주고, 크기(작게/넓게)에 따라 레이아웃이 바뀐다(`SizeMode.Responsive`).
- 주간 요약 계산은 순수 함수 `buildWeekSummary`(단위 테스트 있음). 데이터는 Hilt `EntryPoint`로 `SessionRepository`에서 읽는다.
- 위젯을 누르면 딥링크(`todori://record`)로 기록 화면이 바로 열린다.
- 갱신: 새 완료 기록이 생기면 `TodoriApp`이 `updateAll`을 호출하고(알람으로 프로세스가 새로 떠도 동작), 날짜 변경은 30분 주기 갱신(`updatePeriodMillis`)에 맡긴다.
- 주의: 앱을 강제 종료하면 위젯의 클릭 동작이 다음 갱신 전까지 취소될 수 있다(안드로이드 기본 동작).

## 화면 꺼짐 방지
- 타이머가 `Running`일 때만 `View.keepScreenOn`을 켜고, 일시정지·완료·화면 이탈(`onDispose`) 시 즉시 끈다. 백그라운드에서는 알람이 완료를 처리하므로 화면을 켜 둘 필요가 없다.

## 반응형 레이아웃
- 화면 분기는 기기·방향이 아니라 Window Size Class(`currentWindowAdaptiveInfo().windowSizeClass`, `material3-adaptive`)로 한다.
- 폭 ≥ 600dp 이고 높이 < 900dp(가로 모드 폰, 펼친 폴더블, 가로 태블릿)면 좌우 2단(토마토 | 정보·버튼), 그 외는 1단. 1단은 폭을 480dp로 제한한다.
- 토마토 크기는 고정 dp가 아니라 가용 영역에서 계산하고, 시스템 바·컷아웃은 `safeDrawingPadding`으로 피한다.
- 회전 시 Activity가 재생성되므로 일회성 UI 상태는 `rememberSaveable`, 타이머 상태는 `TimerEngine`에 둔다.

## 멀티모듈 전환 기준
빌드 시간이 체감되거나 feature가 4개 이상이 되면 `:core:*`, `:feature:*`로 추출하고 `build-logic` convention plugin을 도입한다.

## 빌드 주의
AGP 9 built-in Kotlin 사용 → `kotlin-android`/kapt 금지, 어노테이션 처리는 KSP(Room, Hilt).
