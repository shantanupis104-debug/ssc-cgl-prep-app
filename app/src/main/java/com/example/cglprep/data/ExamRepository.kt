package com.example.cglprep.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStreamReader

class ExamRepository(
    private val context: Context,
    private val database: CglDatabase
) {
    private val studentDao = database.studentDao()
    private val errorDiaryDao = database.errorDiaryDao()
    private val attemptDao = database.examAttemptDao()
    private val bookmarkDao = database.bookmarkDao()

    private val gson = Gson()

    // Cached in-memory parsed exam data
    private var cachedDailyMocks: List<MockExam> = emptyList()
    private var cachedPyqPapers: List<PYQPaper> = emptyList()
    private var cachedBankingMocks: List<MockExam> = emptyList()
    private var cachedRailwayMocks: List<MockExam> = emptyList()
    private var cachedPastMocks: List<MockExam> = emptyList()
    private var cachedRevisionCards: List<RevisionCard> = emptyList()
    private var isLoaded: Boolean = false

    val activeStudent: Flow<StudentProfileEntity?> = studentDao.getActiveStudent()
    val allStudents: Flow<List<StudentProfileEntity>> = studentDao.getAllStudents()
    val allErrors: Flow<List<ErrorDiaryEntity>> = errorDiaryDao.getAllErrors()
    val allAttempts: Flow<List<ExamAttemptEntity>> = attemptDao.getAllAttempts()
    val allBookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()

    fun getSpacedDueErrors(currentTime: Long): Flow<List<ErrorDiaryEntity>> {
        return errorDiaryDao.getSpacedDueErrors(currentTime)
    }

    fun getErrorsByBucket(bucket: String): Flow<List<ErrorDiaryEntity>> {
        return errorDiaryDao.getErrorsByBucket(bucket)
    }

    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (isLoaded) return@withContext

        // Ensure default student exists
        val defaultStudent = StudentProfileEntity(
            id = "student_default",
            name = "Aspirant Rahul Sharma",
            email = "shantanupis104@gmail.com",
            targetExam = "SSC CGL 2026 (Auditor / ASO)",
            rollNumber = "CGL-2026-9081",
            avatarColor = 0xFFF59E0BL,
            isActive = true
        )
        studentDao.insertStudent(defaultStudent)

        try {
            context.assets.open("cgl_app_database.json").use { stream ->
                InputStreamReader(stream, "UTF-8").use { reader ->
                    val root = JSONObject(reader.readText())

                    // 1. Daily Mocks
                    if (root.has("daily_mocks")) {
                        val mocksArray = root.getJSONArray("daily_mocks")
                        val list = mutableListOf<MockExam>()
                        for (i in 0 until mocksArray.length()) {
                            val item = mocksArray.getJSONObject(i)
                            list.add(parseMock(item, "ssc"))
                        }
                        cachedDailyMocks = list
                    }

                    // 2. PYQ Papers
                    if (root.has("pyq_papers")) {
                        val pyqArray = root.getJSONArray("pyq_papers")
                        val list = mutableListOf<PYQPaper>()
                        for (i in 0 until pyqArray.length()) {
                            val item = pyqArray.getJSONObject(i)
                            list.add(parsePyq(item))
                        }
                        cachedPyqPapers = list
                    }

                    // 3. Banking Mocks
                    if (root.has("banking_mocks")) {
                        val bankArray = root.getJSONArray("banking_mocks")
                        val list = mutableListOf<MockExam>()
                        for (i in 0 until bankArray.length()) {
                            val item = bankArray.getJSONObject(i)
                            list.add(parseMock(item, "banking"))
                        }
                        cachedBankingMocks = list
                    }

                    // 4. Railway Mocks
                    if (root.has("railway_mocks")) {
                        val rwyArray = root.getJSONArray("railway_mocks")
                        val list = mutableListOf<MockExam>()
                        for (i in 0 until rwyArray.length()) {
                            val item = rwyArray.getJSONObject(i)
                            list.add(parseMock(item, "railway"))
                        }
                        cachedRailwayMocks = list
                    }

                    // 5. Past Mocks Archive
                    if (root.has("past_mocks_archive")) {
                        val pastArray = root.getJSONArray("past_mocks_archive")
                        val list = mutableListOf<MockExam>()
                        for (i in 0 until pastArray.length()) {
                            val item = pastArray.getJSONObject(i)
                            list.add(parseMock(item, "ssc"))
                        }
                        cachedPastMocks = list
                    }

                    // 6. Revision Notes & Boosters
                    val cards = mutableListOf<RevisionCard>()
                    if (root.has("revision_notes")) {
                        val revObj = root.getJSONObject("revision_notes")
                        val keys = revObj.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val subjectVal = revObj.get(key)
                            val subjectTitle = key.replace("_", " ").uppercase()
                            if (subjectVal is org.json.JSONArray) {
                                for (j in 0 until subjectVal.length()) {
                                    val note = subjectVal.get(j).toString()
                                    cards.add(
                                        RevisionCard(
                                            id = "rev_${key}_$j",
                                            subject = subjectTitle,
                                            title = "$subjectTitle Capsule #${j + 1}",
                                            points = listOf(note),
                                            formulaOrTrick = null,
                                            tag = "High Yield Notes"
                                        )
                                    )
                                }
                            } else if (subjectVal is JSONObject) {
                                val subKeys = subjectVal.keys()
                                while (subKeys.hasNext()) {
                                    val subKey = subKeys.next()
                                    val content = subjectVal.get(subKey).toString()
                                    cards.add(
                                        RevisionCard(
                                            id = "rev_${key}_$subKey",
                                            subject = subjectTitle,
                                            title = subKey.replace("_", " ").capitalize(),
                                            points = listOf(content),
                                            tag = "Key Rule"
                                        )
                                    )
                                }
                            }
                        }
                    }

                    if (root.has("boosters_archive")) {
                        val boostObj = root.getJSONObject("boosters_archive")
                        val keys = boostObj.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val subjectTitle = key.replace("_", " ").uppercase()
                            val boosterVal = boostObj.get(key)
                            if (boosterVal is org.json.JSONArray) {
                                for (j in 0 until boosterVal.length()) {
                                    val item = boosterVal.get(j).toString()
                                    cards.add(
                                        RevisionCard(
                                            id = "boost_${key}_$j",
                                            subject = subjectTitle,
                                            title = "$subjectTitle Formula / Booster #${j + 1}",
                                            points = listOf(item),
                                            formulaOrTrick = item,
                                            tag = "Booster Trick"
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // If empty, add standard high-yield CGL booster capsules
                    if (cards.isEmpty()) {
                        cards.addAll(createDefaultRevisionCards())
                    }
                    cachedRevisionCards = cards
                }
            }
            isLoaded = true
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback default mock if parsing encounters an edge case
            cachedDailyMocks = listOf(createDefaultMock())
            cachedRevisionCards = createDefaultRevisionCards()
            isLoaded = true
        }
    }

    private fun parseMock(obj: JSONObject, defaultPillar: String): MockExam {
        val qList = mutableListOf<Question>()
        if (obj.has("questions")) {
            val qArray = obj.getJSONArray("questions")
            for (j in 0 until qArray.length()) {
                val qObj = qArray.getJSONObject(j)
                qList.add(parseQuestion(qObj))
            }
        }
        return MockExam(
            mock_id = obj.optString("mock_id", "mock_${obj.optInt("mock_number", 1)}"),
            mock_number = obj.optInt("mock_number", 1),
            title = obj.optString("title", "SSC CGL 2026 Shift Mock"),
            date = obj.optString("date", "2026-09-12"),
            exam_pillar = obj.optString("exam_pillar", defaultPillar),
            total_questions = obj.optInt("total_questions", qList.size.coerceAtLeast(100)),
            max_marks = obj.optDouble("max_marks", 200.0),
            duration_minutes = obj.optInt("duration_minutes", 60),
            sectional_timing_minutes = if (obj.has("sectional_timing_minutes")) obj.getInt("sectional_timing_minutes") else null,
            questions = qList
        )
    }

    private fun parsePyq(obj: JSONObject): PYQPaper {
        val qList = mutableListOf<Question>()
        if (obj.has("questions")) {
            val qArray = obj.getJSONArray("questions")
            for (j in 0 until qArray.length()) {
                val qObj = qArray.getJSONObject(j)
                qList.add(parseQuestion(qObj))
            }
        }
        return PYQPaper(
            paper_id = obj.optString("paper_id", "pyq_${obj.optInt("year", 2024)}"),
            year = obj.optInt("year", 2024),
            title = obj.optString("title", "SSC CGL Tier 1 Official PYQ"),
            shift = obj.optString("shift", "Shift 1"),
            historical_cutoff = obj.optDouble("historical_cutoff", 145.5),
            total_questions = obj.optInt("total_questions", qList.size.coerceAtLeast(100)),
            max_marks = obj.optDouble("max_marks", 200.0),
            duration_minutes = obj.optInt("duration_minutes", 60),
            questions = qList
        )
    }

    private fun parseQuestion(qObj: JSONObject): Question {
        val opts = mutableListOf<String>()
        if (qObj.has("options")) {
            val optArr = qObj.getJSONArray("options")
            for (k in 0 until optArr.length()) {
                opts.add(optArr.getString(k))
            }
        }
        val optsHi = mutableListOf<String>()
        if (qObj.has("options_hi")) {
            val optHiArr = qObj.getJSONArray("options_hi")
            for (k in 0 until optHiArr.length()) {
                optsHi.add(optHiArr.getString(k))
            }
        }
        return Question(
            id = qObj.optInt("id", (1..99999).random()),
            section = qObj.optString("section", "Quantitative Aptitude"),
            stem = qObj.optString("stem", "Question text"),
            stem_hi = qObj.optString("stem_hi", null),
            options = opts,
            options_hi = if (optsHi.isNotEmpty()) optsHi else null,
            correct = qObj.optString("correct", opts.firstOrNull() ?: "A"),
            solution = qObj.optString("solution", null),
            solution_hi = qObj.optString("solution_hi", null),
            is_pyq = qObj.optBoolean("is_pyq", false)
        )
    }

    fun getDailyMocks(): List<MockExam> = cachedDailyMocks
    fun getPyqPapers(): List<PYQPaper> = cachedPyqPapers
    fun getBankingMocks(): List<MockExam> = cachedBankingMocks
    fun getRailwayMocks(): List<MockExam> = cachedRailwayMocks
    fun getPastMocks(): List<MockExam> = cachedPastMocks
    fun getRevisionCards(): List<RevisionCard> = cachedRevisionCards

    suspend fun saveExamResult(
        studentId: String,
        mockTitle: String,
        pillar: String,
        preset: String,
        score: Double,
        maxScore: Double,
        accuracy: Int,
        attempted: Int,
        correct: Int,
        wrong: Int,
        unattempted: Int,
        timeSpentSec: Long
    ) = withContext(Dispatchers.IO) {
        attemptDao.insertAttempt(
            ExamAttemptEntity(
                studentId = studentId,
                mockTitle = mockTitle,
                pillar = pillar,
                preset = preset,
                score = score,
                maxScore = maxScore,
                accuracy = accuracy,
                attemptedCount = attempted,
                correctCount = correct,
                wrongCount = wrong,
                unattemptedCount = unattempted,
                timeSpentSeconds = timeSpentSec
            )
        )
    }

    suspend fun logError(
        studentId: String,
        question: Question,
        userChoice: String,
        bucket: ErrorBucket = ErrorBucket.BUCKET_A
    ) = withContext(Dispatchers.IO) {
        val optionsJson = gson.toJson(question.options)
        val optionsHiJson = question.options_hi?.let { gson.toJson(it) }
        val now = System.currentTimeMillis()
        val nextReview = now + (3L * 24 * 60 * 60 * 1000) // Day 3 interval
        errorDiaryDao.insertError(
            ErrorDiaryEntity(
                qid = question.id,
                studentId = studentId,
                stem = question.stem,
                stemHi = question.stem_hi,
                section = question.section,
                optionsJson = optionsJson,
                optionsHiJson = optionsHiJson,
                correct = question.correct,
                userChoice = userChoice,
                solution = question.solution,
                solutionHi = question.solution_hi,
                bucket = bucket.code,
                timestamp = now,
                nextReviewDate = nextReview,
                reviewCycle = 1,
                isMastered = false
            )
        )
    }

    suspend fun updateErrorBucket(qid: Int, bucket: ErrorBucket) = withContext(Dispatchers.IO) {
        errorDiaryDao.updateBucket(qid, bucket.code)
    }

    suspend fun markErrorMastered(qid: Int, isMastered: Boolean) = withContext(Dispatchers.IO) {
        errorDiaryDao.setMastered(qid, isMastered)
    }

    suspend fun deleteError(qid: Int) = withContext(Dispatchers.IO) {
        errorDiaryDao.deleteError(qid)
    }

    suspend fun addBookmark(category: String, title: String, content: String) = withContext(Dispatchers.IO) {
        bookmarkDao.insertBookmark(
            BookmarkEntity(category = category, title = title, content = content)
        )
    }

    suspend fun removeBookmark(id: Long) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmark(id)
    }

    suspend fun switchStudent(studentId: String) = withContext(Dispatchers.IO) {
        studentDao.setActiveStudent(studentId)
    }

    suspend fun addStudent(name: String, email: String, targetExam: String) = withContext(Dispatchers.IO) {
        val newId = "student_${System.currentTimeMillis()}"
        val newStudent = StudentProfileEntity(
            id = newId,
            name = name,
            email = email,
            targetExam = targetExam,
            rollNumber = "CGL-2026-${(1000..9999).random()}",
            avatarColor = listOf(0xFFF59E0BL, 0xFF3B82F6L, 0xFF10B981L, 0xFF8B5CF6L, 0xFFEC4899L).random(),
            isActive = true
        )
        studentDao.insertStudent(newStudent)
        studentDao.setActiveStudent(newId)
    }

    private fun createDefaultMock(): MockExam {
        val dummyQuestions = listOf(
            Question(
                id = 101,
                section = "General Intelligence & Reasoning",
                stem = "In a row of students, Rohan is 12th from the left and Sahil is 18th from the right. When they interchange positions, Rohan becomes 25th from the left. How many students are in the row?",
                stem_hi = "विद्यार्थियों की एक पंक्ति में, रोहन बाएं से 12वें स्थान पर है और साहिल दाएं से 18वें स्थान पर है। जब वे आपस में स्थान बदलते हैं, तो रोहन बाएं से 25वां हो जाता है। पंक्ति में कितने विद्यार्थी हैं?",
                options = listOf("41", "42", "43", "44"),
                options_hi = listOf("41", "42", "43", "44"),
                correct = "42",
                solution = "Total students = New position of Rohan + Old position of Sahil - 1 = 25 + 18 - 1 = 42."
            ),
            Question(
                id = 102,
                section = "General Awareness",
                stem = "Under which Article of the Indian Constitution is the Financial Emergency declared?",
                stem_hi = "भारतीय संविधान के किस अनुच्छेद के तहत वित्तीय आपातकाल घोषित किया जाता है?",
                options = listOf("Article 352", "Article 356", "Article 360", "Article 368"),
                options_hi = listOf("अनुच्छेद 352", "अनुच्छेद 356", "अनुच्छेद 360", "अनुच्छेद 368"),
                correct = "Article 360",
                solution = "Article 360 empowers the President of India to proclaim a Financial Emergency if satisfaction exists that the financial stability or credit of India is threatened."
            ),
            Question(
                id = 103,
                section = "Quantitative Aptitude",
                stem = "If x + 1/x = 5, find the value of x^3 + 1/x^3.",
                stem_hi = "यदि x + 1/x = 5 है, तो x^3 + 1/x^3 का मान ज्ञात कीजिए।",
                options = listOf("110", "115", "125", "140"),
                options_hi = listOf("110", "115", "125", "140"),
                correct = "110",
                solution = "Formula: (x + 1/x)^3 - 3(x + 1/x) = 5^3 - 3(5) = 125 - 15 = 110."
            ),
            Question(
                id = 104,
                section = "English Comprehension",
                stem = "Select the most appropriate synonym of the given word: OSTRACIZE",
                stem_hi = "दिए गए शब्द का सबसे उपयुक्त पर्यायवाची चुनें: OSTRACIZE",
                options = listOf("Include", "Banish", "Applaud", "Patronize"),
                options_hi = listOf("Include", "Banish", "Applaud", "Patronize"),
                correct = "Banish",
                solution = "'Ostracize' means to exclude someone from a society or group. Synonym: Banish / Exclude."
            )
        )
        return MockExam(
            mock_id = "daily_mock_1",
            mock_number = 1,
            title = "SSC CGL 2026 Official Daily Simulator 01",
            date = "2026-09-12",
            exam_pillar = "ssc",
            total_questions = dummyQuestions.size,
            max_marks = 200.0,
            duration_minutes = 60,
            questions = dummyQuestions
        )
    }

    private fun createDefaultRevisionCards(): List<RevisionCard> {
        return listOf(
            RevisionCard(
                id = "rev_alg_1",
                subject = "QUANTITATIVE APTITUDE",
                title = "Algebra Identities & Symmetry",
                points = listOf(
                    "If x + 1/x = k, then x² + 1/x² = k² - 2",
                    "x³ + 1/x³ = k³ - 3k",
                    "x⁴ + 1/x⁴ = (k² - 2)² - 2",
                    "If x + y + z = 0, then x³ + y³ + z³ = 3xyz"
                ),
                formulaOrTrick = "x³ + 1/x³ = k³ - 3k",
                tag = "Algebra"
            ),
            RevisionCard(
                id = "rev_polity_1",
                subject = "INDIAN POLITY",
                title = "Emergency Provisions (Part XVIII)",
                points = listOf(
                    "Article 352: National Emergency (War, External Aggression, Armed Rebellion)",
                    "Article 356: President's Rule (Failure of constitutional machinery in states)",
                    "Article 360: Financial Emergency (Never imposed in India so far)"
                ),
                tag = "Articles"
            ),
            RevisionCard(
                id = "rev_vocab_1",
                subject = "ENGLISH COMPREHENSION",
                title = "High-Frequency 1-Word Substitutions",
                points = listOf(
                    "One who is indifferent to pleasure and pain: STOIC",
                    "A person who loves books: BIBLIOPHILE",
                    "One who compiles dictionaries: LEXICOGRAPHER",
                    "A remedy for all diseases: PANACEA"
                ),
                tag = "Vocabulary"
            ),
            RevisionCard(
                id = "rev_geom_1",
                subject = "QUANTITATIVE APTITUDE",
                title = "Geometry Incenter & Circumcenter",
                points = listOf(
                    "Incenter angle: ∠BIC = 90° + ∠A/2",
                    "Circumcenter angle: ∠BOC = 2∠A",
                    "Orthocenter angle: ∠BHC = 180° - ∠A",
                    "Centroid divides median in 2:1 ratio"
                ),
                formulaOrTrick = "∠BIC = 90° + ∠A/2",
                tag = "Geometry"
            ),
            RevisionCard(
                id = "rev_reasoning_1",
                subject = "GENERAL INTELLIGENCE & REASONING",
                title = "Clock & Calendar Shortcuts",
                points = listOf(
                    "Angle between hour & minute hands: θ = |30H - (11/2)M|",
                    "Mirror image of clock: Subtract given time from 11:60",
                    "Water image of clock: Subtract from 18:30 or 17:90",
                    "Leap year repeats after 28 years; Ordinary year repeats after 6 or 11 years"
                ),
                formulaOrTrick = "θ = |30H - (11/2)M|",
                tag = "Shortcuts"
            )
        )
    }
}
