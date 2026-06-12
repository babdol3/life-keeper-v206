package com.ultratul.lifekeeper.util

import java.net.URLDecoder

data class SharedPlaceCandidate(
    val name: String,
    val category: String,
    val latitude: Double?,
    val longitude: Double?,
    val rawText: String
)

object SharedPlaceParser {
    private val latLngPatterns = listOf(
        Regex("""@(-?\d{1,3}\.\d+),\s*(-?\d{1,3}\.\d+)"""),
        Regex("""[?&](?:q|query|ll|center)=(-?\d{1,3}\.\d+),\s*(-?\d{1,3}\.\d+)"""),
        Regex("""!3d(-?\d{1,3}\.\d+)!4d(-?\d{1,3}\.\d+)"""),
        Regex("""(-?\d{1,3}\.\d+),\s*(-?\d{1,3}\.\d+)""")
    )

    fun parse(text: String): SharedPlaceCandidate {
        val decoded = runCatching { URLDecoder.decode(text, "UTF-8") }.getOrDefault(text)
        val coordinate = extractCoordinate(decoded)
        val name = extractName(decoded)
        return SharedPlaceCandidate(
            name = name,
            category = inferCategory(name),
            latitude = coordinate?.first,
            longitude = coordinate?.second,
            rawText = text
        )
    }

    private fun extractCoordinate(text: String): Pair<Double, Double>? {
        for (pattern in latLngPatterns) {
            val match = pattern.find(text) ?: continue
            val lat = match.groupValues.getOrNull(1)?.toDoubleOrNull()
            val lng = match.groupValues.getOrNull(2)?.toDoubleOrNull()
            if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
                return lat to lng
            }
        }
        return null
    }

    private fun extractName(text: String): String {
        val lines = text
            .split('\n', '\r')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { it.startsWith("http://") || it.startsWith("https://") }
            .filterNot { it.equals("Google Maps", ignoreCase = true) }
            .filterNot { it.contains("지도", ignoreCase = true) && it.length < 5 }

        val first = lines.firstOrNull()
            ?.replace("장소", "")
            ?.replace("공유", "")
            ?.trim()

        return first.takeUnless { it.isNullOrBlank() } ?: "공유받은 장소"
    }

    fun inferCategory(name: String): String {
        val n = name.lowercase()
        return when {
            name.contains("다이소") -> "다이소"
            name.contains("약국") || name.contains("약") && name.contains("국") -> "약국"
            name.contains("이마트") || name.contains("홈플러스") || name.contains("롯데마트") || name.contains("마트") -> "마트"
            name.contains("편의점") || n.contains("cu") || n.contains("gs25") || name.contains("세븐일레븐") || name.contains("이마트24") -> "편의점"
            name.contains("학교") || name.contains("초등") || name.contains("중학교") || name.contains("고등학교") -> "학교"
            name.contains("회사") || name.contains("오피스") || n.contains("office") -> "회사"
            name.contains("집") || name.contains("아파트") -> "집"
            else -> "기타"
        }
    }
}
