package com.example.cbstartup

import com.example.cbstartup.domain.model.Course
import com.example.cbstartup.domain.model.CourseCategory
import com.example.cbstartup.domain.model.CourseLevel
import com.example.cbstartup.domain.model.DashboardStats
import com.example.cbstartup.domain.model.Enrollment
import com.example.cbstartup.domain.model.EnrollmentRecord
import com.example.cbstartup.domain.model.EnrollmentStatus
import com.example.cbstartup.domain.model.Student
import com.example.cbstartup.domain.model.StudentStatus
import com.example.cbstartup.domain.model.Trail
import com.example.cbstartup.domain.model.TrailStatus
import com.example.cbstartup.domain.repository.AnalyticsRepository
import com.example.cbstartup.domain.repository.CourseRepository
import com.example.cbstartup.domain.repository.EnrollmentRepository
import com.example.cbstartup.domain.repository.StudentRepository
import com.example.cbstartup.domain.repository.TrailRepository
import com.example.cbstartup.domain.usecase.BusinessResult
import com.example.cbstartup.domain.usecase.TrainingManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingManagerTest {
    @Test
    fun saveStudentRejectsDuplicatedEmail() {
        val fixture = Fixture()
        fixture.manager.saveStudent(Student(name = "Ana Silva", email = "ana@email.com"))

        val result = fixture.manager.saveStudent(Student(name = "Ana Souza", email = "ANA@email.com"))

        assertTrue(result is BusinessResult.Error)
    }

    @Test
    fun enrollStudentRejectsInactiveStudent() {
        val fixture = Fixture()
        val studentId = fixture.students.save(
            Student(name = "Bruno Lima", email = "bruno@email.com", status = StudentStatus.INACTIVE)
        )
        val trailId = fixture.trails.save(
            Trail(name = "Android Pro", description = "Trilha completa", status = TrailStatus.ACTIVE)
        )

        val result = fixture.manager.enrollStudent(studentId, trailId)

        assertTrue(result is BusinessResult.Error)
    }

    @Test
    fun enrollStudentRejectsDuplicatedActiveEnrollment() {
        val fixture = Fixture()
        val studentId = fixture.students.save(Student(name = "Carla Nunes", email = "carla@email.com"))
        val trailId = fixture.trails.save(
            Trail(name = "Kotlin Pro", description = "Trilha completa", status = TrailStatus.ACTIVE)
        )

        fixture.manager.enrollStudent(studentId, trailId)
        val result = fixture.manager.enrollStudent(studentId, trailId)

        assertTrue(result is BusinessResult.Error)
        assertEquals(1, fixture.enrollments.getAllRecords().size)
    }

    @Test
    fun addCourseToTrailRejectsDuplicatedCourse() {
        val fixture = Fixture()
        val courseId = fixture.courses.save(
            Course(
                title = "Compose Essentials",
                workloadHours = 32,
                level = CourseLevel.BASIC,
                category = CourseCategory.ANDROID
            )
        )
        val trailId = fixture.trails.save(
            Trail(name = "Android UI", description = "Interface moderna", status = TrailStatus.ACTIVE)
        )

        fixture.manager.addCourseToTrail(trailId, courseId)
        val result = fixture.manager.addCourseToTrail(trailId, courseId)

        assertTrue(result is BusinessResult.Error)
        assertEquals(1, fixture.trails.getById(trailId)?.courses?.size)
    }
}

private class Fixture {
    val students = InMemoryStudentRepository()
    val courses = InMemoryCourseRepository()
    val trails = InMemoryTrailRepository(courses)
    val enrollments = InMemoryEnrollmentRepository(students, trails)
    val analytics = InMemoryAnalyticsRepository(students, courses, trails, enrollments)
    val manager = TrainingManager(students, courses, trails, enrollments)
}

private class InMemoryStudentRepository : StudentRepository {
    private val items = linkedMapOf<Long, Student>()
    private var nextId = 1L

    override fun getAll(): List<Student> = items.values.toList()
    override fun getById(id: Long): Student? = items[id]
    override fun save(student: Student): Long {
        val id = nextId++
        items[id] = student.copy(id = id)
        return id
    }
    override fun update(student: Student): Boolean {
        if (!items.containsKey(student.id)) return false
        items[student.id] = student
        return true
    }
    override fun delete(id: Long): Boolean = items.remove(id) != null
}

