package com.example.cbstartup.domain.model

enum class EnrollmentStatus(val label: String) {
    ACTIVE("Ativa"),
    CANCELLED("Cancelada"),
    COMPLETED("Concluída");

    companion object {
        fun fromStored(value: String): EnrollmentStatus =
            entries.firstOrNull { it.name == value } ?: ACTIVE
    }
}

data class Enrollment(
    val id: Long = 0,
    val studentId: Long,
    val trailId: Long,
    val status: EnrollmentStatus = EnrollmentStatus.ACTIVE,
    val enrolledAt: Long = System.currentTimeMillis(),
    val finishedAt: Long? = null
)

data class EnrollmentRecord(
    val enrollment: Enrollment,
    val student: Student?,
    val trail: Trail?
)
