# Todori Architecture

단일 `:app` 모듈, 패키지 단위 레이어 분리. Google 권장 구조(UI / Data, 필요 시 Domain, 단방향 데이터 흐름)와 Now in Android의 core/feature 경계를 따른다.

## 패키지 (`com.haruple97.todori`)
| 패키지 | 책임 |
|---|---|
| `core/common` | 순수 Kotlin 공통 유틸(시간 포맷, Dispatcher qualifier). Android 의존 금지 |
| `core/model` | 도메인 모델. Android 의존 금지 |
| `core/data` | Repository(interface+Impl), Room(`database`), DataStore |
| `core/designsystem` | 테마, 공통 컴포넌트 |
| `core/ui` | 토마토 캐릭터 등 공용 Compose UI (추가 예정) |
| `core/di` | Hilt 모듈 |
| `feature/*` | 화면 단위: `Screen` + `ViewModel` + `UiState` |
| `navigation` | NavHost, 타입세이프 라우트 |

## 의존 규칙
- `feature/*` → `core/*` 만 허용. `feature` 간 직접 참조 금지(이동은 `navigation`이 담당).
- `core/*` → `feature/*` 참조 금지.
- ViewModel은 `StateFlow<UiState>` 하나만 노출, Compose는 `collectAsStateWithLifecycle`로 구독.
- Dispatcher는 `@IoDispatcher`/`@DefaultDispatcher`로 주입(하드코딩 금지).
- UseCase(`core/domain`)는 로직이 두 곳 이상에서 재사용될 때만 도입.

## 멀티모듈 전환 기준
빌드 시간이 체감되거나 feature가 4개 이상이 되면 `:core:*`, `:feature:*`로 추출하고 `build-logic` convention plugin을 도입한다.

## 빌드 주의
AGP 9 built-in Kotlin 사용 → `kotlin-android`/kapt 금지, 어노테이션 처리는 KSP(Room, Hilt).
