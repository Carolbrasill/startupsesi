package com.example.cbstartup.data.local.dao

import android.content.ContentValues
import android.database.Cursor
import com.example.cbstartup.data.local.TrainingContract
import com.example.cbstartup.data.local.TrainingDatabase
import com.example.cbstartup.data.local.entity.CourseEntity
import com.example.cbstartup.data.local.entity.EnrollmentEntity
import com.example.cbstartup.data.local.entity.StudentEntity
import com.example.cbstartup.data.local.entity.TrailEntity
import com.example.cbstartup.domain.model.EnrollmentStatus

class StudentDao(private val database: TrainingDatabase) {
    fun findAll(): List<StudentEntity> =
        database.readableDatabase
            .query(
                TrainingContract.Students.TABLE,
                null,
                null,
                null,
                null,
                null,
                "${TrainingContract.Students.NAME} COLLATE NOCASE ASC"
            )
            .use { cursor -> cursor.mapRows { it.toStudentEntity() } }

    fun findById(id: Long): StudentEntity? =
        database.readableDatabase
            .query(
                TrainingContract.Students.TABLE,
                null,
                "${TrainingContract.Students.ID} = ?",
                arrayOf(id.toString()),
                null,
                null,
                null
            )
            .use { cursor -> if (cursor.moveToFirst()) cursor.toStudentEntity() else null }

    fun insert(entity: StudentEntity): Long {
        return database.writableDatabase.insertOrThrow(
            TrainingContract.Students.TABLE,
            null,
            entity.toValues(includeId = false)
        )
    }

    fun update(entity: StudentEntity): Boolean {
        val affected = database.writableDatabase.update(
            TrainingContract.Students.TABLE,
            entity.toValues(includeId = false),
            "${TrainingContract.Students.ID} = ?",
            arrayOf(entity.id.toString())
        )
        return affected > 0
    }

    fun delete(id: Long): Boolean {
        val affected = database.writableDatabase.delete(
            TrainingContract.Students.TABLE,
            "${TrainingContract.Students.ID} = ?",
            arrayOf(id.toString())
        )
        return affected > 0
    }
}

class CourseDao(private val database: TrainingDatabase) {
    fun findAll(): List<CourseEntity> =
        database.readableDatabase
            .query(
                TrainingContract.Courses.TABLE,
                null,
                null,
                null,
                null,
                null,
                "${TrainingContract.Courses.TITLE} COLLATE NOCASE ASC"
            )
            .use { cursor -> cursor.mapRows { it.toCourseEntity() } }

    fun findById(id: Long): CourseEntity? =
        database.readableDatabase
            .query(
                TrainingContract.Courses.TABLE,
                null,
                "${TrainingContract.Courses.ID} = ?",
                arrayOf(id.toString()),
                null,
                null,
                null
            )
            .use { cursor -> if (cursor.moveToFirst()) cursor.toCourseEntity() else null }

    fun findByTrailId(trailId: Long): List<CourseEntity> {
        val sql = """
            SELECT c.*
            FROM ${TrainingContract.Courses.TABLE} c
            INNER JOIN ${TrainingContract.TrailCourses.TABLE} tc
                ON tc.${TrainingContract.TrailCourses.COURSE_ID} = c.${TrainingContract.Courses.ID}
            WHERE tc.${TrainingContract.TrailCourses.TRAIL_ID} = ?
            ORDER BY c.${TrainingContract.Courses.TITLE} COLLATE NOCASE ASC
        """.trimIndent()
        return database.readableDatabase
            .rawQuery(sql, arrayOf(trailId.toString()))
            .use { cursor -> cursor.mapRows { it.toCourseEntity() } }
    }

    fun insert(entity: CourseEntity): Long {
        return database.writableDatabase.insertOrThrow(
            TrainingContract.Courses.TABLE,
            null,
            entity.toValues(includeId = false)
        )
    }

    fun update(entity: CourseEntity): Boolean {
        val affected = database.writableDatabase.update(
            TrainingContract.Courses.TABLE,
            entity.toValues(includeId = false),
            "${TrainingContract.Courses.ID} = ?",
            arrayOf(entity.id.toString())
        )
        return affected > 0
    }

    fun delete(id: Long): Boolean {
        val affected = database.writableDatabase.delete(
            TrainingContract.Courses.TABLE,
            "${TrainingContract.Courses.ID} = ?",
            arrayOf(id.toString())
        )
        return affected > 0
    }
}

