package com.example.cbstartup.domain.model

data class DashboardStats(
    val totalStudents: Int = 0,
    val activeStudents: Int = 0,
    val totalCourses: Int = 0,
    val totalTrails: Int = 0,
    val activeTrails: Int = 0,
    val activeEnrollments: Int = 0,
    val totalWorkloadHours: Int = 0
)
