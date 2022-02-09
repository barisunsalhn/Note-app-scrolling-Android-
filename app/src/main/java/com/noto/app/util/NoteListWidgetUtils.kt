package com.noto.app.util

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import com.noto.app.AppActivity
import com.noto.app.R
import com.noto.app.domain.model.Library
import com.noto.app.domain.model.Note
import com.noto.app.widget.NoteListWidgetConfigActivity
import com.noto.app.widget.NoteListWidgetService

fun Context.createNoteListWidgetRemoteViews(
    appWidgetId: Int,
    isHeaderEnabled: Boolean,
    isEditWidgetButtonEnabled: Boolean,
    isAppIconEnabled: Boolean,
    isNewLibraryButtonEnabled: Boolean,
    widgetRadius: Int,
    library: Library,
    note: Note,
): RemoteViews {
    val color = colorResource(library.color.toResource())
    return RemoteViews(packageName, R.layout.widget_l).apply {
        setTextViewText(R.id.tv_library_title, library.title)
        //setTextViewText(R.id.tvContent, note.body)
        setTextColor(R.id.tv_library_title, color)
        setViewVisibility(R.id.ll_header, if (isHeaderEnabled) View.VISIBLE else View.GONE)
        setViewVisibility(R.id.iv_edit_widget, if (isEditWidgetButtonEnabled) View.VISIBLE else View.GONE)
        setViewVisibility(R.id.iv_app_icon, if (isAppIconEnabled) View.VISIBLE else View.GONE)
        setViewVisibility(R.id.fab, if (isNewLibraryButtonEnabled) View.VISIBLE else View.GONE)
        setViewVisibility(R.id.fab, if (isNewLibraryButtonEnabled) View.VISIBLE else View.GONE)
        setOnClickPendingIntent(R.id.iv_edit_widget, createEditWidgetButtonPendingIntent(appWidgetId, library.id))
        setOnClickPendingIntent(R.id.iv_app_icon, createAppLauncherPendingIntent(appWidgetId))
        setOnClickPendingIntent(R.id.tv_library_title, createLibraryLauncherPendingIntent(appWidgetId, library.id))
        setRemoteAdapter(R.id.lvContent, createNoteListServiceIntent(appWidgetId, library.id))
        setPendingIntentTemplate(R.id.lv, createNoteItemPendingIntent(appWidgetId))
        setInt(R.id.ll, SetBackgroundResourceMethodName, widgetRadius.toWidgetShapeId())
        setInt(R.id.ll_header, SetBackgroundResourceMethodName, widgetRadius.toWidgetHeaderShapeId())
        setInt(R.id.iv_edit_widget, SetColorFilterMethodName, color)
        if (isAppIconEnabled)
            setViewPadding(R.id.tv_library_title, 0.dp, 16.dp, 0.dp, 16.dp)
        else
            setViewPadding(R.id.tv_library_title, 16.dp, 16.dp, 16.dp, 16.dp)
    }
}

private fun Context.createEditWidgetButtonPendingIntent(appWidgetId: Int, libraryId: Long): PendingIntent? {
    val intent = Intent(
        AppWidgetManager.ACTION_APPWIDGET_CONFIGURE,
        null,
        this,
        NoteListWidgetConfigActivity::class.java
    ).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        putExtra(Constants.LibraryId, libraryId)
        data = Uri.parse(this.toUri(Intent.URI_INTENT_SCHEME))
    }
    return PendingIntent.getActivity(this, appWidgetId, intent, PendingIntentFlags)
}

private fun Context.createLibraryLauncherPendingIntent(appWidgetId: Int, libraryId: Long): PendingIntent? {
    val intent = Intent(Constants.Intent.ActionOpenLibrary, null, this, AppActivity::class.java).apply {
        putExtra(Constants.LibraryId, libraryId)
    }
    return PendingIntent.getActivity(this, appWidgetId, intent, PendingIntentFlags)
}

private fun Context.createNewNoteButtonPendingIntent(appWidgetId: Int, libraryId: Long): PendingIntent? {
    val intent = Intent(
        Constants.Intent.ActionCreateNote,
        null,
        this,
        AppActivity::class.java
    ).apply {
        putExtra(Constants.LibraryId, libraryId)
    }
    return PendingIntent.getActivity(this, appWidgetId, intent, PendingIntentFlags)
}

private fun Context.createNoteListServiceIntent(appWidgetId: Int, libraryId: Long): Intent {
    return Intent(this, NoteListWidgetService::class.java).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        putExtra(Constants.LibraryId, libraryId)
        data = Uri.parse(this.toUri(Intent.URI_INTENT_SCHEME))
    }
}

private fun Context.createNoteItemPendingIntent(appWidgetId: Int): PendingIntent? {
    val intent = Intent(
        Constants.Intent.ActionOpenNote,
        null,
        this,
        AppActivity::class.java
    ).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        data = Uri.parse(this.toUri(Intent.URI_INTENT_SCHEME))
    }
    return PendingIntent.getActivity(this, appWidgetId, intent, PendingIntentFlags)
}