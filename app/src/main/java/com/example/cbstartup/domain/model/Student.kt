package com.example.cbstartup.domain.model

enum class StudentStatus(val label: String) {
    ACTIVE("Ativo"),
    INACTIVE("Inativo"),
    BLOCKED("Bloqueado");

    companion object {
        fun fromStored(value: String): StudentStatus =
            entries.firstOrNull { it.name == value } ?: ACTIVE
    }
}

data class Student(
    val id: Long = 0,
    val name: String,
    val email: String,
    val status: StudentStatus = StudentStatus.ACTIVE
)
