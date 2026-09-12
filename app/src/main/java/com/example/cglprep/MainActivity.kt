package com.example.cglprep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cglprep.data.CglDatabase
import com.example.cglprep.data.ExamRepository
import com.example.cglprep.ui.AppTab
import com.example.cglprep.ui.CglViewModel
import com.example.cglprep.ui.components.BottomNavBar
import com.example.cglprep.ui.components.StudentProfileDialog
import com.example.cglprep.ui.components.TopHeader
import com.example.cglprep.ui.screens.*
import com.example.cglprep.ui.theme.AccentGold
import com.example.cglprep.ui.theme.CglPrepTheme
import com.example.cglprep.ui.theme.NavyDark

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = CglDatabase.getDatabase(applicationContext)
        val repository = ExamRepository(applicationContext, database)

        setContent {
            CglPrepTheme {
                val viewModel: CglViewModel = viewModel(
                    factory = CglViewModel.provideFactory(repository)
                )

                val activeStudent by viewModel.activeStudent.collectAsStateWithLifecycle()
                val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
                val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
                val activePillar by viewModel.activePillar.collectAsStateWithLifecycle()
                val activePreset by viewModel.activePreset.collectAsStateWithLifecycle()
                val isHindi by viewModel.isHindi.collectAsStateWithLifecycle()
                val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

                val allErrors by viewModel.allErrors.collectAsStateWithLifecycle()
                val allAttempts by viewModel.allAttempts.collectAsStateWithLifecycle()
                val lastExamResult by viewModel.lastExamResult.collectAsStateWithLifecycle()

                // CBT state
                val activeExamQuestions by viewModel.activeExamQuestions.collectAsStateWithLifecycle()
                val currentIndex by viewModel.currentQuestionIndex.collectAsStateWithLifecycle()
                val userAnswers by viewModel.userAnswers.collectAsStateWithLifecycle()
                val questionStatuses by viewModel.questionStatuses.collectAsStateWithLifecycle()
                val timeSpentPerQuestion by viewModel.timeSpentPerQuestion.collectAsStateWithLifecycle()
                val remainingSeconds by viewModel.remainingSeconds.collectAsStateWithLifecycle()
                val selectedErrorBucket by viewModel.selectedErrorBucket.collectAsStateWithLifecycle()

                var showProfileModal by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        // Show TopHeader unless in active CBT exam or viewing result
                        if (currentTab != AppTab.CBT_EXAM && lastExamResult == null) {
                            TopHeader(
                                activeStudent = activeStudent,
                                activePreset = activePreset,
                                isHindi = isHindi,
                                onPresetSelected = { viewModel.switchPreset(it) },
                                onToggleLanguage = { viewModel.toggleLanguage() },
                                onOpenProfileModal = { showProfileModal = true }
                            )
                        }
                    },
                    bottomBar = {
                        // Hide bottom nav during active CBT exam or result screen
                        if (currentTab != AppTab.CBT_EXAM && lastExamResult == null) {
                            BottomNavBar(
                                currentTab = currentTab,
                                errorCount = allErrors.size,
                                onTabSelected = { viewModel.switchTab(it) }
                            )
                        }
                    },
                    contentWindowInsets = WindowInsets.safeDrawing,
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        when {
                            isLoading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = AccentGold)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            "Initializing TCS iON 2026 Question Vault...",
                                            color = NavyDark,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }

                            lastExamResult != null -> {
                                ExamResultScreen(
                                    result = lastExamResult!!,
                                    isHindi = isHindi,
                                    onDismiss = { viewModel.dismissResult() },
                                    onLogCustomErrorBucket = { q, bucket ->
                                        viewModel.changeErrorBucket(q.id, bucket)
                                    }
                                )
                            }

                            currentTab == AppTab.CBT_EXAM -> {
                                CbtSimulatorScreen(
                                    questions = activeExamQuestions,
                                    currentIndex = currentIndex,
                                    userAnswers = userAnswers,
                                    questionStatuses = questionStatuses,
                                    timeSpentPerQuestion = timeSpentPerQuestion,
                                    remainingSeconds = remainingSeconds,
                                    activePreset = activePreset,
                                    isHindi = isHindi,
                                    onSelectOption = { viewModel.selectOption(it) },
                                    onClearResponse = { viewModel.clearResponse() },
                                    onSaveAndNext = { viewModel.saveAndNext() },
                                    onMarkForReviewAndNext = { viewModel.markForReviewAndNext() },
                                    onJumpToQuestion = { viewModel.jumpToQuestion(it) },
                                    onSubmitExam = { viewModel.submitExam() },
                                    onToggleLanguage = { viewModel.toggleLanguage() }
                                )
                            }

                            currentTab == AppTab.DASHBOARD -> {
                                DashboardScreen(
                                    activePillar = activePillar,
                                    dailyMocks = viewModel.getDailyMocks(),
                                    bankingMocks = viewModel.getBankingMocks(),
                                    railwayMocks = viewModel.getRailwayMocks(),
                                    spacedDueErrors = allErrors.filter {
                                        !it.isMastered && it.nextReviewDate <= System.currentTimeMillis()
                                    },
                                    totalErrors = allErrors.size,
                                    attempts = allAttempts,
                                    onSelectPillar = { viewModel.switchPillar(it) },
                                    onStartMock = { viewModel.startDailyMock(it) },
                                    onLaunchSpacedRevision = {
                                        val due = allErrors.filter { !it.isMastered && it.nextReviewDate <= System.currentTimeMillis() }
                                        viewModel.startSpacedErrorRetest(due)
                                    }
                                )
                            }

                            currentTab == AppTab.PYQ_VAULT -> {
                                PyqVaultScreen(
                                    pyqPapers = viewModel.getPyqPapers(),
                                    onLaunchPyq = { viewModel.startPyqExam(it) }
                                )
                            }

                            currentTab == AppTab.ERROR_DIARY -> {
                                ErrorDiaryScreen(
                                    errors = allErrors,
                                    selectedBucket = selectedErrorBucket,
                                    isHindi = isHindi,
                                    onFilterBucket = { viewModel.filterErrorBucket(it) },
                                    onChangeBucket = { qid, bucket -> viewModel.changeErrorBucket(qid, bucket) },
                                    onMarkMastered = { qid, mastered -> viewModel.markErrorMastered(qid, mastered) },
                                    onDeleteError = { qid -> viewModel.deleteError(qid) },
                                    onLaunchRetest = { errorsToTest -> viewModel.startSpacedErrorRetest(errorsToTest) }
                                )
                            }

                            currentTab == AppTab.ANALYTICS -> {
                                AnalyticsScreen(
                                    attempts = allAttempts
                                )
                            }

                            currentTab == AppTab.REVISION -> {
                                RevisionVaultScreen(
                                    revisionCards = viewModel.getRevisionCards(),
                                    onBookmark = { cat, title, content ->
                                        viewModel.addBookmark(cat, title, content)
                                    }
                                )
                            }
                        }
                    }

                    if (showProfileModal) {
                        StudentProfileDialog(
                            activeStudent = activeStudent,
                            allStudents = allStudents,
                            onDismiss = { showProfileModal = false },
                            onSelectStudent = {
                                viewModel.switchActiveStudent(it)
                                showProfileModal = false
                            },
                            onAddStudent = { name, email, target ->
                                viewModel.createStudent(name, email, target)
                                showProfileModal = false
                            }
                        )
                    }
                }
            }
        }
    }
}
