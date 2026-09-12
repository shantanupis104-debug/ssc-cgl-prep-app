package com.example.cglprep.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cglprep.data.*
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    DASHBOARD("Dashboard"),
    CBT_EXAM("CBT Simulator"),
    PYQ_VAULT("PYQ Vault"),
    ERROR_DIARY("Error Diary"),
    ANALYTICS("Analytics"),
    REVISION("Revision Vault")
}

data class ExamResultData(
    val title: String,
    val pillar: String,
    val preset: String,
    val score: Double,
    val maxScore: Double,
    val accuracy: Int,
    val attempted: Int,
    val correct: Int,
    val wrong: Int,
    val unattempted: Int,
    val timeSpentSeconds: Long,
    val sectionStats: List<SectionStats>,
    val telemetry: ExamTelemetry,
    val questions: List<Question>,
    val userAnswers: Map<Int, String>
)

class CglViewModel(private val repository: ExamRepository) : ViewModel() {

    val activeStudent: StateFlow<StudentProfileEntity?> = repository.activeStudent
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allStudents: StateFlow<List<StudentProfileEntity>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allErrors: StateFlow<List<ErrorDiaryEntity>> = repository.allErrors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttempts: StateFlow<List<ExamAttemptEntity>> = repository.allAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _activePillar = MutableStateFlow(ExamPillar.SSC)
    val activePillar: StateFlow<ExamPillar> = _activePillar.asStateFlow()

    private val _activePreset = MutableStateFlow(ExamPreset.SSC_TIER_1)
    val activePreset: StateFlow<ExamPreset> = _activePreset.asStateFlow()

    private val _isHindi = MutableStateFlow(false)
    val isHindi: StateFlow<Boolean> = _isHindi.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // CBT State
    private val _activeExamQuestions = MutableStateFlow<List<Question>>(emptyList())
    val activeExamQuestions: StateFlow<List<Question>> = _activeExamQuestions.asStateFlow()

    private val _activeExamTitle = MutableStateFlow("CBT Mock")
    val activeExamTitle: StateFlow<String> = _activeExamTitle.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _userAnswers = MutableStateFlow<Map<Int, String>>(emptyMap())
    val userAnswers: StateFlow<Map<Int, String>> = _userAnswers.asStateFlow()

    private val _questionStatuses = MutableStateFlow<Map<Int, QuestionStatus>>(emptyMap())
    val questionStatuses: StateFlow<Map<Int, QuestionStatus>> = _questionStatuses.asStateFlow()

