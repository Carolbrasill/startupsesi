package com.example.cbstartup.domain.model

enum class CourseLevel(val label: String) {
    BASIC("Básico"),
    INTERMEDIATE("Intermediário"),
    ADVANCED("Avançado");

    companion object {
        fun fromStored(value: String): CourseLevel =
            entries.firstOrNull { it.name == value } ?: BASIC
    }
}

enum class CourseCategory(val label: String) {
    KOTLIN("Kotlin"),
    ANDROID("Android"),
    ARCHITECTURE("Arquitetura"),
    TESTING("Testes"),
    DESIGN("Design");

    companion object {
        fun fromStored(value: String): CourseCategory =
            entries.firstOrNull { it.name == value } ?: KOTLIN
    }
}

data class Course(
    val id: Long = 0,
    val title: String,
    val workloadHours: Int,
    val level: CourseLevel,
    val category: CourseCategory
)
