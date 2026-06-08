package com.example.ui.templates

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*

// --- 16 Font Families Preset Mapping ---
val ResumeFonts = listOf(
    "Default" to FontFamily.Default,
    "Sans-Serif" to FontFamily.SansSerif,
    "Serif" to FontFamily.Serif,
    "Monospace" to FontFamily.Monospace,
    "Rounded" to FontFamily.SansSerif, // Compose fallback
    "Elegant Script" to FontFamily.Cursive,
    "Futuristic" to FontFamily.SansSerif,
    "Classic Georgia" to FontFamily.Serif,
    "Technical Console" to FontFamily.Monospace,
    "Playful Headline" to FontFamily.Default,
    "Sleek Gothic" to FontFamily.SansSerif,
    "Book Antiqua" to FontFamily.Serif,
    "Courier Clean" to FontFamily.Monospace,
    "Corporate Narrow" to FontFamily.SansSerif,
    "Times Regency" to FontFamily.Serif,
    "Impact Modern" to FontFamily.SansSerif
)

fun getFontFamily(name: String): FontFamily {
    return ResumeFonts.firstOrNull { it.first == name }?.second ?: FontFamily.Default
}

// --- 50 Predefined Color Presets ---
data class ColorTheme(val name: String, val primary: Color, val secondary: Color, val accent: Color)

