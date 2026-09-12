package com.example.cglprep.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY isActive DESC, name ASC")
    fun getAllStudents(): Flow<List<StudentProfileEntity>>

    @Query("SELECT * FROM students WHERE isActive = 1 LIMIT 1")
    fun getActiveStudent(): Flow<StudentProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentProfileEntity)

    @Query("UPDATE students SET isActive = CASE WHEN id = :studentId THEN 1 ELSE 0 END")
    suspend fun setActiveStudent(studentId: String)
}

@Dao
interface ErrorDiaryDao {
    @Query("SELECT * FROM error_diary ORDER BY timestamp DESC")
    fun getAllErrors(): Flow<List<ErrorDiaryEntity>>

    @Query("SELECT * FROM error_diary WHERE bucket = :bucket ORDER BY timestamp DESC")
    fun getErrorsByBucket(bucket: String): Flow<List<ErrorDiaryEntity>>

    @Query("SELECT * FROM error_diary WHERE isMastered = 0 AND (nextReviewDate <= :currentTime OR nextReviewDate IS NULL) ORDER BY timestamp DESC")
    fun getSpacedDueErrors(currentTime: Long): Flow<List<ErrorDiaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertError(error: ErrorDiaryEntity)

    @Update
    suspend fun updateError(error: ErrorDiaryEntity)

    @Query("UPDATE error_diary SET isMastered = :isMastered WHERE qid = :qid")
    suspend fun setMastered(qid: Int, isMastered: Boolean)

    @Query("UPDATE error_diary SET bucket = :newBucket WHERE qid = :qid")
    suspend fun updateBucket(qid: Int, newBucket: String)

    @Query("DELETE FROM error_diary WHERE qid = :qid")
    suspend fun deleteError(qid: Int)
}

@Dao
interface ExamAttemptDao {
    @Query("SELECT * FROM exam_attempts ORDER BY timestamp DESC")
    fun getAllAttempts(): Flow<List<ExamAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: ExamAttemptEntity)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: Long)
}
