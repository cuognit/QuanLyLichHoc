package com.example.quanlylichhoc.database

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.provider.CalendarContract
import android.util.Log
import java.util.TimeZone

class CalendarSyncManager(private val context: Context) {

    private val ACCOUNT_NAME = "Device"
    private val ACCOUNT_TYPE = CalendarContract.ACCOUNT_TYPE_LOCAL

    // 1. Hàm xóa sạch toàn bộ sự kiện của Lịch App
    fun clearAllEvents(calendarId: Long) {
        try {
            var uri = CalendarContract.Events.CONTENT_URI
            uri = uri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE)
                .build()

            val deletedRows = context.contentResolver.delete(
                uri,
                "${CalendarContract.Events.CALENDAR_ID} = ?",
                arrayOf(calendarId.toString())
            )
            Log.d("CalendarSync", "Đã dọn dẹp sạch sẽ: $deletedRows sự kiện cũ.")
        } catch (e: Exception) {
            Log.e("CalendarSync", "Lỗi khi dọn dẹp: ${e.message}")
        }
    }

    // 2. Hàm ghi sự kiện (Giữ nguyên logic cũ nhưng tối ưu)
    fun syncScheduleToCalendar(subjectName: String, room: String, startTimeInMillis: Long, endTimeInMillis: Long, calendarId: Long): Boolean {
        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startTimeInMillis)
            put(CalendarContract.Events.DTEND, endTimeInMillis)
            put(CalendarContract.Events.TITLE, subjectName)
            put(CalendarContract.Events.EVENT_LOCATION, room)
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }

        return try {
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            uri != null
        } catch (e: Exception) {
            false
        }
    }

    // 3. Tìm hoặc Tạo Lịch App (Luôn trả về một ID chuẩn)
    fun getOrCreateAppCalendar(): Long {
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val selection = "${CalendarContract.Calendars.ACCOUNT_NAME} = ? AND ${CalendarContract.Calendars.ACCOUNT_TYPE} = ?"

        // Tìm lịch cũ
        val cursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI, projection, selection, arrayOf(ACCOUNT_NAME, ACCOUNT_TYPE), null
        )

        cursor?.use {
            if (it.moveToFirst()) return it.getLong(0)
        }

        // Nếu không thấy thì tạo mới
        val values = ContentValues().apply {
            put(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
            put(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE)
            put(CalendarContract.Calendars.NAME, "LichHocApp")
            put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "Lịch Học App")
            put(CalendarContract.Calendars.CALENDAR_COLOR, Color.BLUE)
            put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, 700)
            put(CalendarContract.Calendars.OWNER_ACCOUNT, ACCOUNT_NAME)
            put(CalendarContract.Calendars.VISIBLE, 1)
            put(CalendarContract.Calendars.SYNC_EVENTS, 1)
        }

        var uri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE)
            .build()

        val resultUri = context.contentResolver.insert(uri, values)
        return resultUri?.lastPathSegment?.toLongOrNull() ?: -1L
    }
}