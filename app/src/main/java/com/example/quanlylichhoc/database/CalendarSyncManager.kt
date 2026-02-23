package com.example.quanlylichhoc.database

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.provider.CalendarContract
import android.util.Log
import java.util.TimeZone

class CalendarSyncManager(private val context: Context) {

    fun syncScheduleToCalendar(subjectName: String, room: String, startTimeInMillis: Long, endTimeInMillis: Long): Boolean {
        var calendarId = getPrimaryCalendarId()

        if (calendarId == -1L) {
            Log.w("CalendarSync", "Không có Lịch ưu tiên, tự tạo Lịch mới...")
            calendarId = createLocalCalendar()
        }

        if (calendarId == -1L) return false

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
            if (uri != null) {
                Log.d("CalendarSync", "=> Đã ghi sự kiện [$subjectName] vào Lịch ID: $calendarId")
                true
            } else false
        } catch (e: Exception) {
            Log.e("CalendarSync", "Lỗi ghi sự kiện: ${e.message}")
            false
        }
    }

    private fun getPrimaryCalendarId(): Long {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.ACCOUNT_TYPE,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        )

        try {
            val cursor = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI, projection, null, null, null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    var fallbackId = -1L
                    do {
                        val id = it.getLong(0)
                        val accName = it.getString(1)
                        val accType = it.getString(2)
                        val dispName = it.getString(3)

                        // IN RA LOGCAT TẤT CẢ CÁC LỊCH ĐANG CÓ TRONG MÁY
                        Log.d("CalendarSync", "QUÉT THẤY LỊCH -> ID: $id | Account: $accName | Type: $accType | Tên Lịch: $dispName")

                        if (fallbackId == -1L) fallbackId = id

                        // Ưu tiên tuyệt đối: Nếu máy ảo đã đăng nhập Gmail, lấy luôn Lịch Gmail đó
                        if (accType == "com.google") {
                            Log.d("CalendarSync", "=> QUYẾT ĐỊNH: Dùng Lịch Google (ID: $id)")
                            return id
                        }
                    } while (it.moveToNext())

                    Log.d("CalendarSync", "=> QUYẾT ĐỊNH: Không có Gmail, dùng tạm Lịch (ID: $fallbackId)")
                    return fallbackId
                }
            }
        } catch (e: Exception) {
            Log.e("CalendarSync", "Lỗi quét lịch: ${e.message}")
        }
        return -1L
    }

    private fun createLocalCalendar(): Long {
        // Đổi tên tài khoản thành "Device" để Google Calendar không nghi ngờ
        val accountName = "Device"
        val accountType = CalendarContract.ACCOUNT_TYPE_LOCAL

        val values = ContentValues().apply {
            put(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
            put(CalendarContract.Calendars.ACCOUNT_TYPE, accountType)
            put(CalendarContract.Calendars.NAME, "LichHocApp")
            put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "Lịch Học App")
            put(CalendarContract.Calendars.CALENDAR_COLOR, Color.parseColor("#3B82F6"))
            put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, 700)
            put(CalendarContract.Calendars.OWNER_ACCOUNT, accountName)
            put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().id)
            put(CalendarContract.Calendars.SYNC_EVENTS, 1)
            put(CalendarContract.Calendars.VISIBLE, 1) // CỜ QUAN TRỌNG: ÉP GOOGLE CALENDAR PHẢI HIỂN THỊ
        }

        var uri = CalendarContract.Calendars.CONTENT_URI
        uri = uri.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, accountType)
            .build()

        return try {
            val resultUri = context.contentResolver.insert(uri, values)
            val newId = resultUri?.lastPathSegment?.toLongOrNull() ?: -1L
            Log.d("CalendarSync", "=> Đã khởi tạo thành công Lịch mới với ID: $newId")
            newId
        } catch (e: Exception) {
            Log.e("CalendarSync", "Lỗi tạo lịch: ${e.message}")
            -1L
        }
    }
}