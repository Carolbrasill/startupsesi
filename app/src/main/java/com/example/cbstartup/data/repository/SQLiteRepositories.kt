package com.example.cbstartup.data.repository

import android.database.sqlite.SQLiteException
import android.util.Log
import com.example.cbstartup.data.local.dao.CourseDao
import com.example.cbstartup.data.local.dao.EnrollmentDao
import com.example.cbstartup.data.local.dao.StudentDao
import com.example.cbstartup.data.local.dao.TrailDao
import com.example.cbstartup.data.local.entity.CourseEntity
import com.example.cbstartup.data.local.entity.EnrollmentEntity
import com.example.cbstartup.data.local.entity.StudentEntity
import com.example.cbstartup.data.local.entity.TrailEntity
import com.example.cbstartup.domain.model.DashboardStats
import com.example.cbstartup.domain.model.Enrollment
import com.example.cbstartup.domain.model.EnrollmentRecord
import com.example.cbstartup.domain.model.EnrollmentStatus
import com.example.cbstartup.domain.model.StudentStatus
import com.example.cbstartup.domain.model.Trail
import com.example.cbstartup.domain.model.TrailStatus
import com.example.cbstartup.domain.repository.AnalyticsRepository
import com.example.cbstartup.domain.repository.CourseRepository
import com.example.cbstartup.domain.repository.EnrollmentRepository
import com.example.cbstartup.domain.repository.StudentRepository
import com.example.cbstartup.domain.repository.TrailRepository
import com.example.cbstartup.domain.model.Course
import com.example.cbstartup.domain.model.Student

class SQLiteStudentRepository(
    private val dao: StudentDao
) : StudentRepository {
    override fun getAll(): List<Student> =
        runCatching { dao.findAll().map { it.toDomain() } }
            .getOrElse { logAndReturn("students", it, emptyList()) }

    override fun getById(id: Long): Student? =
        runCatching { dao.findById(id)?.toDomain() }
            .getOrElse { logAndReturn("student", it, null) }

    override fun save(student: Student): Long =
        runCatching { dao.insert(StudentEntity.fromDomain(student)) }
            .getOrElse { throwDataError("save student", it) }

    override fun update(student: Student): Boolean =
        runCatching { dao.update(StudentEntity.fromDomain(student)) }
            .getOrElse { throwDataError("update student", it) }

    override fun delete(id: Long): Boolean =
        runCatching { dao.delete(id) }
            .getOrElse { throwDataError("delete student", it) }
}

class SQLiteCourseRepository(
    private val dao: CourseDao
) : CourseRepository {
    override fun getAll(): List<Course> =
        runCatching { dao.findAll().map { it.toDomain() } }
            .getOrElse { logAndReturn("courses", it, emptyList()) }

    override fun getById(id: Long): Course? =
        runCatching { dao.findById(id)?.toDomain() }
            .getOrElse { logAndReturn("course", it, null) }

    override fun save(course: Course): Long =
        runCatching { dao.insert(CourseEntity.fromDomain(course)) }
            .getOrElse { throwDataError("save course", it) }

    override fun update(course: Course): Boolean =
        runCatching { dao.update(CourseEntity.fromDomain(course)) }
            .getOrElse { throwDataError("update course", it) }

    override fun delete(id: Long): Boolean =
        runCatching { dao.delete(id) }
            .getOrElse { throwDataError("delete course", it) }
}

class SQLiteTrailRepository(
    private val trailDao: TrailDao,
    private val enrollmentDao: EnrollmentDao
) : TrailRepository {
    override fun getAll(): List<Trail> =
        runCatching { trailDao.findAll().map { it.toDomainWithDetails() } }
            .getOrElse { logAndReturn("trails", it, emptyList()) }

    override fun getById(id: Long): Trail? =
        runCatching { trailDao.findById(id)?.toDomainWithDetails() }
            .getOrElse { logAndReturn("trail", it, null) }

    override fun save(trail: Trail): Long =
        runCatching { trailDao.insert(TrailEntity.fromDomain(trail)) }
            .getOrElse { throwDataError("save trail", it) }

    override fun update(trail: Trail): Boolean =
        runCatching { trailDao.update(TrailEntity.fromDomain(trail)) }
            .getOrElse { throwDataError("update trail", it) }

    override fun delete(id: Long): Boolean =
        runCatching { trailDao.delete(id) }
            .getOrElse { throwDataError("delete trail", it) }

    override fun addCourse(trailId: Long, courseId: Long): Boolean =
        runCatching { trailDao.addCourse(trailId, courseId) }
            .getOrElse { throwDataError("add course to trail", it) }

    override fun removeCourse(trailId: Long, courseId: Long): Boolean =
        runCatching { trailDao.removeCourse(trailId, courseId) }
            .getOrElse { throwDataError("remove course from trail", it) }

    override fun hasCourse(trailId: Long, courseId: Long): Boolean =
        runCatching { trailDao.hasCourse(trailId, courseId) }
            .getOrElse { logAndReturn("trail course lookup", it, false) }

    private fun TrailEntity.toDomainWithDetails(): Trail {
        val active = enrollmentDao.countByTrailAndStatus(id, EnrollmentStatus.ACTIVE)
        val completed = enrollmentDao.countByTrailAndStatus(id, EnrollmentStatus.COMPLETED)
        return toDomain(
            courses = trailDao.coursesForTrail(id).map { it.toDomain() },
            activeEnrollments = active,
            completedEnrollments = completed
        )
    }
}