    private val _timeSpentPerQuestion = MutableStateFlow<Map<Int, Long>>(emptyMap())
    val timeSpentPerQuestion: StateFlow<Map<Int, Long>> = _timeSpentPerQuestion.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(3600L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds.asStateFlow()

    private val _isExamOngoing = MutableStateFlow(false)
    val isExamOngoing: StateFlow<Boolean> = _isExamOngoing.asStateFlow()

    private val _lastExamResult = MutableStateFlow<ExamResultData?>(null)
    val lastExamResult: StateFlow<ExamResultData?> = _lastExamResult.asStateFlow()

    // Error Diary Filter
    private val _selectedErrorBucket = MutableStateFlow("ALL")
    val selectedErrorBucket: StateFlow<String> = _selectedErrorBucket.asStateFlow()

    private var timerJob: Job? = null
    private var qStopwatchJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initialize()
            _isLoading.value = false
        }
    }

    fun switchTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun switchPillar(pillar: ExamPillar) {
        _activePillar.value = pillar
        when (pillar) {
            ExamPillar.SSC -> _activePreset.value = ExamPreset.SSC_TIER_1
            ExamPillar.BANKING -> _activePreset.value = ExamPreset.BANKING_PRELIMS
            ExamPillar.RAILWAY -> _activePreset.value = ExamPreset.RAILWAY_NTPC
        }
    }

    fun switchPreset(preset: ExamPreset) {
        _activePreset.value = preset
    }

    fun toggleLanguage() {
        _isHindi.value = !_isHindi.value
    }

    fun filterErrorBucket(bucket: String) {
        _selectedErrorBucket.value = bucket
    }

    fun getDailyMocks(): List<MockExam> = repository.getDailyMocks()
    fun getPyqPapers(): List<PYQPaper> = repository.getPyqPapers()
    fun getBankingMocks(): List<MockExam> = repository.getBankingMocks()
    fun getRailwayMocks(): List<MockExam> = repository.getRailwayMocks()
    fun getPastMocks(): List<MockExam> = repository.getPastMocks()
    fun getRevisionCards(): List<RevisionCard> = repository.getRevisionCards()

    // Launchers
    fun startDailyMock(mock: MockExam) {
        setupCbt(
            questions = mock.questions,
            title = mock.title,
            durationMins = mock.duration_minutes
        )
    }

    fun startPyqExam(pyq: PYQPaper) {
        setupCbt(
            questions = pyq.questions,
            title = "${pyq.year} - ${pyq.title} (${pyq.shift})",
            durationMins = pyq.duration_minutes
        )
    }

    fun startSpacedErrorRetest(dueErrors: List<ErrorDiaryEntity>) {
        val questions = dueErrors.map { error ->
            val opts: List<String> = try {
                val listType = object : TypeToken<List<String>>() {}.type
                com.google.gson.Gson().fromJson(error.optionsJson, listType) ?: emptyList<String>()
            } catch (e: Exception) {
                emptyList<String>()
            }
            Question(
                id = error.qid,
                section = error.section,
                stem = error.stem,
                stem_hi = error.stemHi,
                options = opts,
                correct = error.correct,
                solution = error.solution,
                solution_hi = error.solutionHi
            )
        }
        setupCbt(
            questions = questions,
            title = "Spaced Repetition Error Retest (${questions.size} Qs)",
            durationMins = (questions.size * 1.2).toInt().coerceAtLeast(10)
        )
    }

    private fun setupCbt(questions: List<Question>, title: String, durationMins: Int) {
        stopTimers()
        _activeExamQuestions.value = questions
        _activeExamTitle.value = title
        _currentQuestionIndex.value = 0
        _userAnswers.value = emptyMap()
        _timeSpentPerQuestion.value = emptyMap()
        _remainingSeconds.value = durationMins * 60L
        _lastExamResult.value = null

        val initialStatuses = mutableMapOf<Int, QuestionStatus>()
        questions.forEachIndexed { index, q ->
            initialStatuses[q.id] = if (index == 0) QuestionStatus.NOT_ANSWERED else QuestionStatus.NOT_VISITED
        }
        _questionStatuses.value = initialStatuses
        _isExamOngoing.value = true
        _currentTab.value = AppTab.CBT_EXAM

        startTimer()
        startQuestionStopwatch()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0 && _isExamOngoing.value) {
                delay(1000)
                _remainingSeconds.value -= 1
            }
            if (_remainingSeconds.value <= 0 && _isExamOngoing.value) {
                submitExam()
            }
        }
    }

    private fun startQuestionStopwatch() {
        qStopwatchJob?.cancel()
        qStopwatchJob = viewModelScope.launch {
            while (_isExamOngoing.value) {
                delay(1000)
                val currentQ = _activeExamQuestions.value.getOrNull(_currentQuestionIndex.value)
                if (currentQ != null) {
                    val currentMap = _timeSpentPerQuestion.value.toMutableMap()
                    val currentSec = currentMap[currentQ.id] ?: 0L
                    currentMap[currentQ.id] = currentSec + 1
                    _timeSpentPerQuestion.value = currentMap
                }
            }
        }
    }

    private fun stopTimers() {
        timerJob?.cancel()
        timerJob = null
        qStopwatchJob?.cancel()
        qStopwatchJob = null
    }

    fun selectOption(option: String) {
        val currentQ = _activeExamQuestions.value.getOrNull(_currentQuestionIndex.value) ?: return
        val currentAnswers = _userAnswers.value.toMutableMap()
        currentAnswers[currentQ.id] = option
        _userAnswers.value = currentAnswers
    }

    fun clearResponse() {
        val currentQ = _activeExamQuestions.value.getOrNull(_currentQuestionIndex.value) ?: return
        val currentAnswers = _userAnswers.value.toMutableMap()
        currentAnswers.remove(currentQ.id)
        _userAnswers.value = currentAnswers

        val currentStatuses = _questionStatuses.value.toMutableMap()
        currentStatuses[currentQ.id] = QuestionStatus.NOT_ANSWERED
        _questionStatuses.value = currentStatuses
    }

    fun saveAndNext() {
        val currentQ = _activeExamQuestions.value.getOrNull(_currentQuestionIndex.value) ?: return
        val hasAnswer = _userAnswers.value.containsKey(currentQ.id)

        val currentStatuses = _questionStatuses.value.toMutableMap()
        currentStatuses[currentQ.id] = if (hasAnswer) QuestionStatus.ANSWERED else QuestionStatus.NOT_ANSWERED
        _questionStatuses.value = currentStatuses

        goToNextQuestion()
    }

    fun markForReviewAndNext() {
        val currentQ = _activeExamQuestions.value.getOrNull(_currentQuestionIndex.value) ?: return
        val hasAnswer = _userAnswers.value.containsKey(currentQ.id)

        val currentStatuses = _questionStatuses.value.toMutableMap()
        currentStatuses[currentQ.id] = if (hasAnswer) QuestionStatus.ANSWERED_AND_MARKED else QuestionStatus.MARKED_FOR_REVIEW
        _questionStatuses.value = currentStatuses

        goToNextQuestion()
    }

    fun jumpToQuestion(index: Int) {
        if (index in _activeExamQuestions.value.indices) {
            val oldQ = _activeExamQuestions.value.getOrNull(_currentQuestionIndex.value)
            if (oldQ != null) {
                val oldStatus = _questionStatuses.value[oldQ.id]
                if (oldStatus == QuestionStatus.NOT_VISITED) {
                    val map = _questionStatuses.value.toMutableMap()
                    map[oldQ.id] = QuestionStatus.NOT_ANSWERED
                    _questionStatuses.value = map
                }
            }

            _currentQuestionIndex.value = index
            val newQ = _activeExamQuestions.value[index]
            val newStatus = _questionStatuses.value[newQ.id]
            if (newStatus == QuestionStatus.NOT_VISITED) {
                val map = _questionStatuses.value.toMutableMap()
                map[newQ.id] = QuestionStatus.NOT_ANSWERED
                _questionStatuses.value = map
            }
        }
    }

    private fun goToNextQuestion() {
        val nextIdx = _currentQuestionIndex.value + 1
        if (nextIdx < _activeExamQuestions.value.size) {
            jumpToQuestion(nextIdx)
        }
    }

    fun submitExam() {
        stopTimers()
        _isExamOngoing.value = false

        val questions = _activeExamQuestions.value
        val answers = _userAnswers.value
        val times = _timeSpentPerQuestion.value
        val preset = _activePreset.value

        var correct = 0
        var wrong = 0
        var unattempted = 0

        val timeSinks = mutableListOf<Pair<Int, Long>>()
        val speedWins = mutableListOf<Pair<Int, Long>>()

        val sectionMap = mutableMapOf<String, SectionStats>()
        listOf(
            "General Intelligence & Reasoning" to "15m",
            "General Awareness" to "8m",
            "Quantitative Aptitude" to "22m",
            "English Comprehension" to "12m"
        ).forEach { (sec, rec) ->
            sectionMap[sec] = SectionStats(section = sec, recommendedTimeMins = rec)
        }

        val student = activeStudent.value
        val studentId = student?.id ?: "student_default"

        questions.forEachIndexed { index, q ->
            val userChoice = answers[q.id]
            val timeSec = times[q.id] ?: 0L
            val qNum = index + 1
            val currentSecStat = sectionMap[q.section] ?: SectionStats(section = q.section)

            if (userChoice == null) {
                unattempted++
                sectionMap[q.section] = currentSecStat.copy(
                    timeSpentSeconds = currentSecStat.timeSpentSeconds + timeSec
                )
            } else if (userChoice.trim().equals(q.correct.trim(), ignoreCase = true)) {
                correct++
                if (timeSec < 30) speedWins.add(qNum to timeSec)
                sectionMap[q.section] = currentSecStat.copy(
                    attempted = currentSecStat.attempted + 1,
                    correct = currentSecStat.correct + 1,
                    timeSpentSeconds = currentSecStat.timeSpentSeconds + timeSec
                )
            } else {
                wrong++
                if (timeSec > 90) timeSinks.add(qNum to timeSec)
                sectionMap[q.section] = currentSecStat.copy(
                    attempted = currentSecStat.attempted + 1,
                    wrong = currentSecStat.wrong + 1,
                    timeSpentSeconds = currentSecStat.timeSpentSeconds + timeSec
                )
                // Log error into 3-Bucket Error Diary
                viewModelScope.launch {
                    repository.logError(
                        studentId = studentId,
                        question = q,
                        userChoice = userChoice,
                        bucket = ErrorBucket.BUCKET_A
                    )
                }
            }
        }

        val attempted = correct + wrong
        val rawScore = (correct * preset.markCorrect) - (wrong * preset.markWrong)
        val accuracy = if (attempted > 0) ((correct.toDouble() / attempted) * 100).toInt() else 0
        val totalTimeSec = times.values.sum()
        val avgSec = if (questions.isNotEmpty()) totalTimeSec / questions.size else 0L

        val telemetry = ExamTelemetry(
            timeSinks = timeSinks,
            speedWins = speedWins,
            avgTimePerQuestionSec = avgSec
        )

        val result = ExamResultData(
            title = _activeExamTitle.value,
            pillar = _activePillar.value.label,
            preset = preset.title,
            score = rawScore.coerceAtLeast(0.0),
            maxScore = preset.totalMarks,
            accuracy = accuracy,
            attempted = attempted,
            correct = correct,
            wrong = wrong,
            unattempted = unattempted,
            timeSpentSeconds = totalTimeSec,
            sectionStats = sectionMap.values.toList(),
            telemetry = telemetry,
            questions = questions,
            userAnswers = answers
        )

        _lastExamResult.value = result

        viewModelScope.launch {
            repository.saveExamResult(
                studentId = studentId,
                mockTitle = _activeExamTitle.value,
                pillar = _activePillar.value.key,
                preset = preset.key,
                score = result.score,
                maxScore = result.maxScore,
                accuracy = result.accuracy,
                attempted = attempted,
                correct = correct,
                wrong = wrong,
                unattempted = unattempted,
                timeSpentSec = totalTimeSec
            )
        }
    }

    fun dismissResult() {
        _lastExamResult.value = null
        _currentTab.value = AppTab.DASHBOARD
    }

    // Error diary actions
    fun changeErrorBucket(qid: Int, bucket: ErrorBucket) {
        viewModelScope.launch {
            repository.updateErrorBucket(qid, bucket)
        }
    }

    fun markErrorMastered(qid: Int, mastered: Boolean) {
        viewModelScope.launch {
            repository.markErrorMastered(qid, mastered)
        }
    }

    fun deleteError(qid: Int) {
        viewModelScope.launch {
            repository.deleteError(qid)
        }
    }

    // Student switcher actions
    fun switchActiveStudent(id: String) {
        viewModelScope.launch {
            repository.switchStudent(id)
        }
    }

    fun createStudent(name: String, email: String, targetExam: String) {
        viewModelScope.launch {
            repository.addStudent(name, email, targetExam)
        }
    }

    fun addBookmark(category: String, title: String, content: String) {
        viewModelScope.launch {
            repository.addBookmark(category, title, content)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimers()
    }

    companion object {
        fun provideFactory(repository: ExamRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CglViewModel(repository) as T
                }
            }
    }
}