val ResumeColorThemes = listOf(
    ColorTheme("Royal Navy", Color(0xFF1E3A8A), Color(0xFF3B82F6), Color(0xFFDBEAFE)),
    ColorTheme("Deep Onyx", Color(0xFF1F2937), Color(0xFF4B5563), Color(0xFFF3F4F6)),
    ColorTheme("Emerald Luxe", Color(0xFF064E3B), Color(0xFF10B981), Color(0xFFD1FAE5)),
    ColorTheme("Corporate Wine", Color(0xFF7F1D1D), Color(0xFFEF4444), Color(0xFFFEE2E2)),
    ColorTheme("Midnight Slate", Color(0xFF0F172A), Color(0xFF64748B), Color(0xFFE2E8F0)),
    ColorTheme("Ocean Breeze", Color(0xFF0891B2), Color(0xFF06B6D4), Color(0xFFECFEFF)),
    ColorTheme("Sunset Clay", Color(0xFF7C2D12), Color(0xFFF97316), Color(0xFFFFEDD5)),
    ColorTheme("Forest Mist", Color(0xFF14532D), Color(0xFF22C55E), Color(0xFFDCFCE7)),
    ColorTheme("Royal Purple", Color(0xFF581C87), Color(0xFFA855F7), Color(0xFFF3E8FF)),
    ColorTheme("Steel Tech", Color(0xFF374151), Color(0xFF9CA3AF), Color(0xFFF9FAFB)),
    ColorTheme("Teal Synergy", Color(0xFF134E5E), Color(0xFF71B280), Color(0xFFE0F2F1)),
    ColorTheme("Cosmic Slate", Color(0xFF2E3440), Color(0xFF88C0D0), Color(0xFFE5E9F0)),
    ColorTheme("Warm Copper", Color(0xFF8B4513), Color(0xFFCD853F), Color(0xFFFFE4B5)),
    ColorTheme("Bordeaux Elite", Color(0xFF4C0519), Color(0xFF9F1239), Color(0xFFFFE4E6)),
    ColorTheme("Cyber Neon", Color(0xFF0D0D11), Color(0xFF00FFFF), Color(0xFF00FF00)),
    ColorTheme("Golden Amber", Color(0xFF78350F), Color(0xFFF59E0B), Color(0xFFFEF3C7)),
    ColorTheme("Nordic Ice", Color(0xFF2B4C7E), Color(0xFF5A7FA8), Color(0xFFE8F1F5)),
    ColorTheme("Plum Executive", Color(0xFF4A0E4E), Color(0xFF8B2B9E), Color(0xFFFADBFB)),
    ColorTheme("Asphalt Clean", Color(0xFF2D3748), Color(0xFF718096), Color(0xFFEDF2F7)),
    ColorTheme("Spruce Forest", Color(0xFF064E40), Color(0xFF0E7A63), Color(0xFFE6F4F1)),
    ColorTheme("Classic Slate", Color(0xFF333333), Color(0xFF666666), Color(0xFFEEEEEE)),
    ColorTheme("Coffee Roast", Color(0xFF3E2723), Color(0xFF8D6E63), Color(0xFFEFEBE9)),
    ColorTheme("Military Olive", Color(0xFF33691E), Color(0xFF689F38), Color(0xFFF1F8E9)),
    ColorTheme("Electric Indigo", Color(0xFF1A237E), Color(0xFF3F51B5), Color(0xFFE8EAF6)),
    ColorTheme("Saffron Sunset", Color(0xFFE65100), Color(0xFFFFA726), Color(0xFFFFF3E0)),
    ColorTheme("Rose Luxury", Color(0xFF880E4F), Color(0xFFD81B60), Color(0xFFFCE4EC)),
    ColorTheme("Lagoon Teal", Color(0xFF004D40), Color(0xFF009688), Color(0xFFE0F2F1)),
    ColorTheme("Monochrome Dark", Color(0xFF111111), Color(0xFF444444), Color(0xFFCCCCCC)),
    ColorTheme("Charcoal Modern", Color(0xFF263238), Color(0xFF546E7A), Color(0xFFECEFF1)),
    ColorTheme("Gold Crest", Color(0xFF002244), Color(0xFFD4AF37), Color(0xFFEDF2F4)),
    ColorTheme("Vintage Sepia", Color(0xFF5D4037), Color(0xFF8D6E63), Color(0xFFD7CCC8)),
    ColorTheme("Crimson Royal", Color(0xFFB71C1C), Color(0xFFE53935), Color(0xFFFFEBEE)),
    ColorTheme("Warm Cocoa", Color(0xFF4E342E), Color(0xFF795548), Color(0xFFD7CCC8)),
    ColorTheme("Cool Breeze", Color(0xFF1565C0), Color(0xFF42A5F5), Color(0xFFE3F2FD)),
    ColorTheme("Citrus Fusion", Color(0xFF33691E), Color(0xFFFF8F00), Color(0xFFFFF8E1)),
    ColorTheme("Amethyst Glow", Color(0xFF4A148C), Color(0xFF7B1FA2), Color(0xFFF3E8FF)),
    ColorTheme("Pacific Cruise", Color(0xFF006064), Color(0xFF00ACC1), Color(0xFFE0F7FA)),
    ColorTheme("Cherry Noir", Color(0xFF5D121F), Color(0xFFCD5C5C), Color(0xFFFFF0F5)),
    ColorTheme("Safari Tan", Color(0xFF3E2723), Color(0xFFBCAAA4), Color(0xFFEFEBE9)),
    ColorTheme("Nordic Gray", Color(0xFF37474F), Color(0xFFB0BEC5), Color(0xFFECEFF1)),
    ColorTheme("Pink Gold", Color(0xFF4F001D), Color(0xFFD5A6BD), Color(0xFFFFF0F2)),
    ColorTheme("Mint Sage", Color(0xFF1B5E20), Color(0xFF81C784), Color(0xFFE8F5E9)),
    ColorTheme("Cobalt Shield", Color(0xFF0D47A1), Color(0xFF29B6F6), Color(0xFFE1F5FE)),
    ColorTheme("Canyon Clay", Color(0xFFBF360C), Color(0xFFFF7043), Color(0xFFFBE9E7)),
    ColorTheme("Classic Executive", Color(0xFF212121), Color(0xFF757575), Color(0xFFF5F5F5)),
    ColorTheme("Sunset Ocean", Color(0xFF1A365D), Color(0xFFDD6B20), Color(0xFFEBF8FF)),
    ColorTheme("Velvet Plum", Color(0xFF311B92), Color(0xFFCE93D8), Color(0xFFF3E5F5)),
    ColorTheme("Grasslands", Color(0xFF1B5E20), Color(0xFF9E9D24), Color(0xFFF1F8E9)),
    ColorTheme("Smoky Quartz", Color(0xFF212529), Color(0xFFADB5BD), Color(0xFFF8F9FA)),
    ColorTheme("Brave Orange", Color(0xFFE65100), Color(0xFFBF360C), Color(0xFFFFE0B2)),
    ColorTheme("Alpine White", Color(0xFF455A64), Color(0xFF78909C), Color(0xFFECEFF1)),
    ColorTheme("High Contrast", Color(0xFF000000), Color(0xFF333333), Color(0xFFFFFFFF))
)

