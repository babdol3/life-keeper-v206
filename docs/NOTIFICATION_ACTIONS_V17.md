# v17 알림 액션 구조

## 동작

알림에는 3개 버튼이 붙습니다.

```text
완료 / 10분 뒤 / 앱 열기
```

## 완료

`NotificationActionReceiver`가 `actionTargets` 값을 읽어서 Room DB를 업데이트합니다.

지원 대상:

```text
time:{id}
shopping:{id}
place_task:{id}
travel_action:{id}
```

완료 시 DB 업데이트:

```text
time_reminders.done = true
shopping_items.done = true
place_tasks.done = true
travel_actions.done = true
```

그리고 `ItemSyncState`에 `done_pending`을 기록합니다.

## 10분 뒤

기존 알림 제목/내용/채널/대상 값을 그대로 저장한 뒤, 10분 뒤 `ACTION_SHOW_SNOOZED`로 다시 표시합니다.

## 앱 열기

앱 launcher intent를 PendingIntent로 연결합니다.

## 연결 지점

```text
NotificationHelper.kt
NotificationActionReceiver.kt
TimeReminderReceiver.kt
GeofenceBroadcastReceiver.kt
```
