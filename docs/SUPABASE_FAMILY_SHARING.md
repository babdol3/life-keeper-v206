# Supabase 가족 공유 설정 가이드 (v16)

v16은 v15 안정화 구조에 담당자/항목별 공유 상태를 실제 카드 UI와 Supabase 업로드에 연결했습니다.

## 1. SupabaseConfig.kt 설정

```kotlin
object SupabaseConfig {
    const val SUPABASE_URL = "https://xxxx.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOi..."
}
```

## 2. SQL 실행

Supabase Dashboard > SQL Editor에서 아래 SQL을 실행합니다.

```sql
create table if not exists families (
  family_code text primary key,
  family_name text not null,
  owner_name text,
  updated_at bigint
);

create table if not exists family_share_profiles (
  family_code text primary key,
  family_name text,
  my_name text,
  role text,
  share_places boolean default true,
  share_shopping boolean default true,
  share_place_tasks boolean default true,
  share_travel boolean default true,
  share_time_reminders boolean default false,
  share_stored_items boolean default false,
  updated_at bigint
);

create table if not exists family_members (
  id bigserial primary key,
  family_code text not null,
  name text not null,
  role text default 'member',
  device_label text default 'Android',
  last_seen_at bigint,
  unique(family_code, name)
);

create table if not exists family_activity (
  id bigserial primary key,
  family_code text not null,
  actor_name text,
  message text,
  created_at bigint
);

create table if not exists shared_places (
  id bigserial primary key,
  family_code text not null,
  local_id bigint,
  name text not null,
  category text,
  latitude double precision,
  longitude double precision,
  radius_meters integer default 300,
  updated_at bigint,
  updated_by text,
  deleted_at bigint,
  sync_status text default 'synced',
  assignee text default '전체',
  shared boolean default true,
  unique(family_code, name, category)
);

create table if not exists shared_shopping_items (
  id bigserial primary key,
  family_code text not null,
  local_id bigint,
  place_name text,
  title text not null,
  category text,
  latitude double precision,
  longitude double precision,
  radius_meters integer default 300,
  done boolean default false,
  completed_at bigint,
  updated_at bigint,
  updated_by text,
  deleted_at bigint,
  sync_status text default 'synced',
  assignee text default '전체',
  shared boolean default true,
  unique(family_code, place_name, title)
);

create table if not exists shared_place_tasks (
  id bigserial primary key,
  family_code text not null,
  local_id bigint,
  place_name text,
  title text not null,
  category text,
  latitude double precision,
  longitude double precision,
  radius_meters integer default 300,
  done boolean default false,
  completed_at bigint,
  note text,
  updated_at bigint,
  updated_by text,
  deleted_at bigint,
  sync_status text default 'synced',
  assignee text default '전체',
  shared boolean default true,
  unique(family_code, place_name, title)
);

create table if not exists shared_travel_actions (
  id bigserial primary key,
  family_code text not null,
  local_id bigint,
  travel_title text,
  place_name text,
  kind text,
  title text not null,
  latitude double precision,
  longitude double precision,
  radius_meters integer default 300,
  day_index integer default 1,
  done boolean default false,
  completed_at bigint,
  note text,
  updated_at bigint,
  updated_by text,
  deleted_at bigint,
  sync_status text default 'synced',
  assignee text default '전체',
  shared boolean default true,
  unique(family_code, travel_title, place_name, title)
);
```

## 3. v15 동작

- 앱 시작 시 자동 동기화
- 저장/완료/삭제 시 자동 동기화
- 삭제는 로컬 `ItemSyncState`에 soft delete 기록
- Supabase 업로드는 `on_conflict`로 중복 방지
- Day별 여행 타임라인 표시
- 권한 진단 패널 표시
- 알림 액션 버튼 최소 구조 제공
- 홈 위젯 최소 구조 제공

## 4. 다음 단계

v15.1에서는 실제 빌드 에러가 있으면 수정하고, v15.2에서는 알림 액션을 실제 항목 ID와 연결하면 됩니다.


## v16 담당자/공유 관리

v16부터 앱 카드에서 아래 값을 직접 바꿀 수 있습니다.

- assignee: 전체 / 아빠 / 엄마 / 아이 / 나
- shared: 가족 공유 / 나만 보기

`shared=false`인 항목은 앱의 Supabase snapshot 업로드 대상에서 제외됩니다.  
업로드되는 항목은 `assignee`, `shared`, `updated_by`, `deleted_at`, `sync_status` 컬럼에 메타 정보가 같이 저장됩니다.
