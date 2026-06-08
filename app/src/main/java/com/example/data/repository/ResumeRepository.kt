package com.example.data.repository

import com.example.data.database.ResumeDao
import com.example.data.database.UserDao
import com.example.data.model.Resume
import com.example.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class ResumeRepository(
    private val userDao: UserDao,
    private val resumeDao: ResumeDao
) {
    // --- User Auth Operations ---
    
    suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        userDao.getUserByEmail(email)
    }

    suspend fun getUserById(id: Int): User? = withContext(Dispatchers.IO) {
        userDao.getUserById(id)
    }

    suspend fun getUserCount(): Int = withContext(Dispatchers.IO) {
        userDao.getUserCount()
    }

    suspend fun registerUser(username: String, email: String, passwordRaw: String): Long = withContext(Dispatchers.IO) {
        val hash = hashPassword(passwordRaw)
        val user = User(username = username, email = email, passwordHash = hash)
        userDao.registerUser(user)
    }

    suspend fun authenticateUser(email: String, passwordRaw: String): User? = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email) ?: return@withContext null
        val expectedHash = hashPassword(passwordRaw)
        if (user.passwordHash == expectedHash) user else null
    }

    // Direct SHA-256 Helper for lightweight hashing
    private fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { String.format("%02x", it) }
        } catch (e: Exception) {
            password // Avoid crashing; fallback to string comparison if error
        }
    }

    // --- Resume Operations ---

    fun getResumesForUser(userId: Int): Flow<List<Resume>> = resumeDao.getResumesForUser(userId)

    suspend fun getResumeById(id: Int): Resume? = withContext(Dispatchers.IO) {
        resumeDao.getResumeById(id)
    }

    suspend fun insertResume(resume: Resume): Long = withContext(Dispatchers.IO) {
        resumeDao.insertResume(resume)
    }

    suspend fun updateResume(resume: Resume) = withContext(Dispatchers.IO) {
        resumeDao.updateResume(resume)
    }

    suspend fun deleteResume(resume: Resume) = withContext(Dispatchers.IO) {
        resumeDao.deleteResume(resume)
    }

    suspend fun duplicateResume(resumeId: Int): Long = withContext(Dispatchers.IO) {
        val source = resumeDao.getResumeById(resumeId) ?: return@withContext -1L
        val duplicate = source.copy(
            id = 0, // Generate new primary key
            title = "${source.title} (Copy)",
            createdAt = System.currentTimeMillis()
        )
        resumeDao.insertResume(duplicate)
    }
}
