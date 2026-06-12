# Firebase 가족 공유 연결 가이드

v12에는 가족 공유 UI, Room 데이터 구조, 로컬 동작, 클라우드 동기화 계약이 들어 있습니다.

현재 상태:

- `google-services.json` 없이도 빌드 가능
- 가족 만들기 / 초대 참여 / 공유 범위 설정 UI 제공
- 가족 구성원 / 최근 활동 로컬 표시
- Firebase 연결 전에는 로컬 시뮬레이션 방식으로 동작

## Firebase로 실제 공유를 붙이는 순서

### 1. Firebase 프로젝트 생성

Firebase Console에서 Android 앱을 추가합니다.

패키지명:

```text
com.ultratul.lifekeeper
```

`google-services.json`을 다운로드해서 아래 위치에 넣습니다.

```text
app/google-services.json
```

### 2. Gradle 설정

실제 Firebase 연결 시 아래 플러그인과 의존성을 추가합니다.

루트 `build.gradle`:

```gradle
plugins {
    id "com.google.gms.google-services" version "4.4.2" apply false
}
```

`app/build.gradle` plugins:

```gradle
id "com.google.gms.google-services"
```

dependencies:

```gradle
implementation platform("com.google.firebase:firebase-bom:33.7.0")
implementation "com.google.firebase:firebase-auth"
implementation "com.google.firebase:firebase-firestore"
implementation "com.google.firebase:firebase-messaging"
```

### 3. Firestore 권장 구조

```text
families/{familyId}
  familyName
  ownerUid
  inviteCode
  createdAt

families/{familyId}/members/{uid}
  name
  role
  deviceLabel
  lastSeenAt

families/{familyId}/places/{placeId}
families/{familyId}/shoppingItems/{itemId}
families/{familyId}/placeTasks/{taskId}
families/{familyId}/travelPlans/{travelId}
families/{familyId}/travelPlaces/{placeId}
families/{familyId}/travelActions/{actionId}
families/{familyId}/activities/{activityId}
```

### 4. 공유 기본 정책

추천 기본값:

- 공유 ON: 장소, 마트/다이소 구매 목록, 장소에서 할 일, 여행 플래너
- 공유 OFF: 개인 시간 알림, 물건 위치

개인 시간 알림과 물건 위치는 사생활 정보가 섞일 수 있어서 기본 비공유가 안전합니다.

### 5. 보안 규칙 개념

핵심은 로그인한 사용자가 해당 가족의 members에 있을 때만 읽고 쓰게 하는 것입니다.

```text
families/{familyId}/...
허용 조건:
request.auth.uid가 families/{familyId}/members/{uid}에 존재
```

### 6. 앱 코드 연결 지점

현재 들어간 파일:

```text
app/src/main/java/com/ultratul/lifekeeper/family/FamilyCloudSyncContract.kt
```

Firebase 연결 시 `LocalOnlyFamilyCloudSync` 대신 Firestore 구현체를 만들면 됩니다.

예상 구현체 이름:

```text
FirebaseFamilyCloudSync
```

담당 기능:

- 가족 생성
- 초대 코드로 참여
- 공유 데이터 업로드
- Firestore snapshot listener로 실시간 반영
- FCM으로 변경 알림
