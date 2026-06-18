package com.example.cbstartup.domain.usecase

import com.example.cbstartup.domain.model.Course
import com.example.cbstartup.domain.model.Enrollment
import com.example.cbstartup.domain.model.EnrollmentStatus
import com.example.cbstartup.domain.model.Student
import com.example.cbstartup.domain.model.StudentStatus
import com.example.cbstartup.domain.model.Trail
import com.example.cbstartup.domain.model.TrailStatus
import com.example.cbstartup.domain.repository.CourseRepository
import com.example.cbstartup.domain.repository.EnrollmentRepository
import com.example.cbstartup.domain.repository.StudentRepository
import com.example.cbstartup.domain.repository.TrailRepository

sealed class BusinessResult<out T> {
    data class Success<T>(val value: T) : BusinessResult<T>()
    data class Error(val message: String) : BusinessResult<Nothing>()
}

class TrainingManager(
    private val students: StudentRepository,
    private val courses: CourseRepository,
    private val trails: TrailRepository,
    private val enrollments: EnrollmentRepository
) {
    private val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    fun saveStudent(student: Student): BusinessResult<Long> {
        val normalized = student.copy(
            name = student.name.trim(),
            email = student.email.trim().lowercase()
        )
        val validation = validateStudent(normalized)
        if (validation != null) return BusinessResult.Error(validation)

        val duplicatedEmail = students.getAll().any {
            it.email.equals(normalized.email, ignoreCase = true) && it.id != normalized.id
        }
        if (duplicatedEmail) return BusinessResult.Error("Já existe um aluno com este email.")

        return if (normalized.id == 0L) {
            BusinessResult.Success(students.save(normalized))
        } else if (students.update(normalized)) {
            BusinessResult.Success(normalized.id)
        } else {
            BusinessResult.Error("Aluno não encontrado para edição.")
        }
    }

    fun deleteStudent(id: Long): BusinessResult<Unit> {
        enrollments.deleteByStudent(id)
        return if (students.delete(id)) {
            BusinessResult.Success(Unit)
        } else {
            BusinessResult.Error("Aluno não encontrado.")
        }
    }

    fun saveCourse(course: Course): BusinessResult<Long> {
        val normalized = course.copy(title = course.title.trim())
        val validation = validateCourse(normalized)
        if (validation != null) return BusinessResult.Error(validation)

        return if (normalized.id == 0L) {
            BusinessResult.Success(courses.save(normalized))
        } else if (courses.update(normalized)) {
            BusinessResult.Success(normalized.id)
        } else {
            BusinessResult.Error("Curso não encontrado para edição.")
        }
    }

    fun deleteCourse(id: Long): BusinessResult<Unit> =
        if (courses.delete(id)) {
            BusinessResult.Success(Unit)
        } else {
            BusinessResult.Error("Curso não encontrado.")
        }

    fun saveTrail(trail: Trail): BusinessResult<Long> {
        val normalized = trail.copy(
            name = trail.name.trim(),
            description = trail.description.trim()
        )
        val validation = validateTrail(normalized)
        if (validation != null) return BusinessResult.Error(validation)

        return if (normalized.id == 0L) {
            BusinessResult.Success(trails.save(normalized))
        } else if (trails.update(normalized)) {
            BusinessResult.Success(normalized.id)
        } else {
            BusinessResult.Error("Trilha não encontrada para edição.")
        }
    }

    fun deleteTrail(id: Long): BusinessResult<Unit> {
        enrollments.deleteByTrail(id)
        return if (trails.delete(id)) {
            BusinessResult.Success(Unit)
        } else {
            BusinessResult.Error("Trilha não encontrada.")
        }
    }

    fun addCourseToTrail(trailId: Long, courseId: Long): BusinessResult<Unit> {
        if (trails.getById(trailId) == null) return BusinessResult.Error("Trilha não encontrada.")
        if (courses.getById(courseId) == null) return BusinessResult.Error("Curso não encontrado.")
        if (trails.hasCourse(trailId, courseId)) {
            return BusinessResult.Error("Este curso já faz parte da trilha.")
        }
        return if (trails.addCourse(trailId, courseId)) {
            BusinessResult.Success(Unit)
        } else {
            BusinessResult.Error("Não foi possível adicionar o curso.")
        }
    }

    fun removeCourseFromTrail(trailId: Long, courseId: Long): BusinessResult<Unit> =
        if (trails.removeCourse(trailId, courseId)) {
            BusinessResult.Success(Unit)
        } else {
            BusinessResult.Error("Curso não encontrado nesta trilha.")
        }

    fun enrollStudent(studentId: Long, trailId: Long): BusinessResult<Long> {
        val student = students.getById(studentId)
            ?: return BusinessResult.Error("Aluno não encontrado.")
        val trail = trails.getById(trailId)
            ?: return BusinessResult.Error("Trilha não encontrada.")

        if (student.status != StudentStatus.ACTIVE) {
            return BusinessResult.Error("Apenas alunos ativos podem ser matriculados.")
        }
        if (trail.status == TrailStatus.ARCHIVED || trail.status == TrailStatus.COMPLETED) {
            return BusinessResult.Error("Esta trilha não aceita novas matrículas.")
        }
        if (enrollments.getActiveByStudentAndTrail(studentId, trailId) != null) {
            return BusinessResult.Error("O aluno já possui matrícula ativa nesta trilha.")
        }

        val enrollment = Enrollment(studentId = studentId, trailId = trailId)
        return BusinessResult.Success(enrollments.save(enrollment))
    }

    fun cancelEnrollment(id: Long): BusinessResult<Unit> {
        val enrollment = enrollments.getById(id)
            ?: return BusinessResult.Error("Matrícula não encontrada.")
        if (enrollment.status != EnrollmentStatus.ACTIVE) {
            return BusinessResult.Error("A matrícula selecionada já foi encerrada.")
        }
        return if (enrollments.updateStatus(id, EnrollmentStatus.CANCELLED, System.currentTimeMillis())) {
            BusinessResult.Success(Unit)
        } else {
            BusinessResult.Error("Não foi possível cancelar a matrícula.")
        }
    }

    fun completeEnrollment(id: Long): BusinessResult<Unit> {
        val enrollment = enrollments.getById(id)
            ?: return BusinessResult.Error("Matrícula não encontrada.")
        if (enrollment.status != EnrollmentStatus.ACTIVE) {
            return BusinessResult.Error("A matrícula selecionada já foi encerrada.")
        }
        return if (enrollments.updateStatus(id, EnrollmentStatus.COMPLETED, System.currentTimeMillis())) {
            BusinessResult.Success(Unit)
        } else {
            BusinessResult.Error("Não foi possível concluir a matrícula.")
        }
    }

    private fun validateStudent(student: Student): String? = when {
        student.name.length < 3 -> "Informe um nome com pelo menos 3 caracteres."
        !emailPattern.matches(student.email) -> "Informe um email válido."
        else -> null
    }

    private fun validateCourse(course: Course): String? = when {
        course.title.length < 3 -> "Informe um título com pelo menos 3 caracteres."
        course.workloadHours <= 0 -> "Informe uma carga horária maior que zero."
        course.workloadHours > 500 -> "A carga horária deve ser menor ou igual a 500 horas."
        else -> null
    }

    private fun validateTrail(trail: Trail): String? = when {
        trail.name.length < 3 -> "Informe um nome com pelo menos 3 caracteres."
        trail.description.length < 8 -> "Informe uma descrição mais detalhada."
        else -> null
    }
}
