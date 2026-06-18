package com.example.cbstartup.domain.model

enum class TrailStatus(val label: String) {
    PLANNED("Planejada"),
    ACTIVE("Ativa"),
    COMPLETED("Concluída"),
    ARCHIVED("Arquivada");

    companion object {
        fun fromStored(value: String): TrailStatus =
            entries.firstOrNull { it.name == value } ?: PLANNED
    }
}

data class Trail(
    val id: Long = 0,
    val name: String,
    val description: String,
    val status: TrailStatus = TrailStatus.PLANNED,
    val courses: List<Course> = emptyList(),
    val activeEnrollments: Int = 0,
    val completedEnrollments: Int = 0
) {
    val totalWorkloadHours: Int
        get() = courses.sumOf { it.workloadHours }

    val progressPercent: Int
        get() {
            val total = activeEnrollments + completedEnrollments
            if (total == 0) return when (status) {
                TrailStatus.PLANNED -> 0
                TrailStatus.ACTIVE -> 35
                TrailStatus.COMPLETED -> 100
                TrailStatus.ARCHIVED -> 100
            }
            return ((completedEnrollments.toDouble() / total.toDouble()) * 100).toInt()
        }
}
