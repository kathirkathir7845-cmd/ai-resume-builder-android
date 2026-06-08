package com.example.network

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini Request / Response Models ---

@JsonClass(generateAdapter = true)
data class Part(val text: String)

@JsonClass(generateAdapter = true)
data class Content(val parts: List<Part>)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>
)

@JsonClass(generateAdapter = true)
data class Candidate(val content: Content)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun generateText(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key is missing from Secrets! Please configure GEMINI_API_KEY in the Secrets panel to activate AI suggestions."
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )

        try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "No suggestions generated. Please try again with different inputs."
        } catch (e: Exception) {
            "AI Assistant unavailable: ${e.localizedMessage}. Please verify your credentials and network connection."
        }
    }

    // --- Specialized AI Functions ---

    suspend fun generateProfileSummary(role: String, experienceYears: String, highlights: String): String {
        val prompt = """
            You are an expert executive CV writer. Create a high-quality, professional, recruiter-grade 3-4 sentence profile summary.
            Job Category / Role: $role
            Years of Experience: $experienceYears
            Key Experience Milestones / Highlights: $highlights
            
            Format: Return just the professional summary paragraph itself. Do not include any tags, headers, introductory phrases, or markdown styling.
        """.trimIndent()
        return generateText(prompt)
    }

    suspend fun suggestSkills(role: String, category: String): String {
        val prompt = """
            Provide a list of 10 relevant high-demand $category skills for a: $role state.
            Format the output strictly as a comma-separated list of individual terms, e.g. "Kotlin, Jetpack Compose, MVVM Architecture, Room DB".
            Return ONLY the comma-separated terms. Do not include numbered items, bullet points, introductory chat, or markdown blocks.
        """.trimIndent()
        return generateText(prompt)
    }

    suspend fun generateProjectDescription(title: String, techStack: String): String {
        val prompt = """
            Write a detailed, action-verb and results-focused bulleted project description.
            Project Title: $title
            Tech Stack Used: $techStack
            
            Provide exactly 3 bullet points starting with strong action verbs (e.g. "Developed...", "Optimized...", "Engineered..."). Do not wrap with markdown code fences. Respond only with the bullet points.
        """.trimIndent()
        return generateText(prompt)
    }

    suspend fun generateJobDescription(role: String, company: String): String {
        val prompt = """
            Write a detailed, action-verb and results-focused bulleted work experience / job description.
            Job Title / Role: $role
            ${if (company.isNotBlank()) "Company: $company" else ""}
            
            Provide exactly 3 highly professional bullet points starting with strong action verbs (e.g. "Developed...", "Led...", "Optimized...", "Architected..."). Do not wrap with markdown code fences. Respond only with the bullet points.
        """.trimIndent()
        return generateText(prompt)
    }

    suspend fun suggestImprovements(fullname: String, summary: String, skills: String, experiences: String): String {
        val prompt = """
            Analyze the following resume details for professional polishing:
            Name: $fullname
            Current Summary: $summary
            Skills: $skills
            Work Experience Logs: $experiences
            
            Provide 3-4 highly actionable and specific improvements to make this look premium, ATS-compliant, and impactful for recruiters. Keep the suggestions short and bulleted.
        """.trimIndent()
        return generateText(prompt)
    }

    suspend fun analyzeAtsScore(targetRole: String, resumeText: String): String {
        val prompt = """
            You are an advanced Applicant Tracking System (ATS) Parser and Career Coach. Check the candidate's resume against the target role:
            Target Role: $targetRole
            Full Resume Content:
            $resumeText
            
            Analyze the compatibility and respond strictly in this precise cataloged format:
            SCORE: [a single integer between 0 and 100 representing compatibility]
            MATCHES: [3 keywords found that match well, comma separated]
            MISSING: [3 keywords/requirements commonly expected for '$targetRole' that are missing, comma separated]
            CHECKLIST: [exactly 3 bulleted actionable steps to improve the score to pass a recruiter filter]
            
            Ensure you follow this exact format with lines beginning with the labels (SCORE, MATCHES, MISSING, CHECKLIST). Do not add any conversational introductions.
        """.trimIndent()
        return generateText(prompt)
    }
}
