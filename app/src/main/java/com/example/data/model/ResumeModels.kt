package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@JsonClass(generateAdapter = true)
data class Education(
    val school: String = "",
    val degree: String = "",
    val duration: String = "",
    val gpa: String = ""
)

@JsonClass(generateAdapter = true)
data class Experience(
    val company: String = "",
    val role: String = "",
    val duration: String = "",
    val description: String = ""
)

@JsonClass(generateAdapter = true)
data class Project(
    val title: String = "",
    val techStack: String = "",
    val description: String = ""
)

@JsonClass(generateAdapter = true)
data class Skill(
    val name: String = "",
    val percentage: Int = 80,
    val category: String = "General" // e.g. Technical, Soft, Language
)

@JsonClass(generateAdapter = true)
data class Reference(
    val name: String = "",
    val company: String = "",
    val contact: String = ""
)

@JsonClass(generateAdapter = true)
data class CustomSection(
    val sectionTitle: String = "",
    val content: String = ""
)

@Entity(tableName = "resumes")
data class Resume(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int = 0,
    val title: String = "My Professional Resume",
    val fullname: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val summary: String = "",
    val objective: String = "",
    val referencesJson: String = "[]",
    val educationJson: String = "[]",
    val skillsJson: String = "[]",
    val projectsJson: String = "[]",
    val experienceJson: String = "[]",
    val certificationsJson: String = "[]",
    val achievementsJson: String = "[]",
    val languagesJson: String = "[]",
    val hobbiesJson: String = "[]",
    val customSectionsJson: String = "[]",
    
    // Customization & Styles
    val photo: String? = null, // URI string
    val photoStyle: String = "Circular", // Circular, Square, Rounded
    val photoSize: Int = 100, // dp size
    val themeColor: String = "#1E3A8A", // Predefined/custom color pickerhex
    val fontFamily: String = "Default", // 15+ families (Sans, Serif, Monospace, etc.)
    val fontSize: Int = 14, // Base size
    val templateId: Int = 0, // 0 to 24 (25 unique templates)
    val layoutStyle: String = "Standard", // Standard, Sidebar
    val iconStyle: String = "Filled", // Filled, Outlined, Colored
    
    // Visibility Preferences & Ordering
    val sectionVisibilityJson: String = "{}", // JSON map of visibility
    val sectionOrderJson: String = "[]", // Custom Ordering
    val isDraft: Boolean = true,
    val downloadHistoryCount: Int = 0,
    val score: Int = 0, // Score dynamically checker
    val createdAt: Long = System.currentTimeMillis()
)

object ResumeMoshiHelper {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    
    fun <T> toJson(list: List<T>, clazz: Class<T>): String {
        val type = Types.newParameterizedType(List::class.java, clazz)
        val adapter = moshi.adapter<List<T>>(type)
        return adapter.toJson(list)
    }

    fun <T> fromJson(json: String, clazz: Class<T>): List<T> {
        return try {
            val type = Types.newParameterizedType(List::class.java, clazz)
            val adapter = moshi.adapter<List<T>>(type)
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
