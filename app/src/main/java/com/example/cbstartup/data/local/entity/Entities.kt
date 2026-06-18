package com.example.cbstartup.data.local.entity

import com.example.cbstartup.domain.model.Course
import com.example.cbstartup.domain.model.CourseCategory
import com.example.cbstartup.domain.model.CourseLevel
import com.example.cbstartup.domain.model.Enrollment
import com.example.cbstartup.domain.model.EnrollmentStatus
import com.example.cbstartup.domain.model.Student
import com.example.cbstartup.domain.model.StudentStatus
import com.example.cbstartup.domain.model.Trail
import com.example.cbstartup.domain.model.TrailStatus

data class StudentEntity(
    val id: Long,
    val name: String,
    val email: String,
    val status: String
) {
    fun toDomain(): Student = Student(
        id = id,
        name = name,
        email = email,
        status = StudentStatus.fromStored(status)
    )

    companion object {
        fun fromDomain(student: Student): StudentEntity = StudentEntity(
            id = student.id,
            name = student.name,
            email = student.email,
            status = student.status.name
        )
    }
}

data class CourseEntity(
    val id: Long,
    val title: String,
    val workloadHours: Int,
    val level: String,
    val category: String
) {
    fun toDomain(): Course = Course(
        id = id,
        title = title,
        workloadHours = workloadHours,
        level = CourseLevel.fromStored(level),
        category = CourseCategory.fromStored(category)
    )

    companion object {
        fun fromDomain(course: Course): CourseEntity = CourseEntity(
            id = course.id,
            title = course.title,
            workloadHours = course.workloadHours,
            level = course.level.name,
            category = course.category.name
        )
    }
}

data class TrailEntity(
    val id: Long,
    val name: String,
    val description: String,
    val status: String
) {
    fun toDomain(
        courses: List<Course> = emptyList(),
        activeEnrollments: Int = 0,
        completedEnrollments: Int = 0
    ): Trail = Trail(
        id = id,
        name = name,
        description = description,
        status = TrailStatus.fromStored(status),
        courses = courses,
        activeEnrollments = activeEnrollments,
        completedEnrollments = completedEnrollments
    )

    companion object {
        fun fromDomain(trail: Trail): TrailEntity = TrailEntity(
            id = trail.id,
            name = trail.name,
            description = trail.description,
            status = trail.status.name
        )
    }
}

data class EnrollmentEntity(
    val id: Long,
    val studentId: Long,
    val trailId: Long,
    val status: String,
    val enrolledAt: Long,
    val finishedAt: Long?
) {
    fun toDomain(): Enrollment = Enrollment(
        id = id,
        studentId = studentId,
        trailId = trailId,
        status = EnrollmentStatus.fromStored(status),
        enrolledAt = enrolledAt,
        finishedAt = finishedAt
    )

    companion object {
        fun fromDomain(enrollment: Enrollment): EnrollmentEntity = EnrollmentEntity(
            id = enrollment.id,
            studentId = enrollment.studentId,
            trailId = enrollment.trailId,
            status = enrollment.status.name,
            enrolledAt = enrollment.enrolledAt,
            finishedAt = enrollment.finishedAt
        )
    }
}