class TrailDao(
    private val database: TrainingDatabase,
    private val courseDao: CourseDao
) {
    fun findAll(): List<TrailEntity> =
        database.readableDatabase
            .query(
                TrainingContract.Trails.TABLE,
                null,
                null,
                null,
                null,
                null,
                "${TrainingContract.Trails.NAME} COLLATE NOCASE ASC"
            )
            .use { cursor -> cursor.mapRows { it.toTrailEntity() } }

    fun findById(id: Long): TrailEntity? =
        database.readableDatabase
            .query(
                TrainingContract.Trails.TABLE,
                null,
                "${TrainingContract.Trails.ID} = ?",
                arrayOf(id.toString()),
                null,
                null,
                null
            )
            .use { cursor -> if (cursor.moveToFirst()) cursor.toTrailEntity() else null }

    fun insert(entity: TrailEntity): Long {
        return database.writableDatabase.insertOrThrow(
            TrainingContract.Trails.TABLE,
            null,
            entity.toValues(includeId = false)
        )
    }

    fun update(entity: TrailEntity): Boolean {
        val affected = database.writableDatabase.update(
            TrainingContract.Trails.TABLE,
            entity.toValues(includeId = false),
            "${TrainingContract.Trails.ID} = ?",
            arrayOf(entity.id.toString())
        )
        return affected > 0
    }

    fun delete(id: Long): Boolean {
        val affected = database.writableDatabase.delete(
            TrainingContract.Trails.TABLE,
            "${TrainingContract.Trails.ID} = ?",
            arrayOf(id.toString())
        )
        return affected > 0
    }

    fun addCourse(trailId: Long, courseId: Long): Boolean {
        val values = ContentValues().apply {
            put(TrainingContract.TrailCourses.TRAIL_ID, trailId)
            put(TrainingContract.TrailCourses.COURSE_ID, courseId)
        }
        return database.writableDatabase.insert(
            TrainingContract.TrailCourses.TABLE,
            null,
            values
        ) != -1L
    }

    fun removeCourse(trailId: Long, courseId: Long): Boolean {
        val affected = database.writableDatabase.delete(
            TrainingContract.TrailCourses.TABLE,
            "${TrainingContract.TrailCourses.TRAIL_ID} = ? AND ${TrainingContract.TrailCourses.COURSE_ID} = ?",
            arrayOf(trailId.toString(), courseId.toString())
        )
        return affected > 0
    }

    fun hasCourse(trailId: Long, courseId: Long): Boolean {
        return database.readableDatabase
            .query(
                TrainingContract.TrailCourses.TABLE,
                arrayOf(TrainingContract.TrailCourses.TRAIL_ID),
                "${TrainingContract.TrailCourses.TRAIL_ID} = ? AND ${TrainingContract.TrailCourses.COURSE_ID} = ?",
                arrayOf(trailId.toString(), courseId.toString()),
                null,
                null,
                null
            )
            .use { cursor -> cursor.moveToFirst() }
    }

    fun coursesForTrail(trailId: Long) = courseDao.findByTrailId(trailId)
}

class EnrollmentDao(private val database: TrainingDatabase) {
    fun findAll(): List<EnrollmentEntity> =
        database.readableDatabase
            .query(
                TrainingContract.Enrollments.TABLE,
                null,
                null,
                null,
                null,
                null,
                "${TrainingContract.Enrollments.ENROLLED_AT} DESC"
            )
            .use { cursor -> cursor.mapRows { it.toEnrollmentEntity() } }

    fun findById(id: Long): EnrollmentEntity? =
        database.readableDatabase
            .query(
                TrainingContract.Enrollments.TABLE,
                null,
                "${TrainingContract.Enrollments.ID} = ?",
                arrayOf(id.toString()),
                null,
                null,
                null
            )
            .use { cursor -> if (cursor.moveToFirst()) cursor.toEnrollmentEntity() else null }

    fun findActiveByStudentAndTrail(studentId: Long, trailId: Long): EnrollmentEntity? =
        database.readableDatabase
            .query(
                TrainingContract.Enrollments.TABLE,
                null,
                "${TrainingContract.Enrollments.STUDENT_ID} = ? AND ${TrainingContract.Enrollments.TRAIL_ID} = ? AND ${TrainingContract.Enrollments.STATUS} = ?",
                arrayOf(studentId.toString(), trailId.toString(), EnrollmentStatus.ACTIVE.name),
                null,
                null,
                null
            )
            .use { cursor -> if (cursor.moveToFirst()) cursor.toEnrollmentEntity() else null }

    fun countByTrailAndStatus(trailId: Long, status: EnrollmentStatus): Int {
        val sql = """
            SELECT COUNT(*) AS total
            FROM ${TrainingContract.Enrollments.TABLE}
            WHERE ${TrainingContract.Enrollments.TRAIL_ID} = ?
                AND ${TrainingContract.Enrollments.STATUS} = ?
        """.trimIndent()
        return database.readableDatabase
            .rawQuery(sql, arrayOf(trailId.toString(), status.name))
            .use { cursor -> if (cursor.moveToFirst()) cursor.getInt(0) else 0 }
    }

    fun insert(entity: EnrollmentEntity): Long {
        return database.writableDatabase.insertOrThrow(
            TrainingContract.Enrollments.TABLE,
            null,
            entity.toValues(includeId = false)
        )
    }

