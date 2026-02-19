package com.example.quanlylichhoc.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "QuanLyLichHoc.db"
        private const val DATABASE_VERSION = 4

        // Table Names
        const val TABLE_SUBJECTS = "subjects"
        const val TABLE_CLASSES = "classes"
        const val TABLE_TASKS = "tasks"
        const val TABLE_EXAMS = "exams"

        // Common Columns
        const val KEY_ID = "id"
        const val KEY_SUBJECT_ID = "subject_id"

        // Subjects Table Columns
        const val KEY_SUBJECT_NAME = "name"
        const val KEY_SUBJECT_TEACHER = "teacher"
        const val KEY_SUBJECT_ROOM = "room"
        const val KEY_SUBJECT_COLOR = "color"
        const val KEY_SUBJECT_START = "start_date"
        const val KEY_SUBJECT_END = "end_date"
        const val KEY_SUBJECT_NOTE = "note"

        // Classes Table Columns
        const val KEY_CLASS_DAY = "day_of_week" // 2=Mon, 3=Tue, ...
        const val KEY_CLASS_START = "start_time"
        const val KEY_CLASS_END = "end_time"
        const val KEY_CLASS_ROOM = "room"

        // Tasks Table Columns
        const val KEY_TASK_TITLE = "title"
        const val KEY_TASK_DESC = "description"
        const val KEY_TASK_PRIORITY = "priority"
        const val KEY_TASK_DEADLINE = "deadline"
        const val KEY_TASK_COMPLETED = "is_completed"

        // Exams Table Columns
        const val KEY_EXAM_TYPE = "exam_type"
        const val KEY_EXAM_DATE = "date"
        const val KEY_EXAM_TIME = "time"
        const val KEY_EXAM_DURATION = "duration"
        const val KEY_EXAM_ROOM = "room" // Can be different from subject room
        const val KEY_EXAM_SBD = "sbd"
        const val KEY_EXAM_NOTE = "note"
    }

    override fun onCreate(db: SQLiteDatabase) {
        createTables(db)
        // seedData(db) // Removed per user request
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXAMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CLASSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SUBJECTS")
        onCreate(db)
    }

    private fun createTables(db: SQLiteDatabase) {
        val createSubjects = ("CREATE TABLE " + TABLE_SUBJECTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_SUBJECT_NAME + " TEXT,"
                + KEY_SUBJECT_TEACHER + " TEXT,"
                + KEY_SUBJECT_ROOM + " TEXT,"
                + KEY_SUBJECT_COLOR + " TEXT,"
                + KEY_SUBJECT_START + " TEXT,"
                + KEY_SUBJECT_END + " TEXT,"
                + KEY_SUBJECT_NOTE + " TEXT" + ")")

        val createClasses = ("CREATE TABLE " + TABLE_CLASSES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_SUBJECT_ID + " INTEGER,"
                + KEY_CLASS_DAY + " INTEGER,"
                + KEY_CLASS_START + " TEXT,"
                + KEY_CLASS_END + " TEXT,"
                + KEY_CLASS_ROOM + " TEXT,"
                + "FOREIGN KEY($KEY_SUBJECT_ID) REFERENCES $TABLE_SUBJECTS($KEY_ID)" + ")")

        val createTasks = ("CREATE TABLE " + TABLE_TASKS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_SUBJECT_ID + " INTEGER,"
                + KEY_TASK_TITLE + " TEXT,"
                + KEY_TASK_DESC + " TEXT,"
                + KEY_TASK_PRIORITY + " TEXT,"
                + KEY_TASK_DEADLINE + " TEXT,"
                + KEY_TASK_COMPLETED + " INTEGER,"
                + "FOREIGN KEY($KEY_SUBJECT_ID) REFERENCES $TABLE_SUBJECTS($KEY_ID)" + ")")

        val createExams = ("CREATE TABLE " + TABLE_EXAMS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_SUBJECT_ID + " INTEGER,"
                + KEY_EXAM_TYPE + " TEXT,"
                + KEY_EXAM_DATE + " TEXT,"
                + KEY_EXAM_TIME + " TEXT,"
                + KEY_EXAM_DURATION + " INTEGER,"
                + KEY_EXAM_ROOM + " TEXT,"
                + KEY_EXAM_SBD + " TEXT,"
                + KEY_EXAM_NOTE + " TEXT,"
                + "FOREIGN KEY($KEY_SUBJECT_ID) REFERENCES $TABLE_SUBJECTS($KEY_ID)" + ")")

        db.execSQL(createSubjects)
        db.execSQL(createClasses)
        db.execSQL(createTasks)
        db.execSQL(createExams)
    }

    private fun seedData(db: SQLiteDatabase) {
        // Data seeding removed
    }

    // CRUD Operations

    // --- Subjects ---
    fun insertSubject(name: String, teacher: String, room: String, color: String, startDate: String, endDate: String, note: String = ""): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_SUBJECT_NAME, name)
            put(KEY_SUBJECT_TEACHER, teacher)
            put(KEY_SUBJECT_ROOM, room)
            put(KEY_SUBJECT_COLOR, color)
            put(KEY_SUBJECT_START, startDate)
            put(KEY_SUBJECT_END, endDate)
            put(KEY_SUBJECT_NOTE, note)
        }
        return db.insert(TABLE_SUBJECTS, null, values)
    }
    
    fun updateSubjectNote(id: String, note: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_SUBJECT_NOTE, note)
        }
        db.update(TABLE_SUBJECTS, values, "$KEY_ID = ?", arrayOf(id))
    }
    
    fun updateSubject(id: String, name: String, teacher: String, room: String, color: String, startDate: String, endDate: String, note: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_SUBJECT_NAME, name)
            put(KEY_SUBJECT_TEACHER, teacher)
            put(KEY_SUBJECT_ROOM, room)
            put(KEY_SUBJECT_COLOR, color)
            put(KEY_SUBJECT_START, startDate)
            put(KEY_SUBJECT_END, endDate)
            put(KEY_SUBJECT_NOTE, note)
        }
        db.update(TABLE_SUBJECTS, values, "$KEY_ID = ?", arrayOf(id))
    }

    fun deleteSubject(id: String) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            // Cascade Delete: Exams -> Tasks -> Classes -> Subject
            db.delete(TABLE_EXAMS, "$KEY_SUBJECT_ID = ?", arrayOf(id))
            db.delete(TABLE_TASKS, "$KEY_SUBJECT_ID = ?", arrayOf(id))
            db.delete(TABLE_CLASSES, "$KEY_SUBJECT_ID = ?", arrayOf(id))
            db.delete(TABLE_SUBJECTS, "$KEY_ID = ?", arrayOf(id))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getSubjectNames(): List<String> {
        val names = ArrayList<String>()
        val selectQuery = "SELECT $KEY_SUBJECT_NAME FROM $TABLE_SUBJECTS"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                names.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return names
    }

    fun getSubjectIdByName(name: String): Long {
         val db = this.readableDatabase
         val cursor = db.query(TABLE_SUBJECTS, arrayOf(KEY_ID), "$KEY_SUBJECT_NAME=?", arrayOf(name), null, null, null)
         var id: Long = -1
         if (cursor.moveToFirst()) {
             id = cursor.getLong(0)
         }
         cursor.close()
         return id
    }

    // --- Classes ---
    fun insertClass(subjectId: Long, day: Int, start: String, end: String, room: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_SUBJECT_ID, subjectId)
            put(KEY_CLASS_DAY, day)
            put(KEY_CLASS_START, start)
            put(KEY_CLASS_END, end)
            put(KEY_CLASS_ROOM, room)
        }
        return db.insert(TABLE_CLASSES, null, values)
    }

    fun getSubjectIdByClassId(classId: String): Long {
         val db = this.readableDatabase
         val cursor = db.query(TABLE_CLASSES, arrayOf(KEY_SUBJECT_ID), "$KEY_ID=?", arrayOf(classId), null, null, null)
         var id: Long = -1
         if (cursor.moveToFirst()) {
             id = cursor.getLong(0)
         }
         cursor.close()
         return id
    }
    
    fun deleteClassesBySubjectId(subjectId: String) {
        val db = this.writableDatabase
        db.delete(TABLE_CLASSES, "$KEY_SUBJECT_ID = ?", arrayOf(subjectId))
    }

    fun deleteClass(id: String) {
        val db = this.writableDatabase
        db.delete(TABLE_CLASSES, "$KEY_ID = ?", arrayOf(id))
    }

    fun getAllClasses(): List<com.example.quanlylichhoc.utils.ClassItem> {
        val classes = ArrayList<com.example.quanlylichhoc.utils.ClassItem>()
        val selectQuery = "SELECT c.*, s.$KEY_SUBJECT_NAME, s.$KEY_SUBJECT_TEACHER, s.$KEY_SUBJECT_COLOR, s.$KEY_SUBJECT_START, s.$KEY_SUBJECT_END FROM $TABLE_CLASSES c INNER JOIN $TABLE_SUBJECTS s ON c.$KEY_SUBJECT_ID = s.$KEY_ID ORDER BY c.$KEY_CLASS_START ASC"
        
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID))
                val subjectName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_NAME))
                val teacher = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_TEACHER))
                val room = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLASS_ROOM))
                val startTime = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLASS_START))
                val endTime = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLASS_END))
                val color = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_COLOR))
                val day = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CLASS_DAY))
                
                // Safe handling for potentially missing columns if user hasn't re-created/migrated fully or if data is missing
                var startDate = ""
                var endDate = ""
                try {
                     startDate = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_START)) ?: ""
                     endDate = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_END)) ?: ""
                } catch (e: Exception) {
                    // Column might not exist if migration failed or old data, ignore
                }

                val calendar = java.util.Calendar.getInstance()
                val currentDay = calendar.get(java.util.Calendar.DAY_OF_WEEK) // Sun=1, Mon=2
                val isToday = (day == currentDay)
                
                classes.add(com.example.quanlylichhoc.utils.ClassItem(
                    id, subjectName, room, teacher, startTime, endTime, day, isToday, startDate, endDate, color
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return classes
    }
    
    fun checkTimeConflict(day: Int, newStartTime: String, newEndTime: String, newStartDate: String, newEndDate: String, excludeSubjectId: Long = -1): Boolean {
        val db = this.readableDatabase
        val query = "SELECT c.$KEY_CLASS_START, c.$KEY_CLASS_END, s.$KEY_SUBJECT_START, s.$KEY_SUBJECT_END " +
                    "FROM $TABLE_CLASSES c " +
                    "INNER JOIN $TABLE_SUBJECTS s ON c.$KEY_SUBJECT_ID = s.$KEY_ID " +
                    "WHERE c.$KEY_CLASS_DAY = ? AND c.$KEY_SUBJECT_ID != ?"
        
        val cursor = db.rawQuery(query, arrayOf(day.toString(), excludeSubjectId.toString()))
        val sdfDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())

        var isConflict = false
        if (cursor.moveToFirst()) {
            do {
                val existStartTime = cursor.getString(0)
                val existEndTime = cursor.getString(1)
                val existStartDateStr = cursor.getString(2) ?: ""
                val existEndDateStr = cursor.getString(3) ?: ""
                
                // 1. Time Overlap Check: (StartA < EndB) and (EndA > StartB)
                if (newStartTime < existEndTime && newEndTime > existStartTime) {
                    
                    // 2. Date Overlap Check
                    // If data is missing, assume overlap to be safe
                     if (newStartDate.isEmpty() || newEndDate.isEmpty() || existStartDateStr.isEmpty() || existEndDateStr.isEmpty()) {
                        isConflict = true
                        break
                    }

                    try {
                        val newStart = sdfDate.parse(newStartDate)
                        val newEnd = sdfDate.parse(newEndDate)
                        val existStart = sdfDate.parse(existStartDateStr)
                        val existEnd = sdfDate.parse(existEndDateStr)
                        
                        // Check Overlap: (StartA <= EndB) and (EndA >= StartB)
                        if (newStart != null && newEnd != null && existStart != null && existEnd != null) {
                            if (!newStart.after(existEnd) && !newEnd.before(existStart)) {
                                isConflict = true
                                break
                            }
                        }
                    } catch (e: Exception) {
                        isConflict = true
                        break
                    }
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return isConflict
    }

    // --- Tasks ---
    fun insertTask(subjectId: Long, title: String, desc: String, priority: String, deadline: String, isCompleted: Boolean): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_SUBJECT_ID, subjectId)
            put(KEY_TASK_TITLE, title)
            put(KEY_TASK_DESC, desc)
            put(KEY_TASK_PRIORITY, priority)
            put(KEY_TASK_DEADLINE, deadline)
            put(KEY_TASK_COMPLETED, if (isCompleted) 1 else 0)
        }
        return db.insert(TABLE_TASKS, null, values)
    }
    
    fun updateTask(id: String, title: String, desc: String, priority: String, deadline: String, subjectId: Long = -1) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
             put(KEY_TASK_TITLE, title)
             put(KEY_TASK_DESC, desc)
             put(KEY_TASK_PRIORITY, priority)
             put(KEY_TASK_DEADLINE, deadline)
             if (subjectId != -1L) {
                 put(KEY_SUBJECT_ID, subjectId)
             }
        }
        db.update(TABLE_TASKS, values, "$KEY_ID = ?", arrayOf(id))
    }

    fun updateTaskStatus(id: String, isCompleted: Boolean) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_TASK_COMPLETED, if (isCompleted) 1 else 0)
        }
        db.update(TABLE_TASKS, values, "$KEY_ID = ?", arrayOf(id))
    }

    fun getTaskById(id: String): com.example.quanlylichhoc.utils.TaskItem? {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_TASKS, null, "$KEY_ID = ?", arrayOf(id), null, null, null)
        var task: com.example.quanlylichhoc.utils.TaskItem? = null
        if (cursor.moveToFirst()) {
             task = com.example.quanlylichhoc.utils.TaskItem(
                id = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_TITLE)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_DESC)),
                priority = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_PRIORITY)),
                deadline = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_DEADLINE)),
                isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TASK_COMPLETED)) == 1,
                subjectId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_SUBJECT_ID))
            )
        }
        cursor.close()
        return task
    }
    
    fun deleteTask(id: String) {
        val db = this.writableDatabase
        db.delete(TABLE_TASKS, "$KEY_ID = ?", arrayOf(id))
    }

    fun getAllTasks(): List<com.example.quanlylichhoc.utils.TaskItem> {
        val tasks = ArrayList<com.example.quanlylichhoc.utils.TaskItem>()
        val selectQuery = "SELECT * FROM $TABLE_TASKS"
        
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_TITLE))
                val desc = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_DESC))
                val priority = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_PRIORITY))
                val deadline = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_DEADLINE))
                val isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TASK_COMPLETED)) == 1
                val sId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_SUBJECT_ID))
                
                tasks.add(com.example.quanlylichhoc.utils.TaskItem(
                    id = id,
                    title = title,
                    description = desc,
                    priority = priority,
                    isCompleted = isCompleted,
                    deadline = deadline,
                    subjectId = sId
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return tasks
    }

    fun getTasksBySubjectId(subjectId: String): List<com.example.quanlylichhoc.utils.TaskItem> {
        val tasks = ArrayList<com.example.quanlylichhoc.utils.TaskItem>()
        val selectQuery = "SELECT * FROM $TABLE_TASKS WHERE $KEY_SUBJECT_ID = ?"
        
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, arrayOf(subjectId))
        
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_TITLE))
                val desc = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_DESC))
                val priority = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_PRIORITY))
                val deadline = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_DEADLINE))
                val isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TASK_COMPLETED)) == 1
                val sId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_SUBJECT_ID))
                
                tasks.add(com.example.quanlylichhoc.utils.TaskItem(
                    id = id,
                    title = title,
                    description = desc,
                    priority = priority,
                    isCompleted = isCompleted,
                    deadline = deadline,
                    subjectId = sId
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return tasks
    }

    // --- Exams ---
    fun getAllExams(): List<com.example.quanlylichhoc.utils.ExamItem> {
        val exams = ArrayList<com.example.quanlylichhoc.utils.ExamItem>()
        val selectQuery = "SELECT e.*, s.$KEY_SUBJECT_NAME FROM $TABLE_EXAMS e INNER JOIN $TABLE_SUBJECTS s ON e.$KEY_SUBJECT_ID = s.$KEY_ID"
        
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_NAME))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXAM_DATE))
                val time = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXAM_TIME))
                val type = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXAM_TYPE))
                val room = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXAM_ROOM)) ?: ""
                val sbd = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXAM_SBD)) ?: ""
                
                exams.add(com.example.quanlylichhoc.utils.ExamItem(id, name, date, time, type, room, sbd))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return exams
    }

    fun insertExam(subjectId: Long, type: String, date: String, time: String, duration: Int, room: String, sbd: String = "", note: String = ""): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_SUBJECT_ID, subjectId)
            put(KEY_EXAM_TYPE, type)
            put(KEY_EXAM_DATE, date)
            put(KEY_EXAM_TIME, time)
            put(KEY_EXAM_DURATION, duration)
            put(KEY_EXAM_ROOM, room)
            put(KEY_EXAM_SBD, sbd)
            put(KEY_EXAM_NOTE, note)
        }
        return db.insert(TABLE_EXAMS, null, values)
    }
    
     fun deleteExam(id: String) {
        val db = this.writableDatabase
        db.delete(TABLE_EXAMS, "$KEY_ID = ?", arrayOf(id))
    }

    // --- Subjects List with Schedule ---
    fun getAllSubjects(): List<SubjectItem> {
        val subjects = ArrayList<SubjectItem>()
        val selectQuery = "SELECT * FROM $TABLE_SUBJECTS"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_NAME))
                val teacher = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_TEACHER))
                val room = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_ROOM))
                val color = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_COLOR))
                var startDate = ""
                var endDate = ""
                var note = ""
                try {
                     startDate = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_START)) ?: ""
                     endDate = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_END)) ?: ""
                     note = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_NOTE)) ?: ""
                } catch (e: Exception) {
                    // Ignore
                }
                
                // Get Schedule Summary
                val scheduleSummary = getScheduleSummary(id)

                subjects.add(SubjectItem(id, name, teacher, room, color, scheduleSummary, startDate, endDate, note))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return subjects
    }

    private fun getScheduleSummary(subjectId: String): String {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_CLASSES, arrayOf(KEY_CLASS_DAY, KEY_CLASS_START, KEY_CLASS_END), "$KEY_SUBJECT_ID=?", arrayOf(subjectId), null, null, "$KEY_CLASS_DAY ASC")
        val builder = StringBuilder()
        
        if (cursor.moveToFirst()) {
            do {
                val day = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CLASS_DAY))
                val start = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLASS_START))
                val end = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLASS_END))
                
                val dayStr = when(day) {
                    2 -> "T2"
                    3 -> "T3"
                    4 -> "T4"
                    5 -> "T5"
                    6 -> "T6"
                    7 -> "T7"
                    1 -> "CN"
                    else -> "?"
                }
                
                if (builder.isNotEmpty()) builder.append(", ")
                builder.append("$dayStr ($start-$end)")
            } while (cursor.moveToNext())
        }
        cursor.close()
        return builder.toString()
    }

    fun getSubjectById(id: String): SubjectItem? {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_SUBJECTS, null, "$KEY_ID=?", arrayOf(id), null, null, null)
        var subject: SubjectItem? = null
        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_NAME))
            val teacher = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_TEACHER))
            val room = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_ROOM))
            val color = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_COLOR))
            var startDate = ""
            var endDate = ""
            var note = ""
            try {
                 startDate = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_START)) ?: ""
                 endDate = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_END)) ?: ""
                 note = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_NOTE)) ?: ""
            } catch (e: Exception) { }
            
            val schedule = getScheduleSummary(id)
            subject = SubjectItem(id, name, teacher, room, color, schedule, startDate, endDate, note)
        }
        cursor.close()
        return subject
    }
}

data class SubjectItem(
    val id: String,
    val name: String,
    val teacher: String,
    val room: String,
    val color: String,
    val schedule: String,
    val startDate: String,
    val endDate: String,
    val note: String
)
