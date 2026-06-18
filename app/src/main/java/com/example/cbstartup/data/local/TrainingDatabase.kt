package com.example.cbstartup.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class TrainingDatabase(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE ${TrainingContract.Students.TABLE} (
                ${TrainingContract.Students.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${TrainingContract.Students.NAME} TEXT NOT NULL,
                ${TrainingContract.Students.EMAIL} TEXT NOT NULL UNIQUE,
                ${TrainingContract.Students.STATUS} TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE ${TrainingContract.Courses.TABLE} (
                ${TrainingContract.Courses.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${TrainingContract.Courses.TITLE} TEXT NOT NULL,
                ${TrainingContract.Courses.WORKLOAD_HOURS} INTEGER NOT NULL,
                ${TrainingContract.Courses.LEVEL} TEXT NOT NULL,
                ${TrainingContract.Courses.CATEGORY} TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE ${TrainingContract.Trails.TABLE} (
                ${TrainingContract.Trails.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${TrainingContract.Trails.NAME} TEXT NOT NULL,
                ${TrainingContract.Trails.DESCRIPTION} TEXT NOT NULL,
                ${TrainingContract.Trails.STATUS} TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE ${TrainingContract.TrailCourses.TABLE} (
                ${TrainingContract.TrailCourses.TRAIL_ID} INTEGER NOT NULL,
                ${TrainingContract.TrailCourses.COURSE_ID} INTEGER NOT NULL,
                PRIMARY KEY (${TrainingContract.TrailCourses.TRAIL_ID}, ${TrainingContract.TrailCourses.COURSE_ID}),
                FOREIGN KEY (${TrainingContract.TrailCourses.TRAIL_ID})
                    REFERENCES ${TrainingContract.Trails.TABLE}(${TrainingContract.Trails.ID}) ON DELETE CASCADE,
                FOREIGN KEY (${TrainingContract.TrailCourses.COURSE_ID})
                    REFERENCES ${TrainingContract.Courses.TABLE}(${TrainingContract.Courses.ID}) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE ${TrainingContract.Enrollments.TABLE} (
                ${TrainingContract.Enrollments.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${TrainingContract.Enrollments.STUDENT_ID} INTEGER NOT NULL,
                ${TrainingContract.Enrollments.TRAIL_ID} INTEGER NOT NULL,
                ${TrainingContract.Enrollments.STATUS} TEXT NOT NULL,
                ${TrainingContract.Enrollments.ENROLLED_AT} INTEGER NOT NULL,
                ${TrainingContract.Enrollments.FINISHED_AT} INTEGER,
                FOREIGN KEY (${TrainingContract.Enrollments.STUDENT_ID})
                    REFERENCES ${TrainingContract.Students.TABLE}(${TrainingContract.Students.ID}) ON DELETE CASCADE,
                FOREIGN KEY (${TrainingContract.Enrollments.TRAIL_ID})
                    REFERENCES ${TrainingContract.Trails.TABLE}(${TrainingContract.Trails.ID}) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE UNIQUE INDEX ${TrainingContract.Enrollments.ACTIVE_UNIQUE_INDEX}
            ON ${TrainingContract.Enrollments.TABLE} (
                ${TrainingContract.Enrollments.STUDENT_ID},
                ${TrainingContract.Enrollments.TRAIL_ID}
            )
            WHERE ${TrainingContract.Enrollments.STATUS} = 'ACTIVE'
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        DatabaseMigrations.migrate(db, oldVersion, newVersion)
    }

    companion object {
        private const val DATABASE_NAME = "cbstartup_training.db"
        private const val DATABASE_VERSION = 1
    }
}

object DatabaseMigrations {
    fun migrate(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.i("TrainingDatabase", "Applying migrations from $oldVersion to $newVersion")
        db.version = newVersion
    }
}

object TrainingContract {
    object Students {
        const val TABLE = "students"
        const val ID = "id"
        const val NAME = "name"
        const val EMAIL = "email"
        const val STATUS = "status"
    }

    object Courses {
        const val TABLE = "courses"
        const val ID = "id"
        const val TITLE = "title"
        const val WORKLOAD_HOURS = "workload_hours"
        const val LEVEL = "level"
        const val CATEGORY = "category"
    }

    object Trails {
        const val TABLE = "trails"
        const val ID = "id"
        const val NAME = "name"
        const val DESCRIPTION = "description"
        const val STATUS = "status"
    }

    object TrailCourses {
        const val TABLE = "trail_courses"
        const val TRAIL_ID = "trail_id"
        const val COURSE_ID = "course_id"
    }

    object Enrollments {
        const val TABLE = "enrollments"
        const val ID = "id"
        const val STUDENT_ID = "student_id"
        const val TRAIL_ID = "trail_id"
        const val STATUS = "status"
        const val ENROLLED_AT = "enrolled_at"
        const val FINISHED_AT = "finished_at"
        const val ACTIVE_UNIQUE_INDEX = "idx_unique_active_enrollment"
    }
}
