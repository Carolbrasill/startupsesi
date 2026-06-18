package com.example.cbstartup.domain.repository

import com.example.cbstartup.domain.model.Course
import com.example.cbstartup.domain.model.DashboardStats
import com.example.cbstartup.domain.model.Enrollment
import com.example.cbstartup.domain.model.EnrollmentRecord
import com.example.cbstartup.domain.model.EnrollmentStatus
import com.example.cbstartup.domain.model.Student
import com.example.cbstartup.domain.model.Trail

interface StudentRepository {
    fun getAll(): List<Student>
    fun getById(id: Long): Student?
    fun save(student: Student): Long
    fun update(student: Student): Boolean
    fun delete(id: Long): Boolean
}

interface CourseRepository {
    fun getAll(): List<Course>
    fun getById(id: Long): Course?
    fun save(course: Course): Long
    fun update(course: Course): Boolean
    fun delete(id: Long): Boolean
}

interface TrailRepository {
    fun getAll(): List<Trail>
    fun getById(id: Long): Trail?
    fun save(trail: Trail): Long
    fun update(trail: Trail): Boolean
    fun delete(id: Long): Boolean
    fun addCourse(trailId: Long, courseId: Long): Boolean
    fun removeCourse(trailId: Long, courseId: Long): Boolean
    fun hasCourse(trailId: Long, courseId: Long): Boolean
}

interface EnrollmentRepository {
    fun getAllRecords(): List<EnrollmentRecord>
    fun getById(id: Long): Enrollment?
    fun getActiveByStudentAndTrail(studentId: Long, trailId: Long): Enrollment?
    fun save(enrollment: Enrollment): Long
    fun updateStatus(id: Long, status: EnrollmentStatus, finishedAt: Long?): Boolean
    fun deleteByTrail(idTrail: Long)
    fun deleteByStudent(idStudent: Long)
}

interface AnalyticsRepository {
    fun getDashboardStats(): DashboardStats
}
