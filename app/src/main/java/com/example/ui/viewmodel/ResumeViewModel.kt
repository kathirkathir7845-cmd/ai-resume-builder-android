package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.ResumeRepository
import com.example.network.GeminiApiClient
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class ResumeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ResumeRepository(db.userDao(), db.resumeDao())

    // --- Authentication States ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authSuccess = MutableStateFlow(false)
    val authSuccess: StateFlow<Boolean> = _authSuccess.asStateFlow()

    private val _sessionChecked = MutableStateFlow(false)
    val sessionChecked: StateFlow<Boolean> = _sessionChecked.asStateFlow()

    private val _hasRegisteredUsers = MutableStateFlow(false)
    val hasRegisteredUsers: StateFlow<Boolean> = _hasRegisteredUsers.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                // Check if any users have already registered on this database
                val count = repository.getUserCount()
                _hasRegisteredUsers.value = count > 0

                val sharedPrefs = application.getSharedPreferences("user_session", Context.MODE_PRIVATE)
                val savedUserId = sharedPrefs.getInt("logged_in_user_id", -1)
                if (savedUserId != -1) {
                    val user = repository.getUserById(savedUserId)
                    if (user != null) {
                        _currentUser.value = user
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _sessionChecked.value = true
            }
        }
    }

    // --- Resume Lists (Reactive based on User Session) ---
    val userResumes: StateFlow<List<Resume>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getResumesForUser(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Selected Resume for Edit & Real-Time Preview ---
    private val _currentResume = MutableStateFlow<Resume?>(null)
    val currentResume: StateFlow<Resume?> = _currentResume.asStateFlow()

    // --- AI Operations Loading states ---
    private val _aiResult = MutableStateFlow<String?>(null)
    val aiResult: StateFlow<String?> = _aiResult.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _atsScoreResult = MutableStateFlow<AtsAnalysis?>(null)
    val atsScoreResult: StateFlow<AtsAnalysis?> = _atsScoreResult.asStateFlow()

    // Auto-Save Coroutine logic
    private var autoSaveJob: Job? = null

    // --- Authentication Actions ---

    fun login(email: String, passwordRaw: String) {
        viewModelScope.launch {
            _authError.value = null
            if (email.isBlank() || passwordRaw.isBlank()) {
                _authError.value = "All fields are required."
                return@launch
            }
            val user = repository.authenticateUser(email, passwordRaw)
            if (user != null) {
                // Save user ID to SharedPreferences for persistent session recovery
                val sharedPrefs = getApplication<Application>().getSharedPreferences("user_session", Context.MODE_PRIVATE)
                sharedPrefs.edit().putInt("logged_in_user_id", user.id).apply()
                _hasRegisteredUsers.value = true
                _currentUser.value = user
                _authSuccess.value = true
            } else {
                _authError.value = "Invalid email or password."
            }
        }
    }

    fun register(username: String, email: String, passwordRaw: String) {
        viewModelScope.launch {
            _authError.value = null
            if (username.isBlank() || email.isBlank() || passwordRaw.isBlank()) {
                _authError.value = "All fields are required."
                return@launch
            }
            try {
                val existing = repository.getUserByEmail(email)
                if (existing != null) {
                    _authError.value = "An account with this email already exists."
                    return@launch
                }
                val userId = repository.registerUser(username, email, passwordRaw)
                val newUser = repository.getUserById(userId.toInt())
                if (newUser != null) {
                    // Save user ID to SharedPreferences for persistent session recovery
                    val sharedPrefs = getApplication<Application>().getSharedPreferences("user_session", Context.MODE_PRIVATE)
                    sharedPrefs.edit().putInt("logged_in_user_id", newUser.id).apply()
                    _hasRegisteredUsers.value = true
                    _currentUser.value = newUser
                    _authSuccess.value = true
                } else {
                    _authError.value = "Registration error: user registration data unavailable."
                }
            } catch (e: Exception) {
                _authError.value = "Registration failed: ${e.localizedMessage}"
            }
        }
    }

    fun logout() {
        // Clear user session from SharedPreferences
        val sharedPrefs = getApplication<Application>().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        sharedPrefs.edit().remove("logged_in_user_id").apply()

        _currentUser.value = null
        _currentResume.value = null
        _authSuccess.value = false
        _authError.value = null
        _atsScoreResult.value = null
        _aiResult.value = null
    }

    fun clearAuthSuccessFlag() {
        _authSuccess.value = false
    }

    // --- Resume Management Actions ---

    fun createNewResume() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val defaultResume = Resume(
                userId = user.id,
                title = "My Resume ${userResumes.value.size + 1}",
                fullname = user.username,
                email = user.email,
                sectionVisibilityJson = """{
                    "Summary": true,
                    "Education": true,
                    "Skills": true,
                    "Projects": true,
                    "Experience": true,
                    "Certifications": true,
                    "Achievements": true,
                    "Languages": true,
                    "Hobbies": true,
                    "References": true
                }""".trimIndent(),
                sectionOrderJson = """["Summary", "Experience", "Projects", "Skills", "Education", "Certifications", "Achievements", "Languages", "Hobbies", "References"]"""
            )
            val insertId = repository.insertResume(defaultResume)
            val created = defaultResume.copy(id = insertId.toInt())
            _currentResume.value = created
        }
    }

    fun loadResume(resumeId: Int) {
        viewModelScope.launch {
            _currentResume.value = repository.getResumeById(resumeId)
            _atsScoreResult.value = null
            _aiResult.value = null
        }
    }

    fun updateCurrentResumeState(updated: Resume) {
        _currentResume.value = updated
        // Recalculate local scoring in real-time
        val score = calculateScore(updated)
        val finalUpdated = updated.copy(score = score)
        _currentResume.value = finalUpdated
        
        // Trigger auto-save debounce
        triggerAutoSave(finalUpdated)
    }

    private fun triggerAutoSave(resume: Resume) {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1500) // 1.5 seconds auto-save debounce as requested!
            repository.updateResume(resume)
        }
    }

    fun duplicateResume(resumeId: Int) {
        viewModelScope.launch {
            val newId = repository.duplicateResume(resumeId)
            if (newId != -1L) {
                // Optionally load the duplicate
                _currentResume.value = repository.getResumeById(newId.toInt())
            }
        }
    }

    fun deleteResume(resume: Resume) {
        viewModelScope.launch {
            autoSaveJob?.cancel()
            repository.deleteResume(resume)
            if (_currentResume.value?.id == resume.id) {
                _currentResume.value = null
            }
        }
    }

    // --- Scoring Engine ---
    fun calculateScore(resume: Resume): Int {
        var score = 0
        if (resume.fullname.isNotBlank()) score += 15
        if (resume.email.isNotBlank() && resume.phone.isNotBlank()) score += 15
        if (resume.address.isNotBlank()) score += 10
        if (resume.summary.isNotBlank() || resume.objective.isNotBlank()) score += 15
        
        val eduCount = ResumeMoshiHelper.fromJson(resume.educationJson, Education::class.java).size
        if (eduCount > 0) score += 15
        
        val expCount = ResumeMoshiHelper.fromJson(resume.experienceJson, Experience::class.java).size
        if (expCount > 0) score += 15
        
        val projCount = ResumeMoshiHelper.fromJson(resume.projectsJson, Project::class.java).size
        if (projCount > 0) score += 10
        
        val skillsCount = ResumeMoshiHelper.fromJson(resume.skillsJson, Skill::class.java).size
        if (skillsCount > 2) score += 5

        return score.coerceIn(0, 100)
    }

    // --- AI Operations Trigger (Calling Direct REST API) ---

    fun aiGenerateProfileSummary(role: String, experienceYears: String, highlights: String, onCompleted: (String) -> Unit) {
        viewModelScope.launch {
            _aiLoading.value = true
            _aiResult.value = null
            val result = GeminiApiClient.generateProfileSummary(role, experienceYears, highlights)
            _aiResult.value = result
            _aiLoading.value = false
            onCompleted(result)
        }
    }

    fun aiSuggestSkills(role: String, category: String, onCompleted: (String) -> Unit) {
        viewModelScope.launch {
            _aiLoading.value = true
            _aiResult.value = null
            val result = GeminiApiClient.suggestSkills(role, category)
            _aiResult.value = result
            _aiLoading.value = false
            onCompleted(result)
        }
    }

    fun aiSuggestProjectDescription(title: String, techStack: String, onCompleted: (String) -> Unit) {
        viewModelScope.launch {
            _aiLoading.value = true
            _aiResult.value = null
            val result = GeminiApiClient.generateProjectDescription(title, techStack)
            _aiResult.value = result
            _aiLoading.value = false
            onCompleted(result)
        }
    }

    fun aiGenerateJobDescription(role: String, company: String, onCompleted: (String) -> Unit) {
        viewModelScope.launch {
            _aiLoading.value = true
            _aiResult.value = null
            val result = GeminiApiClient.generateJobDescription(role, company)
            _aiResult.value = result
            _aiLoading.value = false
            onCompleted(result)
        }
    }

    fun aiAnalyzeImprovement(fullname: String, summary: String, skills: String, experiences: String) {
        viewModelScope.launch {
            _aiLoading.value = true
            _aiResult.value = null
            val result = GeminiApiClient.suggestImprovements(fullname, summary, skills, experiences)
            _aiResult.value = result
            _aiLoading.value = false
        }
    }

    fun aiAnalyzeAtsCompatibility(targetRole: String, resumeText: String) {
        viewModelScope.launch {
            _aiLoading.value = true
            _atsScoreResult.value = null
            val rawResult = GeminiApiClient.analyzeAtsScore(targetRole, resumeText)
            
            // Parse custom structured output from Gemini format:
            val parsed = parseAtsResponse(rawResult)
            _atsScoreResult.value = parsed
            _aiLoading.value = false
        }
    }

    private fun parseAtsResponse(raw: String): AtsAnalysis {
        var score = 50
        var matches = emptyList<String>()
        var missing = emptyList<String>()
        var checklist = emptyList<String>()

        try {
            val lines = raw.split("\n")
            var checklistSec = false
            val checklistLines = mutableListOf<String>()
            
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("SCORE:", ignoreCase = true)) {
                    val num = trimmed.substring(6).trim().filter { it.isDigit() }
                    if (num.isNotEmpty()) score = num.toInt().coerceIn(0, 100)
                } else if (trimmed.startsWith("MATCHES:", ignoreCase = true)) {
                    matches = trimmed.substring(8).trim().split(",").map { it.trim() }.filter { it.isNotEmpty() }
                } else if (trimmed.startsWith("MISSING:", ignoreCase = true)) {
                    missing = trimmed.substring(8).trim().split(",").map { it.trim() }.filter { it.isNotEmpty() }
                } else if (trimmed.startsWith("CHECKLIST:", ignoreCase = true)) {
                    checklistSec = true
                    val content = trimmed.substring(10).trim()
                    if (content.isNotEmpty()) {
                        checklistLines.add(content)
                    }
                } else if (checklistSec) {
                    if (trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.isNotBlank()) {
                        checklistLines.add(trimmed.trimStart('-', '*', ' '))
                    }
                }
            }
            checklist = if (checklistLines.isNotEmpty()) checklistLines else listOf("Enhance keywords targeting", "Add project context", "Include academic certifications")
        } catch (e: Exception) {
            // fallback
            score = 65
            matches = listOf("Professional Experience", "Education Background")
            missing = listOf("Specific ATS keywords", "Target Job metrics")
            checklist = listOf("Integrate more keywords", "Format experiences with strong action verbs", "Re-evaluate title margins for parsing")
        }

        return AtsAnalysis(score = score, matchingKeywords = matches, missingKeywords = missing, improvementsChecklist = checklist)
    }
}

data class AtsAnalysis(
    val score: Int = 0,
    val matchingKeywords: List<String> = emptyList(),
    val missingKeywords: List<String> = emptyList(),
    val improvementsChecklist: List<String> = emptyList()
)
