package com.ultratul.lifekeeper.family

/**
 * v13 Supabase 가족 공유 설정.
 *
 * 1) Supabase 프로젝트 생성
 * 2) Project Settings > API 에서 Project URL / anon public key 복사
 * 3) 아래 값에 붙여넣기
 *
 * 예:
 * const val SUPABASE_URL = "https://xxxx.supabase.co"
 * const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIs..."
 */
object SupabaseConfig {
    const val SUPABASE_URL = ""
    const val SUPABASE_ANON_KEY = ""

    val isConfigured: Boolean
        get() = SUPABASE_URL.startsWith("https://") && SUPABASE_ANON_KEY.length > 20
}