// --- Preset Icon Styling Mapping ---
fun getIconForSection(section: String): ImageVector {
    return when (section) {
        "Summary", "Objective" -> Icons.Default.Description
        "Education" -> Icons.Default.School
        "Skills" -> Icons.Default.Handyman
        "Projects" -> Icons.Default.Work
        "Experience" -> Icons.Default.BusinessCenter
        "Certifications" -> Icons.Default.Verified
        "Achievements" -> Icons.Default.Star
        "Languages" -> Icons.Default.Language
        "Hobbies" -> Icons.Default.Favorite
        "References" -> Icons.Default.People
        else -> Icons.Default.Bookmark
    }
}

// --- 25 Dynamic Template Definitions ---
data class TemplatePreset(val id: Int, val name: String, val category: String, val description: String)

val ResumeTemplatesList = listOf(
    TemplatePreset(0, "Modern Tech", "Modern", "Sleek, heavy left accent line, colored badges, perfect for professionals."),
    TemplatePreset(1, "Corporate Prestige", "Corporate", "Dynamic top banner in the brand accent color, elegant layout hierarchy."),
    TemplatePreset(2, "Executive Boardroom", "Executive", "Traditional elegant navy and black headers, classical typography, high luxury."),
    TemplatePreset(3, "Minimalist Air", "Minimal", "Highly spacious, bold margins, thin dividers, clean lines."),
    TemplatePreset(4, "First Student", "Student", "Combines objectives, education first, and dynamic project cards."),
    TemplatePreset(5, "ATS Certified Pro", "ATS Friendly", "Standard single-column, zero grids or custom canvas. Maximum parser readability."),
    TemplatePreset(6, "Creative Split", "Creative", "Split horizontal grid, left rail is modern color tinted with skills and contacts."),
    TemplatePreset(7, "Elegance Dark", "Dark Theme", "Inverse colors, premium slate charcoal background with neon accent text."),
    TemplatePreset(8, "Premium Business", "Corporate", "Double divider borders, luxurious executive letter layout, gold/navy themes."),
    TemplatePreset(9, "Metropolitan Rail", "Sidebar", "Sturdy colored left canvas rail encompassing profile picture and skills, modern."),
    TemplatePreset(10, "Warm Ochre", "Creative", "Soft pastel cream backdrop, dark brown text, cozy elegant rounded sections."),
    TemplatePreset(11, "Slate Block", "Modern", "Section headers are block-enclosed in primary colors, delivering sharp layout cues."),
    TemplatePreset(12, "Teal Spark", "Creative", "Striking primary gradient dividing lines and bold teal bullet metrics."),
    TemplatePreset(13, "Asymmetric Grid", "Creative", "Overlapping headers, visual offset layout for striking creative agencies."),
    TemplatePreset(14, "Double Column", "Sidebar", "Clever left-right balanced columns, maximizes single-page experience."),
    TemplatePreset(15, "Compact Ledger", "ATS Friendly", "Bulleted rows are bordered with a soft ledger style margin."),
    TemplatePreset(16, "Bordeaux Elite", "Executive", "Luxury wine-colored margins and elegant typography for top commanders."),
    TemplatePreset(17, "Journal Headline", "Creative", "Simulates an elegant academic or recruiter-grade editorial publication."),
    TemplatePreset(18, "Cyberpunk Canvas", "Dark Theme", "Futuristic pitch-black background with matrix cyan lines and custom technical metrics."),
    TemplatePreset(19, "Aero Sleek", "Minimal", "Glow gradients on sections, ultra-modern card elements holding skills."),
    TemplatePreset(20, "Royal Gold", "Executive", "Gleaming gold trim accentuations with rich navy labels."),
    TemplatePreset(21, "Nordic Wind", "Minimal", "Cold ice-blue outlines, modular pill blocks, and spacious white borders."),
    TemplatePreset(22, "Brutalist Monochrome", "Creative", "High-contrast block panels, thick black border strokes, bold typographic statements."),
    TemplatePreset(23, "Vanguard Angle", "Modern", "Dynamic diagonal divider bars and sharp custom corner geometry."),
    TemplatePreset(24, "Ledger Minimal", "Minimal", "Understated left ledger indices with dots mapping timeline progress.")
)

