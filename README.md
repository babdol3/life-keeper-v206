# 생활비서 Life Keeper Native Android

## v20.1 안정화 패치

이번 버전은 v20 점검 중 발견된 빌드 위험과 복원 안정성 문제를 수정한 패치입니다.

### 수정 내용

- ShoppingScreen에 잘못 전달되던 백업 콜백 3개 제거
- v12/v15 계열 DB에서 v19/v20으로 올라갈 때 필요한 Room migration 추가
- 백업 복원 시 중복 항목을 건너뛰도록 개선
- 복원 완료 메시지를 실제 새로 추가된 개수 기준으로 변경
- v20 백업/복원 기능 유지

## v20.1 안정화 패치

v20은 기존 로드맵의 마지막 단계로, 가족/여행/장소 데이터를 안전하게 보관하고 다른 폰으로 옮길 수 있도록 백업/복원 기능을 강화한 버전입니다.

### 추가/개선 내용

- JSON 백업 내보내기
  - 클립보드에 백업 JSON 복사
  - 메모장, 카카오톡 나에게 보내기, Google Keep 등에 저장 가능
- JSON 복원
  - 클립보드의 백업 JSON 읽기
  - 장소 / 구매목록 / 장소 할 일 / 여행 / 시간 알림 복원
- v19 여행 데이터까지 백업/복원
  - travel_plans
  - travel_places
  - travel_actions
  - travel_checklist_items
  - travel_reservations
  - travel_memos
- 권한 진단 리포트
  - 알림 권한
  - 위치 권한
  - 정밀 위치
  - 백그라운드 위치
  - 진동 권한
  - Supabase 설정 상태
  - 데이터 개수 요약
- 더보기 탭에 백업/복원 패널 추가
- 권한 진단 내용을 클립보드에 복사
- 기존 기능 유지
  - v19 여행 모드
  - v17 알림 액션
  - v16 담당자/공유 관리
  - Supabase 가족 공유
  - 홈 위젯 최소 구조

### 사용법

#### 백업

```text
더보기
→ v20 백업/복원
→ 백업 복사
→ 클립보드의 JSON을 메모장/카톡/Keep 등에 저장
```

#### 복원

```text
저장해둔 백업 JSON 전체 복사
→ 생활비서 앱 실행
→ 더보기
→ v20 백업/복원
→ 클립보드 복원
```

#### 권한 진단

```text
더보기
→ v20 백업/복원
→ 권한 진단 내용 복사
```

### 주의

v20은 파일 선택 UI 대신 클립보드 기반 백업/복원으로 구현했습니다.  
이 방식은 빌드 안정성이 높고, 새 폰으로 옮기기도 쉽습니다.

DB 스키마는 v19와 동일하게 유지했습니다.  
따라서 v19에서 v20으로 업데이트해도 Room 버전 증가로 인한 데이터 삭제를 피할 수 있습니다.

이 환경에서는 Android Gradle 빌드를 직접 실행하지 못했습니다.  
GitHub Actions에서 최종 빌드를 확인해 주세요.


## GitHub Actions 빌드 안내 v20.2

이 버전은 `gradlew`가 없어도 GitHub Actions에서 Gradle을 설치해서 빌드하도록 `.github/workflows/build-android.yml`을 포함합니다.

중요: GitHub 저장소 루트에 `app/`, `build.gradle`, `settings.gradle`, `.github/`가 바로 보이도록 업로드하세요.

빌드 후 APK 위치:

```text
Actions > Build Android APK > Artifacts > LifeKeeper-debug-apk
```


## v20.3 AndroidX 빌드 수정

GitHub Actions 오류 `android.useAndroidX property is not enabled`를 해결하기 위해 루트 `gradle.properties`에 아래 값을 추가했습니다.

```properties
android.useAndroidX=true
android.enableJetifier=true
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
kotlin.code.style=official
```


## v20.4 JVM 17 빌드 수정

GitHub Actions 오류 `Inconsistent JVM-target compatibility`를 해결했습니다.

수정 내용:

```gradle
compileOptions {
    sourceCompatibility JavaVersion.VERSION_17
    targetCompatibility JavaVersion.VERSION_17
}

kotlinOptions {
    jvmTarget = "17"
}

kotlin {
    jvmToolchain(17)
}
```

또한 빌드 실패 시 `LifeKeeper-build-log` artifact가 항상 업로드되도록 workflow를 보강했습니다.


## v20.5 One-pass Build Stability

v20.5는 GitHub Actions 빌드 피로도를 줄이기 위한 안정화 버전입니다.

수정 내용:

- 중복 workflow 제거
- `.github/workflows/build-android.yml` 하나만 유지
- AndroidX 설정 유지
- Java/Kotlin/kapt JVM target을 17로 통일
- `kotlin { jvmToolchain(17) }`을 Gradle 표준 위치인 top-level로 이동
- `kapt { correctErrorTypes = true }` 추가
- `LifeKeeperApp.kt`에 file-level Compose Experimental opt-in 추가
- 빌드 실패 시 `LifeKeeper-build-log` artifact 항상 업로드
- workflow에서 프로젝트 구조를 먼저 검증

GitHub에 올릴 때 저장소 최상단은 아래처럼 보여야 합니다.

```text
app/
.github/
docs/
build.gradle
settings.gradle
gradle.properties
README.md
```


## v20.6 Kotlin Compile Fix

GitHub Actions `LifeKeeper-build-log` 기준으로 발견된 Kotlin 컴파일 오류 5개를 수정했습니다.

수정 내용:

- 잘못된 `androidx.compose.foundation.layout.weight` import 제거
- 문자열 보간 오류 수정: `$readyCount개` → `${readyCount}개`
- `MoreScreen`에 `@Composable` 추가
- `AddBottomSheet` 저장 when에 `AddType.TimeReminder` 분기 추가
- v20.5의 빌드 안정화 설정 유지
