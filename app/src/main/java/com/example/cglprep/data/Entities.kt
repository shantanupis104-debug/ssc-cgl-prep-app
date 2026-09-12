package com.example.cglprep.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val targetExam: String = "SSC CGL 2026",
    val rollNumber: String = "CGL-2026-9081",
    val avatarColor: Long = 0xFFF59E0BL,
    val isActive: Boolean = true
)

@Entity(tableName = "error_diary")
data class ErrorDiaryEntity(
    @PrimaryKey val qid: Int,
    val studentId: String,
    val stem: String,
    val stemHi: String? = null,
    val section: String,
    val optionsJson: String,
    val optionsHiJson: String? = null,
    val correct: String,
    val userChoice: String,
    val solution: String? = null,
    val solutionHi: String? = null,
    val bucket: String = "BUCKET_A",
    val timestamp: Long = System.currentTimeMillis(),
    val nextReviewDate: Long = System.currentTimeMillis() + (3L * 24 * 60 * 60 * 1000),
    val reviewCycle: Int = 1,
    val isMastered: Boolean = false
)

@Entity(tableName = "exam_attempts")
data class ExamAttemptEntity(
    @PrimaryKey(autoGenerate = true) val attemptId: Long = 0,
    val studentId: String,
    val mockTitle: String,
    val pillar: String,
    val preset: String,
    val score: Double,
    val maxScore: Double,
    val accuracy: Int,
    val attemptedCount: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unattemptedCount: Int,
    val timeSpentSeconds: Long,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