class SQLiteEnrollmentRepository(
    private val enrollmentDao: EnrollmentDao,
    private val studentDao: StudentDao,
    private val trailRepository: TrailRepository
) : EnrollmentRepository {
    override fun getAllRecords(): List<EnrollmentRecord> =
        runCatching {
            enrollmentDao.findAll().map { entity ->
                val enrollment = entity.toDomain()
                EnrollmentRecord(
                    enrollment = enrollment,
                    student = studentDao.findById(enrollment.studentId)?.toDomain(),
                    trail = trailRepository.getById(enrollment.trailId)
                )
            }
        }.getOrElse { logAndReturn("enrollment records", it, emptyList()) }

    override fun getById(id: Long): Enrollment? =
        runCatching { enrollmentDao.findById(id)?.toDomain() }
            .getOrElse { logAndReturn("enrollment", it, null) }

    override fun getActiveByStudentAndTrail(studentId: Long, trailId: Long): Enrollment? =
        runCatching { enrollmentDao.findActiveByStudentAndTrail(studentId, trailId)?.toDomain() }
            .getOrElse { logAndReturn("active enrollment", it, null) }

    override fun save(enrollment: Enrollment): Long =
        runCatching { enrollmentDao.insert(EnrollmentEntity.fromDomain(enrollment)) }
            .getOrElse { throwDataError("save enrollment", it) }

    override fun updateStatus(id: Long, status: EnrollmentStatus, finishedAt: Long?): Boolean =
        runCatching { enrollmentDao.updateStatus(id, status, finishedAt) }
            .getOrElse { throwDataError("update enrollment", it) }

    override fun deleteByTrail(idTrail: Long) {
        runCatching { enrollmentDao.deleteByTrail(idTrail) }
            .getOrElse { throwDataError("delete trail enrollments", it) }
    }

    override fun deleteByStudent(idStudent: Long) {
        runCatching { enrollmentDao.deleteByStudent(idStudent) }
            .getOrElse { throwDataError("delete student enrollments", it) }
    }
}

class SQLiteAnalyticsRepository(
    private val studentRepository: StudentRepository,
    private val courseRepository: CourseRepository,
    private val trailRepository: TrailRepository,
    private val enrollmentRepository: EnrollmentRepository
) : AnalyticsRepository {
    override fun getDashboardStats(): DashboardStats {
        val students = studentRepository.getAll()
        val courses = courseRepository.getAll()
        val trails = trailRepository.getAll()
        val enrollments = enrollmentRepository.getAllRecords()
        return DashboardStats(
            totalStudents = students.size,
            activeStudents = students.count { it.status == StudentStatus.ACTIVE },
            totalCourses = courses.size,
            totalTrails = trails.size,
            activeTrails = trails.count { it.status == TrailStatus.ACTIVE },
            activeEnrollments = enrollments.count { it.enrollment.status == EnrollmentStatus.ACTIVE },
            totalWorkloadHours = courses.sumOf { it.workloadHours }
        )
    }
}

private fun <T> logAndReturn(area: String, throwable: Throwable, fallback: T): T {
    Log.e("TrainingRepository", "Failed to load $area", throwable)
    return fallback
}

private fun <T> throwDataError(action: String, throwable: Throwable): T {
    Log.e("TrainingRepository", "Failed to $action", throwable)
    throw SQLiteException("Não foi possível concluir a operação no banco de dados.")
}