    fun updateStatus(id: Long, status: EnrollmentStatus, finishedAt: Long?): Boolean {
        val values = ContentValues().apply {
            put(TrainingContract.Enrollments.STATUS, status.name)
            if (finishedAt == null) {
                putNull(TrainingContract.Enrollments.FINISHED_AT)
            } else {
                put(TrainingContract.Enrollments.FINISHED_AT, finishedAt)
            }
        }
        val affected = database.writableDatabase.update(
            TrainingContract.Enrollments.TABLE,
            values,
            "${TrainingContract.Enrollments.ID} = ?",
            arrayOf(id.toString())
        )
        return affected > 0
    }

    fun deleteByTrail(idTrail: Long) {
        database.writableDatabase.delete(
            TrainingContract.Enrollments.TABLE,
            "${TrainingContract.Enrollments.TRAIL_ID} = ?",
            arrayOf(idTrail.toString())
        )
    }

    fun deleteByStudent(idStudent: Long) {
        database.writableDatabase.delete(
            TrainingContract.Enrollments.TABLE,
            "${TrainingContract.Enrollments.STUDENT_ID} = ?",
            arrayOf(idStudent.toString())
        )
    }
}

private fun Cursor.toStudentEntity(): StudentEntity = StudentEntity(
    id = getLong(column(TrainingContract.Students.ID)),
    name = getString(column(TrainingContract.Students.NAME)),
    email = getString(column(TrainingContract.Students.EMAIL)),
    status = getString(column(TrainingContract.Students.STATUS))
)

private fun Cursor.toCourseEntity(): CourseEntity = CourseEntity(
    id = getLong(column(TrainingContract.Courses.ID)),
    title = getString(column(TrainingContract.Courses.TITLE)),
    workloadHours = getInt(column(TrainingContract.Courses.WORKLOAD_HOURS)),
    level = getString(column(TrainingContract.Courses.LEVEL)),
    category = getString(column(TrainingContract.Courses.CATEGORY))
)

private fun Cursor.toTrailEntity(): TrailEntity = TrailEntity(
    id = getLong(column(TrainingContract.Trails.ID)),
    name = getString(column(TrainingContract.Trails.NAME)),
    description = getString(column(TrainingContract.Trails.DESCRIPTION)),
    status = getString(column(TrainingContract.Trails.STATUS))
)

private fun Cursor.toEnrollmentEntity(): EnrollmentEntity = EnrollmentEntity(
    id = getLong(column(TrainingContract.Enrollments.ID)),
    studentId = getLong(column(TrainingContract.Enrollments.STUDENT_ID)),
    trailId = getLong(column(TrainingContract.Enrollments.TRAIL_ID)),
    status = getString(column(TrainingContract.Enrollments.STATUS)),
    enrolledAt = getLong(column(TrainingContract.Enrollments.ENROLLED_AT)),
    finishedAt = column(TrainingContract.Enrollments.FINISHED_AT).let { index ->
        if (isNull(index)) null else getLong(index)
    }
)

private fun StudentEntity.toValues(includeId: Boolean): ContentValues =
    ContentValues().apply {
        if (includeId) put(TrainingContract.Students.ID, id)
        put(TrainingContract.Students.NAME, name)
        put(TrainingContract.Students.EMAIL, email)
        put(TrainingContract.Students.STATUS, status)
    }

private fun CourseEntity.toValues(includeId: Boolean): ContentValues =
    ContentValues().apply {
        if (includeId) put(TrainingContract.Courses.ID, id)
        put(TrainingContract.Courses.TITLE, title)
        put(TrainingContract.Courses.WORKLOAD_HOURS, workloadHours)
        put(TrainingContract.Courses.LEVEL, level)
        put(TrainingContract.Courses.CATEGORY, category)
    }

private fun TrailEntity.toValues(includeId: Boolean): ContentValues =
    ContentValues().apply {
        if (includeId) put(TrainingContract.Trails.ID, id)
        put(TrainingContract.Trails.NAME, name)
        put(TrainingContract.Trails.DESCRIPTION, description)
        put(TrainingContract.Trails.STATUS, status)
    }

private fun EnrollmentEntity.toValues(includeId: Boolean): ContentValues =
    ContentValues().apply {
        if (includeId) put(TrainingContract.Enrollments.ID, id)
        put(TrainingContract.Enrollments.STUDENT_ID, studentId)
        put(TrainingContract.Enrollments.TRAIL_ID, trailId)
        put(TrainingContract.Enrollments.STATUS, status)
        put(TrainingContract.Enrollments.ENROLLED_AT, enrolledAt)
        if (finishedAt == null) {
            putNull(TrainingContract.Enrollments.FINISHED_AT)
        } else {
            put(TrainingContract.Enrollments.FINISHED_AT, finishedAt)
        }
    }

private inline fun <T> Cursor.mapRows(mapper: (Cursor) -> T): List<T> {
    val items = mutableListOf<T>()
    while (moveToNext()) {
        items.add(mapper(this))
    }
    return items
}

private fun Cursor.column(name: String): Int = getColumnIndexOrThrow(name)
