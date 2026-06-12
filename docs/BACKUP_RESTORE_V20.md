# v20 백업/복원 + 권한 진단

## 백업 대상

v20 백업에는 아래 데이터가 포함됩니다.

```text
places
shoppingItems
placeTasks
travelPlans
travelPlaces
travelActions
travelChecklist
travelReservations
travelMemos
timeReminders
```

## 백업 방식

`LifeKeeperBackupManager.exportToJson()`이 현재 앱 데이터를 JSON으로 만들고,  
`LifeKeeperViewModel.exportBackupToClipboard()`가 클립보드에 복사합니다.

## 복원 방식

`LifeKeeperViewModel.importBackupFromClipboard()`가 클립보드의 JSON을 읽고,  
`LifeKeeperBackupManager.importFromJson()`으로 파싱한 뒤 Room DB에 다시 저장합니다.

## 권한 진단

권한 진단 리포트에는 아래 항목이 포함됩니다.

```text
알림 권한
위치 권한
정밀 위치
백그라운드 위치
진동 권한
Supabase 설정 상태
장소 수
구매목록 수
장소 할 일 수
여행 액션 수
시간 알림 수
```

## 왜 클립보드 방식인가?

Android Storage Access Framework까지 붙이면 파일 선택 UI, Uri 권한, 예외 처리가 커집니다.  
v20은 안정적인 마지막 버전을 목표로 하기 때문에, 먼저 클립보드 기반으로 확실하게 동작하는 백업/복원을 구현했습니다.

## 다음 확장 후보

```text
v20.1 파일 저장/불러오기 UI
v20.2 Google Drive 백업
v20.3 Supabase 백업 테이블
v20.4 백업 암호화
```
