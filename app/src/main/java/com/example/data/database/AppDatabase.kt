package com.example.data.database

import android.content.Context
import androidx.room.*
import com.example.data.model.Resume
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registerUser(user: User): Long
}

@Dao
interface ResumeDao {
    @Query("SELECT * FROM resumes WHERE userId = :userId ORDER BY createdAt DESC")
    fun getResumesForUser(userId: Int): Flow<List<Resume>>

    @Query("SELECT * FROM resumes WHERE id = :id LIMIT 1")
    suspend fun getResumeById(id: Int): Resume?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResume(resume: Resume): Long

    @Update
    suspend fun updateResume(resume: Resume)

    @Delete
    suspend fun deleteResume(resume: Resume)
}

@Database(entities = [User::class, Resume::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun resumeDao(): ResumeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "resume_builder_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