// --- Resume Template Drawing Entry point ---

@Composable
fun ResumePreviewRenderer(
    resume: Resume,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false,
    fontSizeAdjustment: Int = 0 // Offset
) {
    val selectedColorHex = resume.themeColor
    val primaryColor = try {
        Color(android.graphics.Color.parseColor(selectedColorHex))
    } catch (e: Exception) {
        Color(0xFF1E3A8A)
    }

    val baseFontFamily = getFontFamily(resume.fontFamily)
    val baseSize = (resume.fontSize + fontSizeAdjustment).coerceIn(10, 24)

    val canvasBg = if (isDarkMode || resume.templateId == 7 || resume.templateId == 18) {
        Color(0xFF111827) // Dark gray/black
    } else if (resume.templateId == 10) {
        Color(0xFFFFFDF9) // Pastelly Warm Cream
    } else {
        Color.White
    }

    val textPrimary = if (isDarkMode || resume.templateId == 7 || resume.templateId == 18) {
        Color.White
    } else {
        Color(0xFF111827)
    }

    val textSecondary = if (isDarkMode || resume.templateId == 7 || resume.templateId == 18) {
        Color(0xFF9CA3AF)
    } else {
        Color(0xFF4B5563)
    }

    // Parse Nested lists
    val education = ResumeMoshiHelper.fromJson(resume.educationJson, Education::class.java)
    val skills = ResumeMoshiHelper.fromJson(resume.skillsJson, Skill::class.java)
    val projects = ResumeMoshiHelper.fromJson(resume.projectsJson, Project::class.java)
    val experiences = ResumeMoshiHelper.fromJson(resume.experienceJson, Experience::class.java)
    val certifications = ResumeMoshiHelper.fromJson(resume.certificationsJson, String::class.java)
    val achievements = ResumeMoshiHelper.fromJson(resume.achievementsJson, String::class.java)
    val languages = ResumeMoshiHelper.fromJson(resume.languagesJson, String::class.java)
    val hobbies = ResumeMoshiHelper.fromJson(resume.hobbiesJson, String::class.java)
    val customSections = ResumeMoshiHelper.fromJson(resume.customSectionsJson, CustomSection::class.java)
    val references = ResumeMoshiHelper.fromJson(resume.referencesJson, Reference::class.java)

    // Visibility Check helper
    val visibilityMap = try {
        val moshi = com.squareup.moshi.Moshi.Builder().build()
        val adapter = moshi.adapter<Map<String, Boolean>>(
            com.squareup.moshi.Types.newParameterizedType(Map::class.java, String::class.java, java.lang.Boolean::class.java)
        )
        adapter.fromJson(resume.sectionVisibilityJson) ?: emptyMap()
    } catch (e: Exception) {
        emptyMap()
    }

    fun isSectionVisible(title: String): Boolean {
        return visibilityMap[title] != false
    }

    // Section Ordering helper
    val orderList = try {
        val moshi = com.squareup.moshi.Moshi.Builder().build()
        val adapter = moshi.adapter<List<String>>(
            com.squareup.moshi.Types.newParameterizedType(List::class.java, String::class.java)
        )
        adapter.fromJson(resume.sectionOrderJson) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    val finalOrder = if (orderList.isNotEmpty()) orderList else listOf("Summary", "Experience", "Projects", "Skills", "Education", "Certifications", "Achievements", "Languages", "Hobbies", "References")

    // --- Header Section ---
    @Composable
    fun HeaderLayout() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resume.fullname.ifBlank { "Client Fullname" },
                    fontFamily = baseFontFamily,
                    fontSize = (baseSize + 10).sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    lineHeight = (baseSize + 12).sp
                )
                
                if (resume.objective.isNotBlank()) {
                    Text(
                        text = resume.objective,
                        fontFamily = baseFontFamily,
                        fontSize = (baseSize - 1).sp,
                        fontStyle = FontStyle.Italic,
                        color = textSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Contact grid
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (resume.phone.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Phone",
                                tint = primaryColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(resume.phone, fontSize = (baseSize - 3).sp, color = textSecondary, fontFamily = baseFontFamily)
                        }
                    }
                    if (resume.email.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = primaryColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(resume.email, fontSize = (baseSize - 3).sp, color = textSecondary, fontFamily = baseFontFamily)
                        }
                    }
                }
                if (resume.address.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Address",
                            tint = primaryColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(resume.address, fontSize = (baseSize - 3).sp, color = textSecondary, fontFamily = baseFontFamily)
                    }
                }
            }

            // Photo rendering if set
            if (!resume.photo.isNullOrBlank()) {
                val clipShape = when (resume.photoStyle) {
                    "Circular" -> CircleShape
                    "Rounded" -> RoundedCornerShape(12.dp)
                    else -> RoundedCornerShape(0.dp) // Square
                }
                AsyncImage(
                    model = resume.photo,
                    contentDescription = "Profile photo",
                    modifier = Modifier
                        .size(resume.photoSize.dp)
                        .clip(clipShape)
                        .border(1.5.dp, primaryColor, clipShape),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }

    // --- Inner section renderers ---
    @Composable
    fun SectionHeader(title: String) {
        val showIcons = resume.iconStyle != "Outlined" && resume.templateId != 5 // Skip icons in ATS friendly template
        val displayTitle = if (title == "Summary") "Profile Summary" else title

        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            when (resume.templateId) {
                11 -> { // Slate Block Style
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(primaryColor, RoundedCornerShape(4.dp))
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = displayTitle.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = (baseSize + 1).sp,
                            fontFamily = baseFontFamily
                        )
                    }
                }
                1 -> { // Corporate Prestige Style
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayTitle,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = (baseSize + 2).sp,
                            fontFamily = baseFontFamily
                        )
                    }
                    Divider(color = primaryColor, thickness = 1.5.dp, modifier = Modifier.padding(top = 2.dp, bottom = 4.dp))
                }
                8 -> { // Premium Business / Luxurious Double lines
                    Divider(color = primaryColor.copy(alpha = 0.5f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = displayTitle.uppercase(),
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = (baseSize + 2).sp,
                        fontFamily = baseFontFamily,
                        letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Divider(color = primaryColor, thickness = 1.5.dp)
                }
                else -> { // Default Clean style
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (showIcons) {
                            Icon(
                                imageVector = getIconForSection(title),
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier
                                    .size((baseSize + 2).dp)
                                    .padding(end = 4.dp)
                            )
                        }
                        Text(
                            text = displayTitle,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = (baseSize + 2).sp,
                            fontFamily = baseFontFamily
                        )
                    }
                    Divider(color = textSecondary.copy(alpha = 0.3f), modifier = Modifier.padding(top = 2.dp, bottom = 4.dp))
                }
            }
        }
    }

    // Render individual components safely
    @Composable
    fun RenderSummarySection() {
        if (resume.summary.isNotBlank()) {
            Text(
                text = resume.summary,
                fontSize = baseSize.sp,
                color = textPrimary,
                fontFamily = baseFontFamily,
                lineHeight = (baseSize + 4).sp
            )
        }
    }

    @Composable
    fun RenderEducationSection() {
        if (education.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                education.forEach { edu ->
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = edu.degree.ifBlank { "Degree" },
                                fontWeight = FontWeight.Bold,
                                fontSize = baseSize.sp,
                                color = textPrimary,
                                fontFamily = baseFontFamily
                            )
                            Text(
                                text = edu.duration,
                                fontSize = (baseSize - 1).sp,
                                color = textSecondary,
                                fontFamily = baseFontFamily,
                                fontStyle = FontStyle.Italic
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = edu.school.ifBlank { "Institution" },
                                fontSize = (baseSize - 1).sp,
                                color = textSecondary,
                                fontFamily = baseFontFamily
                            )
                            if (edu.gpa.isNotBlank()) {
                                Text(
                                    text = "GPA: ${edu.gpa}",
                                    fontSize = (baseSize - 1).sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryColor,
                                    fontFamily = baseFontFamily
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun RenderSkillsSection() {
        if (skills.isNotEmpty()) {
            // Render categorized or badges or percentage list depending on iconStyle or presets
            when {
                resume.templateId == 18 || resume.templateId == 0 -> { // Grid Pill Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Column {
                            skills.chunked(3).forEach { rowSkills ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    rowSkills.forEach { skill ->
                                        Box(
                                            modifier = Modifier
                                                .background(primaryColor.copy(alpha = 0.12f), RoundedCornerShape(100.dp))
                                                .border(0.5.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(100.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "${skill.name} (${skill.percentage}%)",
                                                color = primaryColor,
                                                fontSize = (baseSize - 2).sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = baseFontFamily
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> { // Standard skill progress bars
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        skills.forEach { skill ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = skill.name,
                                    fontSize = baseSize.sp,
                                    color = textPrimary,
                                    fontFamily = baseFontFamily,
                                    modifier = Modifier.weight(0.4f)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(0.5f)
                                        .height(6.dp)
                                        .background(textSecondary.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(skill.percentage / 100f)
                                            .background(primaryColor, RoundedCornerShape(100.dp))
                                    )
                                }
                                Text(
                                    text = "${skill.percentage}%",
                                    fontSize = (baseSize - 2).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier
                                        .weight(0.1f)
                                        .padding(start = 4.dp),
                                    fontFamily = baseFontFamily
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun RenderExperienceSection() {
        if (experiences.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                experiences.forEach { exp ->
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = exp.role.ifBlank { "Role Title" },
                                fontWeight = FontWeight.Bold,
                                fontSize = baseSize.sp,
                                color = textPrimary,
                                fontFamily = baseFontFamily
                            )
                            Text(
                                text = exp.duration,
                                fontSize = (baseSize - 1).sp,
                                color = textSecondary,
                                fontFamily = baseFontFamily,
                                fontStyle = FontStyle.Italic
                            )
                        }
                        Text(
                            text = exp.company.ifBlank { "Company Name" },
                            fontSize = (baseSize - 1).sp,
                            fontWeight = FontWeight.Medium,
                            color = primaryColor,
                            fontFamily = baseFontFamily,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        if (exp.description.isNotBlank()) {
                            Text(
                                text = exp.description,
                                fontSize = (baseSize - 1).sp,
                                color = textPrimary,
                                fontFamily = baseFontFamily,
                                lineHeight = (baseSize + 2).sp
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun RenderProjectsSection() {
        if (projects.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                projects.forEach { proj ->
                    Column {
                        Text(
                            text = proj.title.ifBlank { "Project Title" },
                            fontWeight = FontWeight.Bold,
                            fontSize = baseSize.sp,
                            color = textPrimary,
                            fontFamily = baseFontFamily
                        )
                        if (proj.techStack.isNotBlank()) {
                            Text(
                                text = "Technologies: ${proj.techStack}",
                                fontSize = (baseSize - 2).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryColor,
                                fontFamily = baseFontFamily,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                        if (proj.description.isNotBlank()) {
                            Text(
                                text = proj.description,
                                fontSize = (baseSize - 1).sp,
                                color = textSecondary,
                                fontFamily = baseFontFamily,
                                lineHeight = (baseSize + 2).sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Helper for List-of-strings based sections
    @Composable
    fun RenderStringListSection(items: List<String>, isBullet: Boolean = true) {
        if (items.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        if (isBullet) {
                            Text(
                                text = "• ",
                                color = primaryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = baseSize.sp,
                                fontFamily = baseFontFamily
                            )
                        }
                        Text(
                            text = item,
                            fontSize = baseSize.sp,
                            color = textPrimary,
                            fontFamily = baseFontFamily
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun RenderReferencesSection() {
        if (references.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                references.forEach { ref ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ref.name.ifBlank { "Reference Name" },
                            fontWeight = FontWeight.Bold,
                            fontSize = baseSize.sp,
                            color = textPrimary,
                            fontFamily = baseFontFamily
                        )
                        Text(
                            text = ref.company,
                            fontSize = (baseSize - 1).sp,
                            color = textSecondary,
                            fontFamily = baseFontFamily
                        )
                        Text(
                            text = ref.contact,
                            fontSize = (baseSize - 2).sp,
                            color = primaryColor,
                            fontStyle = FontStyle.Italic,
                            fontFamily = baseFontFamily
                        )
                    }
                }
            }
        }
    }

    // --- Main Layout Canvas ---
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(canvasBg)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // ALWAYS Header first
            HeaderLayout()
            Spacer(modifier = Modifier.height(4.dp))

            // Dynamic section layouts depending on ordering preferences
            finalOrder.forEach { sectionTitle ->
                if (isSectionVisible(sectionTitle)) {
                    var hasContent = false
                    when (sectionTitle) {
                        "Summary" -> if (resume.summary.isNotBlank()) hasContent = true
                        "Education" -> if (education.isNotEmpty()) hasContent = true
                        "Skills" -> if (skills.isNotEmpty()) hasContent = true
                        "Projects" -> if (projects.isNotEmpty()) hasContent = true
                        "Experience" -> if (experiences.isNotEmpty()) hasContent = true
                        "Certifications" -> if (certifications.isNotEmpty()) hasContent = true
                        "Achievements" -> if (achievements.isNotEmpty()) hasContent = true
                        "Languages" -> if (languages.isNotEmpty()) hasContent = true
                        "Hobbies" -> if (hobbies.isNotEmpty()) hasContent = true
                        "References" -> if (references.isNotEmpty()) hasContent = true
                    }

                    if (hasContent) {
                        SectionHeader(title = sectionTitle)
                        when (sectionTitle) {
                            "Summary" -> RenderSummarySection()
                            "Education" -> RenderEducationSection()
                            "Skills" -> RenderSkillsSection()
                            "Projects" -> RenderProjectsSection()
                            "Experience" -> RenderExperienceSection()
                            "Certifications" -> RenderStringListSection(certifications)
                            "Achievements" -> RenderStringListSection(achievements)
                            "Languages" -> RenderStringListSection(languages, isBullet = false)
                            "Hobbies" -> RenderStringListSection(hobbies, isBullet = false)
                            "References" -> RenderReferencesSection()
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            // Custom Sections
            if (customSections.isNotEmpty()) {
                customSections.forEach { block ->
                    if (block.sectionTitle.isNotBlank() && block.content.isNotBlank()) {
                        SectionHeader(title = block.sectionTitle)
                        Text(
                            text = block.content,
                            fontSize = baseSize.sp,
                            color = textPrimary,
                            fontFamily = baseFontFamily,
                            lineHeight = (baseSize + 3).sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}
