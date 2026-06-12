package com.ultratul.lifekeeper.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.ultratul.lifekeeper.R

/**
 * v15 홈 화면 위젯 최소 구조.
 * 현재는 고정 문구를 보여주고, 다음 단계에서 Room 데이터와 연결하면 됩니다.
 */
class LifeKeeperWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_life_keeper)
            views.setTextViewText(R.id.widgetTitle, "생활비서")
            views.setTextViewText(R.id.widgetBody, "오늘 할 일 · 마트 구매목록 · 다음 알림")
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
