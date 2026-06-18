package com.example.cbstartup.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cbstartup.domain.model.Course
import com.example.cbstartup.domain.model.CourseCategory
import com.example.cbstartup.domain.model.CourseLevel
import com.example.cbstartup.domain.model.EnrollmentRecord
import com.example.cbstartup.domain.model.EnrollmentStatus
import com.example.cbstartup.domain.model.Student
import com.example.cbstartup.domain.model.StudentStatus
import com.example.cbstartup.domain.model.Trail
import com.example.cbstartup.domain.model.TrailStatus
import com.example.cbstartup.presentation.AppScreen
import com.example.cbstartup.presentation.TrainingUiState
import com.example.cbstartup.presentation.TrainingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrainingApp(viewModel: TrainingViewModel) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it.text)
            viewModel.consumeMessage()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val wideLayout = maxWidth >= 900.dp
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (!wideLayout) {
                    BottomNavigation(
                        selected = state.selectedScreen,
                        onSelected = viewModel::selectScreen
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (wideLayout) {
                    SideNavigation(
                        selected = state.selectedScreen,
                        onSelected = viewModel::selectScreen
                    )
                }
                AppContent(
                    state = state,
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AppContent(
    state: TrainingUiState,
    viewModel: TrainingViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedVisibility(visible = state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        when (state.selectedScreen) {
            AppScreen.DASHBOARD -> DashboardScreen(state)
            AppScreen.STUDENTS -> StudentsScreen(state, viewModel)
            AppScreen.COURSES -> CoursesScreen(state, viewModel)
            AppScreen.TRAILS -> TrailsScreen(state, viewModel)
            AppScreen.ENROLLMENTS -> EnrollmentsScreen(state, viewModel)
            AppScreen.REPORTS -> ReportsScreen(state)
        }
    }
}

@Composable
private fun SideNavigation(
    selected: AppScreen,
    onSelected: (AppScreen) -> Unit
) {
    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxHeight()
            .width(180.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Brand()
            Spacer(Modifier.height(24.dp))
            NavigationRail(
                containerColor = Color.Transparent
            ) {
                AppScreen.entries.forEach { screen ->
                    NavigationRailItem(
                        selected = selected == screen,
                        onClick = { onSelected(screen) },
                        icon = { NavMark(screen.label) },
                        label = { Text(screen.label, maxLines = 1) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavigation(
    selected: AppScreen,
    onSelected: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        AppScreen.entries.forEach { screen ->
            NavigationBarItem(
                selected = selected == screen,
                onClick = { onSelected(screen) },
                icon = { NavMark(screen.label) },
                label = { Text(screen.label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            )
        }
    }
}

@Composable
private fun Brand() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "CB",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "CBstartup",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Treinamento",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun NavMark(label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(28.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label.take(1),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun DashboardScreen(state: TrainingUiState) {
    ScreenColumn(
        title = "Dashboard",
        subtitle = "Visão geral do treinamento educacional"
    ) {
        ResponsiveStatGrid(
            stats = listOf(
                Stat("Alunos", state.dashboardStats.totalStudents.toString(), "${state.dashboardStats.activeStudents} ativos"),
                Stat("Cursos", state.dashboardStats.totalCourses.toString(), "${state.dashboardStats.totalWorkloadHours} horas catalogadas"),
                Stat("Trilhas", state.dashboardStats.totalTrails.toString(), "${state.dashboardStats.activeTrails} ativas"),
                Stat("Matrículas", state.dashboardStats.activeEnrollments.toString(), "ativas agora")
            )
        )

        Spacer(Modifier.height(20.dp))
        SurfacePanel {
            Text("Indicadores", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            IndicatorRow(
                label = "Alunos ativos",
                value = state.dashboardStats.activeStudents,
                total = state.dashboardStats.totalStudents
            )
            IndicatorRow(
                label = "Trilhas ativas",
                value = state.dashboardStats.activeTrails,
                total = state.dashboardStats.totalTrails
            )
            IndicatorRow(
                label = "Matrículas ativas por aluno",
                value = state.dashboardStats.activeEnrollments,
                total = state.dashboardStats.totalStudents.coerceAtLeast(1)
            )
        }

        Spacer(Modifier.height(20.dp))
        SurfacePanel {
            Text("Operação recente", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            if (state.enrollments.isEmpty()) {
                EmptyState("Nenhuma matrícula registrada ainda.")
            } else {
                state.enrollments.take(5).forEachIndexed { index, record ->
                    EnrollmentCompactRow(record)
                    if (index < state.enrollments.take(5).lastIndex) HorizontalDivider(Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun StudentsScreen(
    state: TrainingUiState,
    viewModel: TrainingViewModel
) {
    var editing by remember { mutableStateOf<Student?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Student?>(null) }
    val students = state.filteredStudents()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ListHeader(
                title = "Gestão de Alunos",
                subtitle = "Cadastre, edite, filtre e acompanhe a situação dos alunos",
                search = state.studentSearch,
                searchPlaceholder = "Buscar por nome, email ou ID",
                onSearchChange = viewModel::updateStudentSearch,
                actionLabel = "Novo aluno",
                onAction = {
                    editing = null
                    showDialog = true
                }
            )
            FilterRow {
                StatusChip(
                    label = "Todos",
                    selected = state.studentStatusFilter == null,
                    onClick = { viewModel.updateStudentStatusFilter(null) }
                )
                StudentStatus.entries.forEach { status ->
                    StatusChip(
                        label = status.label,
                        selected = state.studentStatusFilter == status,
                        onClick = { viewModel.updateStudentStatusFilter(status) }
                    )
                }
            }
        }

        if (students.isEmpty()) {
            item { EmptyState("Nenhum aluno encontrado.") }
        } else {
            items(students, key = { it.id }) { student ->
                StudentRow(
                    student = student,
                    onEdit = {
                        editing = student
                        showDialog = true
                    },
                    onDelete = { deleteTarget = student }
                )
            }
        }
    }

    if (showDialog) {
        StudentDialog(
            student = editing,
            onDismiss = { showDialog = false },
            onSave = {
                viewModel.saveStudent(it)
                showDialog = false
            }
        )
    }

    deleteTarget?.let { student ->
        ConfirmDeleteDialog(
            title = "Excluir aluno",
            message = "Deseja excluir ${student.name}? As matrículas vinculadas também serão removidas.",
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.deleteStudent(student.id)
                deleteTarget = null
            }
        )
    }
}

@Composable
private fun CoursesScreen(
    state: TrainingUiState,
    viewModel: TrainingViewModel
) {
    var editing by remember { mutableStateOf<Course?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Course?>(null) }
    val courses = state.filteredCourses()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ListHeader(
                title = "Gestão de Cursos",
                subtitle = "Organize o catálogo por nível, categoria e carga horária",
                search = state.courseSearch,
                searchPlaceholder = "Buscar por título, categoria ou ID",
                onSearchChange = viewModel::updateCourseSearch,
                actionLabel = "Novo curso",
                onAction = {
                    editing = null
                    showDialog = true
                }
            )
            FilterRow {
                StatusChip(
                    label = "Todos",
                    selected = state.courseLevelFilter == null,
                    onClick = { viewModel.updateCourseLevelFilter(null) }
                )
                CourseLevel.entries.forEach { level ->
                    StatusChip(
                        label = level.label,
                        selected = state.courseLevelFilter == level,
                        onClick = { viewModel.updateCourseLevelFilter(level) }
                    )
                }
            }
            FilterRow {
                StatusChip(
                    label = "Todas categorias",
                    selected = state.courseCategoryFilter == null,
                    onClick = { viewModel.updateCourseCategoryFilter(null) }
                )
                CourseCategory.entries.forEach { category ->
                    StatusChip(
                        label = category.label,
                        selected = state.courseCategoryFilter == category,
                        onClick = { viewModel.updateCourseCategoryFilter(category) }
                    )
                }
            }
        }

        if (courses.isEmpty()) {
            item { EmptyState("Nenhum curso encontrado.") }
        } else {
            items(courses, key = { it.id }) { course ->
                CourseRow(
                    course = course,
                    onEdit = {
                        editing = course
                        showDialog = true
                    },
                    onDelete = { deleteTarget = course }
                )
            }
        }
    }

    if (showDialog) {
        CourseDialog(
            course = editing,
            onDismiss = { showDialog = false },
            onSave = {
                viewModel.saveCourse(it)
                showDialog = false
            }
        )
    }

    deleteTarget?.let { course ->
        ConfirmDeleteDialog(
            title = "Excluir curso",
            message = "Deseja excluir ${course.title}? Ele será removido das trilhas vinculadas.",
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.deleteCourse(course.id)
                deleteTarget = null
            }
        )
    }
}

@Composable
private fun TrailsScreen(
    state: TrainingUiState,
    viewModel: TrainingViewModel
) {
    var editing by remember { mutableStateOf<Trail?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Trail?>(null) }
    val trails = state.filteredTrails()
    val selectedTrail = state.trails.firstOrNull { it.id == state.selectedTrailId }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth >= 980.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            ListHeader(
                title = "Gestão de Trilhas",
                subtitle = "Monte jornadas com cursos, carga horária e progresso",
                search = state.trailSearch,
                searchPlaceholder = "Buscar por nome, status ou ID",
                onSearchChange = viewModel::updateTrailSearch,
                actionLabel = "Nova trilha",
                onAction = {
                    editing = null
                    showDialog = true
                }
            )
            FilterRow {
                StatusChip(
                    label = "Todas",
                    selected = state.trailStatusFilter == null,
                    onClick = { viewModel.updateTrailStatusFilter(null) }
                )
                TrailStatus.entries.forEach { status ->
                    StatusChip(
                        label = status.label,
                        selected = state.trailStatusFilter == status,
                        onClick = { viewModel.updateTrailStatusFilter(status) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TrailList(
                        trails = trails,
                        selectedTrailId = state.selectedTrailId,
                        onSelected = viewModel::selectTrail,
                        onEdit = {
                            editing = it
                            showDialog = true
                        },
                        onDelete = { deleteTarget = it },
                        modifier = Modifier.weight(0.42f)
                    )
                    TrailDetails(
                        trail = selectedTrail,
                        allCourses = state.courses,
                        onAddCourse = viewModel::addCourseToTrail,
                        onRemoveCourse = viewModel::removeCourseFromTrail,
                        modifier = Modifier.weight(0.58f)
                    )
                }
            } else {
                TrailList(
                    trails = trails,
                    selectedTrailId = state.selectedTrailId,
                    onSelected = viewModel::selectTrail,
                    onEdit = {
                        editing = it
                        showDialog = true
                    },
                    onDelete = { deleteTarget = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                TrailDetails(
                    trail = selectedTrail,
                    allCourses = state.courses,
                    onAddCourse = viewModel::addCourseToTrail,
                    onRemoveCourse = viewModel::removeCourseFromTrail,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showDialog) {
        TrailDialog(
            trail = editing,
            onDismiss = { showDialog = false },
            onSave = {
                viewModel.saveTrail(it)
                showDialog = false
            }
        )
    }

    deleteTarget?.let { trail ->
        ConfirmDeleteDialog(
            title = "Excluir trilha",
            message = "Deseja excluir ${trail.name}? Cursos não serão apagados, mas matrículas desta trilha serão removidas.",
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.deleteTrail(trail.id)
                deleteTarget = null
            }
        )
    }
}

@Composable
private fun EnrollmentsScreen(
    state: TrainingUiState,
    viewModel: TrainingViewModel
) {
    var selectedStudentId by remember(state.students) {
        mutableStateOf(state.students.firstOrNull { it.status == StudentStatus.ACTIVE }?.id)
    }
    var selectedTrailId by remember(state.trails) {
        mutableStateOf(state.trails.firstOrNull { it.status != TrailStatus.ARCHIVED && it.status != TrailStatus.COMPLETED }?.id)
    }
    val records = state.filteredEnrollments()
    val eligibleStudents = state.students.filter { it.status == StudentStatus.ACTIVE }
    val eligibleTrails = state.trails.filter { it.status != TrailStatus.ARCHIVED && it.status != TrailStatus.COMPLETED }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ListHeader(
                title = "Matrículas",
                subtitle = "Matricule alunos, acompanhe histórico e encerre registros",
                search = state.enrollmentSearch,
                searchPlaceholder = "Buscar por aluno, trilha ou ID",
                onSearchChange = viewModel::updateEnrollmentSearch,
                actionLabel = null,
                onAction = {}
            )
            SurfacePanel {
                Text("Nova matrícula", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                BoxWithConstraints {
                    val wide = maxWidth >= 760.dp
                    if (wide) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SelectField(
                                label = "Aluno",
                                selected = selectedStudentId,
                                options = eligibleStudents.map { it.id },
                                placeholder = "Selecione",
                                optionLabel = { id -> eligibleStudents.firstOrNull { it.id == id }?.name ?: "Selecione" },
                                onSelected = { selectedStudentId = it },
                                modifier = Modifier.weight(1f)
                            )
                            SelectField(
                                label = "Trilha",
                                selected = selectedTrailId,
                                options = eligibleTrails.map { it.id },
                                placeholder = "Selecione",
                                optionLabel = { id -> eligibleTrails.firstOrNull { it.id == id }?.name ?: "Selecione" },
                                onSelected = { selectedTrailId = it },
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    val studentId = selectedStudentId
                                    val trailId = selectedTrailId
                                    if (studentId != null && trailId != null) {
                                        viewModel.enrollStudent(studentId, trailId)
                                    }
                                },
                                enabled = selectedStudentId != null && selectedTrailId != null,
                                modifier = Modifier.align(Alignment.Bottom).height(48.dp)
                            ) {
                                Text("Matricular")
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SelectField(
                                label = "Aluno",
                                selected = selectedStudentId,
                                options = eligibleStudents.map { it.id },
                                placeholder = "Selecione",
                                optionLabel = { id -> eligibleStudents.firstOrNull { it.id == id }?.name ?: "Selecione" },
                                onSelected = { selectedStudentId = it }
                            )
                            SelectField(
                                label = "Trilha",
                                selected = selectedTrailId,
                                options = eligibleTrails.map { it.id },
                                placeholder = "Selecione",
                                optionLabel = { id -> eligibleTrails.firstOrNull { it.id == id }?.name ?: "Selecione" },
                                onSelected = { selectedTrailId = it }
                            )
                            Button(
                                onClick = {
                                    val studentId = selectedStudentId
                                    val trailId = selectedTrailId
                                    if (studentId != null && trailId != null) {
                                        viewModel.enrollStudent(studentId, trailId)
                                    }
                                },
                                enabled = selectedStudentId != null && selectedTrailId != null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Matricular")
                            }
                        }
                    }
                }
            }
            FilterRow {
                StatusChip(
                    label = "Todas",
                    selected = state.enrollmentStatusFilter == null,
                    onClick = { viewModel.updateEnrollmentStatusFilter(null) }
                )
                EnrollmentStatus.entries.forEach { status ->
                    StatusChip(
                        label = status.label,
                        selected = state.enrollmentStatusFilter == status,
                        onClick = { viewModel.updateEnrollmentStatusFilter(status) }
                    )
                }
            }
        }

        if (records.isEmpty()) {
            item { EmptyState("Nenhuma matrícula encontrada.") }
        } else {
            items(records, key = { it.enrollment.id }) { record ->
                EnrollmentRow(
                    record = record,
                    onCancel = { viewModel.cancelEnrollment(record.enrollment.id) },
                    onComplete = { viewModel.completeEnrollment(record.enrollment.id) }
                )
            }
        }
    }
}

@Composable
private fun ReportsScreen(state: TrainingUiState) {
    ScreenColumn(
        title = "Relatórios",
        subtitle = "Listagens e indicadores consolidados do treinamento"
    ) {
        ResponsiveStatGrid(
            stats = listOf(
                Stat("Alunos cadastrados", state.students.size.toString(), "base total"),
                Stat("Trilhas ativas", state.trails.count { it.status == TrailStatus.ACTIVE }.toString(), "em operação"),
                Stat("Cursos disponíveis", state.courses.size.toString(), "no catálogo"),
                Stat("Horas disponíveis", state.courses.sumOf { it.workloadHours }.toString(), "carga total")
            )
        )
        Spacer(Modifier.height(20.dp))
        ReportSection("Alunos cadastrados") {
            if (state.students.isEmpty()) EmptyState("Nenhum aluno cadastrado.")
            state.students.forEach { student ->
                ReportLine("${student.id} - ${student.name}", "${student.email} | ${student.status.label}")
            }
        }
        Spacer(Modifier.height(16.dp))
        ReportSection("Trilhas ativas") {
            val activeTrails = state.trails.filter { it.status == TrailStatus.ACTIVE }
            if (activeTrails.isEmpty()) EmptyState("Nenhuma trilha ativa.")
            activeTrails.forEach { trail ->
                ReportLine(trail.name, "${trail.courses.size} cursos | ${trail.totalWorkloadHours}h")
            }
        }
        Spacer(Modifier.height(16.dp))
        ReportSection("Cursos disponíveis") {
            if (state.courses.isEmpty()) EmptyState("Nenhum curso disponível.")
            state.courses.forEach { course ->
                ReportLine(course.title, "${course.level.label} | ${course.category.label} | ${course.workloadHours}h")
            }
        }
    }
}

@Composable
private fun ScreenColumn(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        ScreenTitle(title, subtitle)
        Spacer(Modifier.height(20.dp))
        content()
    }
}

@Composable
private fun ListHeader(
    title: String,
    subtitle: String,
    search: String,
    searchPlaceholder: String,
    onSearchChange: (String) -> Unit,
    actionLabel: String?,
    onAction: () -> Unit
) {
    ScreenTitle(title, subtitle)
    Spacer(Modifier.height(18.dp))
    BoxWithConstraints {
        val wide = maxWidth >= 720.dp
        if (wide) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchField(
                    value = search,
                    placeholder = searchPlaceholder,
                    onValueChange = onSearchChange,
                    modifier = Modifier.weight(1f)
                )
                if (actionLabel != null) {
                    Button(onClick = onAction, modifier = Modifier.height(52.dp)) {
                        Text(actionLabel)
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SearchField(
                    value = search,
                    placeholder = searchPlaceholder,
                    onValueChange = onSearchChange
                )
                if (actionLabel != null) {
                    Button(onClick = onAction, modifier = Modifier.fillMaxWidth()) {
                        Text(actionLabel)
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun ScreenTitle(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SearchField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun FilterRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}

@Composable
private fun StatusChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, maxLines = 1) }
    )
}

@Composable
private fun StudentRow(
    student: Student,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    SurfacePanel {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EntityAvatar(text = student.name.initials())
            Column(modifier = Modifier.weight(1f)) {
                Text(student.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "ID ${student.id} | ${student.email}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            StatusBadge(label = student.status.label, color = student.status.statusColor())
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = onEdit) { Text("Editar") }
                TextButton(onClick = onDelete) { Text("Excluir") }
            }
        }
    }
}

@Composable
private fun CourseRow(
    course: Course,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    SurfacePanel {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EntityAvatar(text = course.category.label.take(2).uppercase())
            Column(modifier = Modifier.weight(1f)) {
                Text(course.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    "ID ${course.id} | ${course.workloadHours}h | ${course.level.label} | ${course.category.label}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = onEdit) { Text("Editar") }
                TextButton(onClick = onDelete) { Text("Excluir") }
            }
        }
    }
}

@Composable
private fun TrailList(
    trails: List<Trail>,
    selectedTrailId: Long?,
    onSelected: (Long) -> Unit,
    onEdit: (Trail) -> Unit,
    onDelete: (Trail) -> Unit,
    modifier: Modifier = Modifier
) {
    SurfacePanel(modifier = modifier) {
        Text("Trilhas", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        if (trails.isEmpty()) {
            EmptyState("Nenhuma trilha encontrada.")
        } else {
            trails.forEachIndexed { index, trail ->
                TrailListItem(
                    trail = trail,
                    selected = selectedTrailId == trail.id,
                    onSelected = { onSelected(trail.id) },
                    onEdit = { onEdit(trail) },
                    onDelete = { onDelete(trail) }
                )
                if (index < trails.lastIndex) HorizontalDivider(Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun TrailListItem(
    trail: Trail,
    selected: Boolean,
    onSelected: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onSelected)
            .background(if (selected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
            .padding(10.dp)
            .animateContentSize()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(trail.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${trail.courses.size} cursos | ${trail.totalWorkloadHours}h",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            StatusBadge(label = trail.status.label, color = trail.status.statusColor())
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = onEdit) { Text("Editar") }
            TextButton(onClick = onDelete) { Text("Excluir") }
        }
    }
}

@Composable
private fun TrailDetails(
    trail: Trail?,
    allCourses: List<Course>,
    onAddCourse: (Long, Long) -> Unit,
    onRemoveCourse: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    SurfacePanel(modifier = modifier) {
        if (trail == null) {
            EmptyState("Selecione uma trilha para visualizar os detalhes.")
            return@SurfacePanel
        }
        var selectedCourseId by remember(trail.id, allCourses) {
            mutableStateOf(allCourses.firstOrNull { course -> trail.courses.none { it.id == course.id } }?.id)
        }
        val availableCourses = allCourses.filter { course -> trail.courses.none { it.id == course.id } }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(trail.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    "ID ${trail.id} | ${trail.status.label}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            StatusBadge(label = "${trail.progressPercent}%", color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(10.dp))
        Text(trail.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniMetric("Carga total", "${trail.totalWorkloadHours}h", Modifier.weight(1f))
            MiniMetric("Cursos", trail.courses.size.toString(), Modifier.weight(1f))
            MiniMetric("Ativas", trail.activeEnrollments.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { trail.progressPercent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.height(20.dp))
        Text("Cursos da trilha", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        if (trail.courses.isEmpty()) {
            EmptyState("Nenhum curso adicionado a esta trilha.")
        } else {
            trail.courses.forEachIndexed { index, course ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(course.title, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${course.workloadHours}h | ${course.level.label} | ${course.category.label}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    TextButton(onClick = { onRemoveCourse(trail.id, course.id) }) {
                        Text("Remover")
                    }
                }
                if (index < trail.courses.lastIndex) HorizontalDivider(Modifier.padding(vertical = 8.dp))
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Adicionar curso", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        if (availableCourses.isEmpty()) {
            EmptyState("Todos os cursos disponíveis já estão nesta trilha.")
        } else {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SelectField(
                    label = "Curso",
                    selected = selectedCourseId,
                    options = availableCourses.map { it.id },
                    placeholder = "Selecione",
                    optionLabel = { id -> availableCourses.firstOrNull { it.id == id }?.title ?: "Selecione" },
                    onSelected = { selectedCourseId = it },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        selectedCourseId?.let { onAddCourse(trail.id, it) }
                    },
                    enabled = selectedCourseId != null,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Adicionar")
                }
            }
        }
    }
}

@Composable
private fun EnrollmentRow(
    record: EnrollmentRecord,
    onCancel: () -> Unit,
    onComplete: () -> Unit
) {
    val enrollment = record.enrollment
    SurfacePanel {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EntityAvatar(text = record.student?.name?.initials() ?: "NA")
            Column(modifier = Modifier.weight(1f)) {
                Text(record.student?.name ?: "Aluno removido", style = MaterialTheme.typography.titleMedium)
                Text(
                    "${record.trail?.name ?: "Trilha removida"} | ID ${enrollment.id}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Entrada: ${formatDate(enrollment.enrolledAt)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            StatusBadge(label = enrollment.status.label, color = enrollment.status.statusColor())
            if (enrollment.status == EnrollmentStatus.ACTIVE) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = onComplete) { Text("Concluir") }
                    TextButton(onClick = onCancel) { Text("Cancelar") }
                }
            }
        }
    }
}

@Composable
private fun EnrollmentCompactRow(record: EnrollmentRecord) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        EntityAvatar(text = record.student?.name?.initials() ?: "NA", size = 36.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(record.student?.name ?: "Aluno removido", style = MaterialTheme.typography.titleMedium)
            Text(
                record.trail?.name ?: "Trilha removida",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        StatusBadge(record.enrollment.status.label, record.enrollment.status.statusColor())
    }
}

@Composable
private fun StudentDialog(
    student: Student?,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    var name by remember(student?.id) { mutableStateOf(student?.name.orEmpty()) }
    var email by remember(student?.id) { mutableStateOf(student?.email.orEmpty()) }
    var status by remember(student?.id) { mutableStateOf(student?.status ?: StudentStatus.ACTIVE) }

    EntityDialog(
        title = if (student == null) "Novo aluno" else "Editar aluno",
        onDismiss = onDismiss,
        onConfirm = {
            onSave(
                Student(
                    id = student?.id ?: 0L,
                    name = name,
                    email = email,
                    status = status
                )
            )
        }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        SelectField(
            label = "Situação",
            selected = status,
            options = StudentStatus.entries,
            placeholder = "Selecione",
            optionLabel = { it.label },
            onSelected = { status = it }
        )
    }
}

@Composable
private fun CourseDialog(
    course: Course?,
    onDismiss: () -> Unit,
    onSave: (Course) -> Unit
) {
    var title by remember(course?.id) { mutableStateOf(course?.title.orEmpty()) }
    var workload by remember(course?.id) { mutableStateOf(course?.workloadHours?.toString().orEmpty()) }
    var level by remember(course?.id) { mutableStateOf(course?.level ?: CourseLevel.BASIC) }
    var category by remember(course?.id) { mutableStateOf(course?.category ?: CourseCategory.KOTLIN) }

    EntityDialog(
        title = if (course == null) "Novo curso" else "Editar curso",
        onDismiss = onDismiss,
        onConfirm = {
            onSave(
                Course(
                    id = course?.id ?: 0L,
                    title = title,
                    workloadHours = workload.toIntOrNull() ?: 0,
                    level = level,
                    category = category
                )
            )
        }
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        OutlinedTextField(
            value = workload,
            onValueChange = { workload = it.filter(Char::isDigit) },
            label = { Text("Carga horária") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        SelectField(
            label = "Nível",
            selected = level,
            options = CourseLevel.entries,
            placeholder = "Selecione",
            optionLabel = { it.label },
            onSelected = { level = it }
        )
        SelectField(
            label = "Categoria",
            selected = category,
            options = CourseCategory.entries,
            placeholder = "Selecione",
            optionLabel = { it.label },
            onSelected = { category = it }
        )
    }
}

@Composable
private fun TrailDialog(
    trail: Trail?,
    onDismiss: () -> Unit,
    onSave: (Trail) -> Unit
) {
    var name by remember(trail?.id) { mutableStateOf(trail?.name.orEmpty()) }
    var description by remember(trail?.id) { mutableStateOf(trail?.description.orEmpty()) }
    var status by remember(trail?.id) { mutableStateOf(trail?.status ?: TrailStatus.PLANNED) }

    EntityDialog(
        title = if (trail == null) "Nova trilha" else "Editar trilha",
        onDismiss = onDismiss,
        onConfirm = {
            onSave(
                Trail(
                    id = trail?.id ?: 0L,
                    name = name,
                    description = description,
                    status = status,
                    courses = trail?.courses.orEmpty(),
                    activeEnrollments = trail?.activeEnrollments ?: 0,
                    completedEnrollments = trail?.completedEnrollments ?: 0
                )
            )
        }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descrição") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )
        SelectField(
            label = "Status",
            selected = status,
            options = TrailStatus.entries,
            placeholder = "Selecione",
            optionLabel = { it.label },
            onSelected = { status = it }
        )
    }
}

@Composable
private fun EntityDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.widthIn(min = 280.dp, max = 520.dp)
            ) {
                content()
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Excluir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun <T> SelectField(
    label: String,
    selected: T?,
    options: List<T>,
    placeholder: String,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                enabled = options.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = selected?.let(optionLabel) ?: placeholder,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(optionLabel(option)) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SurfacePanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun ResponsiveStatGrid(stats: List<Stat>) {
    BoxWithConstraints {
        val columns = when {
            maxWidth >= 1100.dp -> 4
            maxWidth >= 640.dp -> 2
            else -> 1
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            stats.chunked(columns).forEach { rowStats ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowStats.forEach { stat ->
                        StatCard(stat, Modifier.weight(1f))
                    }
                    repeat(columns - rowStats.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(stat: Stat, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.heightIn(min = 116.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                stat.label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(10.dp))
            Text(
                stat.value,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                stat.description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun IndicatorRow(label: String, value: Int, total: Int) {
    val progress = if (total == 0) 0f else (value.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Text("$value / $total", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    }
}

@Composable
private fun MiniMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun EntityAvatar(text: String, size: Dp = 44.dp) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        modifier = Modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text.take(2).uppercase(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun StatusBadge(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Text(
            text = label,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1
        )
    }
}

@Composable
private fun EmptyState(message: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ReportSection(title: String, content: @Composable () -> Unit) {
    SurfacePanel {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun ReportLine(primary: String, secondary: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(primary, style = MaterialTheme.typography.titleMedium)
        Text(secondary, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

private data class Stat(
    val label: String,
    val value: String,
    val description: String
)

private fun TrainingUiState.filteredStudents(): List<Student> {
    val query = studentSearch.trim().lowercase()
    return students.filter { student ->
        val matchesQuery = query.isBlank()
            || student.name.lowercase().contains(query)
            || student.email.lowercase().contains(query)
            || student.id.toString().contains(query)
        val matchesStatus = studentStatusFilter == null || student.status == studentStatusFilter
        matchesQuery && matchesStatus
    }
}

private fun TrainingUiState.filteredCourses(): List<Course> {
    val query = courseSearch.trim().lowercase()
    return courses.filter { course ->
        val matchesQuery = query.isBlank()
            || course.title.lowercase().contains(query)
            || course.category.label.lowercase().contains(query)
            || course.level.label.lowercase().contains(query)
            || course.id.toString().contains(query)
        val matchesLevel = courseLevelFilter == null || course.level == courseLevelFilter
        val matchesCategory = courseCategoryFilter == null || course.category == courseCategoryFilter
        matchesQuery && matchesLevel && matchesCategory
    }
}

private fun TrainingUiState.filteredTrails(): List<Trail> {
    val query = trailSearch.trim().lowercase()
    return trails.filter { trail ->
        val matchesQuery = query.isBlank()
            || trail.name.lowercase().contains(query)
            || trail.description.lowercase().contains(query)
            || trail.status.label.lowercase().contains(query)
            || trail.id.toString().contains(query)
        val matchesStatus = trailStatusFilter == null || trail.status == trailStatusFilter
        matchesQuery && matchesStatus
    }
}

private fun TrainingUiState.filteredEnrollments(): List<EnrollmentRecord> {
    val query = enrollmentSearch.trim().lowercase()
    return enrollments.filter { record ->
        val matchesQuery = query.isBlank()
            || record.student?.name.orEmpty().lowercase().contains(query)
            || record.trail?.name.orEmpty().lowercase().contains(query)
            || record.enrollment.id.toString().contains(query)
        val matchesStatus = enrollmentStatusFilter == null || record.enrollment.status == enrollmentStatusFilter
        matchesQuery && matchesStatus
    }
}

private fun StudentStatus.statusColor(): Color = when (this) {
    StudentStatus.ACTIVE -> Color(0xFF2E7D32)
    StudentStatus.INACTIVE -> Color(0xFF757575)
    StudentStatus.BLOCKED -> Color(0xFFC62828)
}

private fun TrailStatus.statusColor(): Color = when (this) {
    TrailStatus.PLANNED -> Color(0xFF757575)
    TrailStatus.ACTIVE -> Color(0xFF1565C0)
    TrailStatus.COMPLETED -> Color(0xFF2E7D32)
    TrailStatus.ARCHIVED -> Color(0xFF1E1E1E)
}

private fun EnrollmentStatus.statusColor(): Color = when (this) {
    EnrollmentStatus.ACTIVE -> Color(0xFF1565C0)
    EnrollmentStatus.CANCELLED -> Color(0xFFC62828)
    EnrollmentStatus.COMPLETED -> Color(0xFF2E7D32)
}

private fun String.initials(): String {
    val parts = trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "NA"
        parts.size == 1 -> parts.first().take(2)
        else -> parts.first().take(1) + parts.last().take(1)
    }
}

private fun formatDate(timestamp: Long): String {
    val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR"))
    return format.format(Date(timestamp))
}
