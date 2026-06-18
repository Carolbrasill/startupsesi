package com.example.cbstartup.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cbstartup.core.di.AppContainer
import com.example.cbstartup.domain.model.Course
import com.example.cbstartup.domain.model.CourseCategory
import com.example.cbstartup.domain.model.CourseLevel
import com.example.cbstartup.domain.model.DashboardStats
import com.example.cbstartup.domain.model.EnrollmentRecord
import com.example.cbstartup.domain.model.EnrollmentStatus
import com.example.cbstartup.domain.model.Student
import com.example.cbstartup.domain.model.StudentStatus
import com.example.cbstartup.domain.model.Trail
import com.example.cbstartup.domain.model.TrailStatus
import com.example.cbstartup.domain.usecase.BusinessResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppScreen(val label: String) {
    DASHBOARD("Dashboard"),
    STUDENTS("Alunos"),
    COURSES("Cursos"),
    TRAILS("Trilhas"),
    ENROLLMENTS("Matrículas"),
    REPORTS("Relatórios")
}

data class UiMessage(
    val text: String,
    val isError: Boolean = false
)

data class TrainingUiState(
    val isLoading: Boolean = true,
    val selectedScreen: AppScreen = AppScreen.DASHBOARD,
    val dashboardStats: DashboardStats = DashboardStats(),
    val students: List<Student> = emptyList(),
    val courses: List<Course> = emptyList(),
    val trails: List<Trail> = emptyList(),
    val enrollments: List<EnrollmentRecord> = emptyList(),
    val studentSearch: String = "",
    val courseSearch: String = "",
    val trailSearch: String = "",
    val enrollmentSearch: String = "",
    val studentStatusFilter: StudentStatus? = null,
    val courseLevelFilter: CourseLevel? = null,
    val courseCategoryFilter: CourseCategory? = null,
    val trailStatusFilter: TrailStatus? = null,
    val enrollmentStatusFilter: EnrollmentStatus? = null,
    val selectedTrailId: Long? = null,
    val message: UiMessage? = null
)

