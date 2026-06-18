package com.example.cbstartup.core.di

import android.content.Context
import com.example.cbstartup.data.local.TrainingDatabase
import com.example.cbstartup.data.local.dao.CourseDao
import com.example.cbstartup.data.local.dao.EnrollmentDao
import com.example.cbstartup.data.local.dao.StudentDao
import com.example.cbstartup.data.local.dao.TrailDao
import com.example.cbstartup.data.repository.SQLiteAnalyticsRepository
import com.example.cbstartup.data.repository.SQLiteCourseRepository
import com.example.cbstartup.data.repository.SQLiteEnrollmentRepository
import com.example.cbstartup.data.repository.SQLiteStudentRepository
import com.example.cbstartup.data.repository.SQLiteTrailRepository
import com.example.cbstartup.domain.repository.AnalyticsRepository
import com.example.cbstartup.domain.repository.CourseRepository
import com.example.cbstartup.domain.repository.EnrollmentRepository
import com.example.cbstartup.domain.repository.StudentRepository
import com.example.cbstartup.domain.repository.TrailRepository
import com.example.cbstartup.domain.usecase.TrainingManager

class AppContainer(context: Context) {
    private val database = TrainingDatabase(context.applicationContext)
    private val studentDao = StudentDao(database)
    private val courseDao = CourseDao(database)
    private val enrollmentDao = EnrollmentDao(database)
    private val trailDao = TrailDao(database, courseDao)

    val studentRepository: StudentRepository = SQLiteStudentRepository(studentDao)
    val courseRepository: CourseRepository = SQLiteCourseRepository(courseDao)
    val trailRepository: TrailRepository = SQLiteTrailRepository(trailDao, enrollmentDao)
    val enrollmentRepository: EnrollmentRepository = SQLiteEnrollmentRepository(
        enrollmentDao = enrollmentDao,
        studentDao = studentDao,
        trailRepository = trailRepository
    )
    val analyticsRepository: AnalyticsRepository = SQLiteAnalyticsRepository(
        studentRepository = studentRepository,
        courseRepository = courseRepository,
        trailRepository = trailRepository,
        enrollmentRepository = enrollmentRepository
    )
    val trainingManager = TrainingManager(
        students = studentRepository,
        courses = courseRepository,
        trails = trailRepository,
        enrollments = enrollmentRepository
    )
}