private class InMemoryCourseRepository : CourseRepository {
    private val items = linkedMapOf<Long, Course>()
    private var nextId = 1L

    override fun getAll(): List<Course> = items.values.toList()
    override fun getById(id: Long): Course? = items[id]
    override fun save(course: Course): Long {
        val id = nextId++
        items[id] = course.copy(id = id)
        return id
    }
    override fun update(course: Course): Boolean {
        if (!items.containsKey(course.id)) return false
        items[course.id] = course
        return true
    }
    override fun delete(id: Long): Boolean = items.remove(id) != null
}

private class InMemoryTrailRepository(
    private val courses: CourseRepository
) : TrailRepository {
    private val items = linkedMapOf<Long, Trail>()
    private var nextId = 1L

    override fun getAll(): List<Trail> = items.values.toList()
    override fun getById(id: Long): Trail? = items[id]
    override fun save(trail: Trail): Long {
        val id = nextId++
        items[id] = trail.copy(id = id)
        return id
    }
    override fun update(trail: Trail): Boolean {
        if (!items.containsKey(trail.id)) return false
        items[trail.id] = trail
        return true
    }
    override fun delete(id: Long): Boolean = items.remove(id) != null
    override fun addCourse(trailId: Long, courseId: Long): Boolean {
        val trail = items[trailId] ?: return false
        val course = courses.getById(courseId) ?: return false
        if (trail.courses.any { it.id == courseId }) return false
        items[trailId] = trail.copy(courses = trail.courses + course)
        return true
    }
    override fun removeCourse(trailId: Long, courseId: Long): Boolean {
        val trail = items[trailId] ?: return false
        if (trail.courses.none { it.id == courseId }) return false
        items[trailId] = trail.copy(courses = trail.courses.filterNot { it.id == courseId })
        return true
    }
    override fun hasCourse(trailId: Long, courseId: Long): Boolean =
        items[trailId]?.courses?.any { it.id == courseId } == true
}

private class InMemoryEnrollmentRepository(
    private val students: StudentRepository,
    private val trails: TrailRepository
) : EnrollmentRepository {
    private val items = linkedMapOf<Long, Enrollment>()
    private var nextId = 1L

    override fun getAllRecords(): List<EnrollmentRecord> =
        items.values.map { EnrollmentRecord(it, students.getById(it.studentId), trails.getById(it.trailId)) }
    override fun getById(id: Long): Enrollment? = items[id]
    override fun getActiveByStudentAndTrail(studentId: Long, trailId: Long): Enrollment? =
        items.values.firstOrNull {
            it.studentId == studentId && it.trailId == trailId && it.status == EnrollmentStatus.ACTIVE
        }
    override fun save(enrollment: Enrollment): Long {
        val id = nextId++
        items[id] = enrollment.copy(id = id)
        return id
    }
    override fun updateStatus(id: Long, status: EnrollmentStatus, finishedAt: Long?): Boolean {
        val current = items[id] ?: return false
        items[id] = current.copy(status = status, finishedAt = finishedAt)
        return true
    }
    override fun deleteByTrail(idTrail: Long) {
        items.values.removeAll { it.trailId == idTrail }
    }
    override fun deleteByStudent(idStudent: Long) {
        items.values.removeAll { it.studentId == idStudent }
    }
}

private class InMemoryAnalyticsRepository(
    private val students: StudentRepository,
    private val courses: CourseRepository,
    private val trails: TrailRepository,
    private val enrollments: EnrollmentRepository
) : AnalyticsRepository {
    override fun getDashboardStats(): DashboardStats = DashboardStats(
        totalStudents = students.getAll().size,
        activeStudents = students.getAll().count { it.status == StudentStatus.ACTIVE },
        totalCourses = courses.getAll().size,
        totalTrails = trails.getAll().size,
        activeTrails = trails.getAll().count { it.status == TrailStatus.ACTIVE },
        activeEnrollments = enrollments.getAllRecords().count { it.enrollment.status == EnrollmentStatus.ACTIVE },
        totalWorkloadHours = courses.getAll().sumOf { it.workloadHours }
    )
}
