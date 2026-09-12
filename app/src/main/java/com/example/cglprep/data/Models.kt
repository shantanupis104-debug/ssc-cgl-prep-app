package com.example.cglprep.data

data class Question(
    val id: Int,
    val section: String,
    val stem: String,
    val stem_hi: String? = null,
    val options: List<String> = emptyList(),
    val options_hi: List<String>? = null,
    val correct: String,
    val solution: String? = null,
    val solution_hi: String? = null,
    val is_pyq: Boolean = false
)

data class MockExam(
    val mock_id: String,
    val mock_number: Int,
    val title: String,
    val date: String? = null,
    val exam_pillar: String = "ssc",
    val total_questions: Int = 100,
    val max_marks: Double = 200.0,
    val duration_minutes: Int = 60,
    val sectional_timing_minutes: Int? = null,
    val questions: List<Question> = emptyList()
)

data class PYQPaper(
    val paper_id: String,
    val year: Int,
    val title: String,
    val shift: String,
    val historical_cutoff: Double,
    val total_questions: Int = 100,
    val max_marks: Double = 200.0,
    val duration_minutes: Int = 60,
    val questions: List<Question> = emptyList()
)

data class RevisionCard(
    val id: String,
    val subject: String,
    val title: String,
    val points: List<String>,
    val formulaOrTrick: String? = null,
    val tag: String = "High Yield"
)

enum class ExamPillar(val key: String, val label: String, val badge: String) {
    SSC("ssc", "SSC CGL 2026", "Tier-1 200M"),
    BANKING("banking", "Banking / IBPS PO", "Sectional 100M"),
    RAILWAY("railway", "Railway RRB NTPC", "CBT-1 100M")
}

enum class ExamPreset(
    val key: String,
    val title: String,
    val totalQuestions: Int,
    val durationMinutes: Int,
    val markCorrect: Double,
    val markWrong: Double,
    val totalMarks: Double,
    val hasSectionalTiming: Boolean
) {
    SSC_TIER_1("ssc_tier1", "SSC CGL Tier-1 (TCS iON)", 100, 60, 2.0, 0.5, 200.0, false),
    BANKING_PRELIMS("banking_prelims", "IBPS / SBI PO Prelims", 100, 60, 1.0, 0.25, 100.0, true),
    RAILWAY_NTPC("railway_ntpc", "RRB NTPC Stage-1", 100, 90, 1.0, 0.33, 100.0, false)
}

enum class QuestionStatus(val label: String, val code: Int) {
    NOT_VISITED("Not Visited", 0),
    NOT_ANSWERED("Not Answered", 1),
    ANSWERED("Answered", 2),
    MARKED_FOR_REVIEW("Marked for Review", 3),
    ANSWERED_AND_MARKED("Answered & Marked for Review", 4)
}

enum class ErrorBucket(val code: String, val title: String, val description: String) {
    BUCKET_A("BUCKET_A", "Conceptual Gap", "Lack of concept / syllabus revision required"),
    BUCKET_B("BUCKET_B", "Silly Mistake", "Calculation error or misread stem"),
    BUCKET_C("BUCKET_C", "Guesswork / Panic", "Rushed under time pressure or blind guess")
}

data class SectionStats(
    val section: String,
    val attempted: Int = 0,
    val correct: Int = 0,
    val wrong: Int = 0,
    val timeSpentSeconds: Long = 0,
    val recommendedTimeMins: String = "15m"
) {
    val accuracy: Int
        get() = if (attempted > 0) ((correct.toDouble() / attempted) * 100).toInt() else 0
    val score: Double
        get() = (correct * 2.0) - (wrong * 0.5)
}

data class ExamTelemetry(
    val timeSinks: List<Pair<Int, Long>>, // (qNum, seconds spent on incorrect)
    val speedWins: List<Pair<Int, Long>>, // (qNum, seconds spent on correct < 30s)
    val avgTimePerQuestionSec: Long
)