class TrainingViewModel(
    private val container: AppContainer
) : ViewModel() {
    private val _uiState = MutableStateFlow(TrainingUiState())
    val uiState: StateFlow<TrainingUiState> = _uiState.asStateFlow()

    init {
        refreshData()
    }

    fun selectScreen(screen: AppScreen) {
        _uiState.value = _uiState.value.copy(selectedScreen = screen)
    }

    fun selectTrail(id: Long?) {
        _uiState.value = _uiState.value.copy(selectedTrailId = id)
    }

    fun updateStudentSearch(value: String) {
        _uiState.value = _uiState.value.copy(studentSearch = value)
    }

    fun updateCourseSearch(value: String) {
        _uiState.value = _uiState.value.copy(courseSearch = value)
    }

    fun updateTrailSearch(value: String) {
        _uiState.value = _uiState.value.copy(trailSearch = value)
    }

    fun updateEnrollmentSearch(value: String) {
        _uiState.value = _uiState.value.copy(enrollmentSearch = value)
    }

    fun updateStudentStatusFilter(value: StudentStatus?) {
        _uiState.value = _uiState.value.copy(studentStatusFilter = value)
    }

    fun updateCourseLevelFilter(value: CourseLevel?) {
        _uiState.value = _uiState.value.copy(courseLevelFilter = value)
    }

    fun updateCourseCategoryFilter(value: CourseCategory?) {
        _uiState.value = _uiState.value.copy(courseCategoryFilter = value)
    }

    fun updateTrailStatusFilter(value: TrailStatus?) {
        _uiState.value = _uiState.value.copy(trailStatusFilter = value)
    }

    fun updateEnrollmentStatusFilter(value: EnrollmentStatus?) {
        _uiState.value = _uiState.value.copy(enrollmentStatusFilter = value)
    }

    fun saveStudent(student: Student) {
        handleBusinessResult(
            result = container.trainingManager.saveStudent(student),
            successMessage = if (student.id == 0L) "Aluno cadastrado com sucesso." else "Aluno atualizado com sucesso."
        )
    }

    fun deleteStudent(id: Long) {
        handleBusinessResult(
            result = container.trainingManager.deleteStudent(id),
            successMessage = "Aluno excluído com sucesso."
        )
    }

    fun saveCourse(course: Course) {
        handleBusinessResult(
            result = container.trainingManager.saveCourse(course),
            successMessage = if (course.id == 0L) "Curso cadastrado com sucesso." else "Curso atualizado com sucesso."
        )
    }

    fun deleteCourse(id: Long) {
        handleBusinessResult(
            result = container.trainingManager.deleteCourse(id),
            successMessage = "Curso excluído com sucesso."
        )
    }

    fun saveTrail(trail: Trail) {
        handleBusinessResult(
            result = container.trainingManager.saveTrail(trail),
            successMessage = if (trail.id == 0L) "Trilha criada com sucesso." else "Trilha atualizada com sucesso."
        )
    }

    fun deleteTrail(id: Long) {
        handleBusinessResult(
            result = container.trainingManager.deleteTrail(id),
            successMessage = "Trilha excluída com sucesso."
        )
    }

    fun addCourseToTrail(trailId: Long, courseId: Long) {
        handleBusinessResult(
            result = container.trainingManager.addCourseToTrail(trailId, courseId),
            successMessage = "Curso adicionado à trilha."
        )
    }

    fun removeCourseFromTrail(trailId: Long, courseId: Long) {
        handleBusinessResult(
            result = container.trainingManager.removeCourseFromTrail(trailId, courseId),
            successMessage = "Curso removido da trilha."
        )
    }

    fun enrollStudent(studentId: Long, trailId: Long) {
        handleBusinessResult(
            result = container.trainingManager.enrollStudent(studentId, trailId),
            successMessage = "Matrícula realizada com sucesso."
        )
    }

    fun cancelEnrollment(id: Long) {
        handleBusinessResult(
            result = container.trainingManager.cancelEnrollment(id),
            successMessage = "Matrícula cancelada."
        )
    }

    fun completeEnrollment(id: Long) {
        handleBusinessResult(
            result = container.trainingManager.completeEnrollment(id),
            successMessage = "Matrícula concluída."
        )
    }

    fun refreshData(message: UiMessage? = _uiState.value.message) {
        val current = _uiState.value
        _uiState.value = current.copy(isLoading = true)
        runCatching {
            val students = container.studentRepository.getAll()
            val courses = container.courseRepository.getAll()
            val trails = container.trailRepository.getAll()
            val enrollments = container.enrollmentRepository.getAllRecords()
            val selectedTrailId = current.selectedTrailId
                ?.takeIf { selectedId -> trails.any { it.id == selectedId } }
                ?: trails.firstOrNull()?.id

            current.copy(
                isLoading = false,
                dashboardStats = container.analyticsRepository.getDashboardStats(),
                students = students,
                courses = courses,
                trails = trails,
                enrollments = enrollments,
                selectedTrailId = selectedTrailId,
                message = message
            )
        }.onSuccess {
            _uiState.value = it
        }.onFailure { throwable ->
            _uiState.value = current.copy(
                isLoading = false,
                message = UiMessage(
                    text = throwable.message ?: "Não foi possível carregar os dados.",
                    isError = true
                )
            )
        }
    }

    fun consumeMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    private fun <T> handleBusinessResult(
        result: BusinessResult<T>,
        successMessage: String
    ) {
        when (result) {
            is BusinessResult.Success -> refreshData(UiMessage(successMessage))
            is BusinessResult.Error -> _uiState.value = _uiState.value.copy(
                message = UiMessage(result.message, isError = true)
            )
        }
    }

    class Factory(
        private val container: AppContainer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TrainingViewModel::class.java)) {
                return TrainingViewModel(container) as T
            }
            throw IllegalArgumentException("ViewModel não suportado: ${modelClass.name}")
        }
    }
}
