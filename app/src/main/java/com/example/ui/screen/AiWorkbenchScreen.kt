package com.example.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.viewmodel.ResumeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiWorkbenchScreen(
    viewModel: ResumeViewModel,
    resumeId: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentResume by viewModel.currentResume.collectAsState()
    val aiLoading by viewModel.aiLoading.collectAsState()
    val aiResult by viewModel.aiResult.collectAsState()
    val atsScoreResult by viewModel.atsScoreResult.collectAsState()

    var targetRoleByAts by remember { mutableStateOf("Senior Kotlin Developer") }
    val scrollState = rememberScrollState()

    LaunchedEffect(resumeId) {
        viewModel.loadResume(resumeId)
    }

    val resume = currentResume
    if (resume == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Build plain-text representation of core fields for AI analysis
    val resumePlainText = remember(resume) {
        val educations = ResumeMoshiHelper.fromJson(resume.educationJson, Education::class.java)
        val skills = ResumeMoshiHelper.fromJson(resume.skillsJson, Skill::class.java)
        val experiences = ResumeMoshiHelper.fromJson(resume.experienceJson, Experience::class.java)
        val projects = ResumeMoshiHelper.fromJson(resume.projectsJson, Project::class.java)
        
        buildString {
            appendLine("Name: ${resume.fullname}")
            appendLine("Target objective: ${resume.objective}")
            appendLine("Profile Summary: ${resume.summary}")
            appendLine("Skills: ${skills.joinToString { "${it.name} (${it.percentage}%)" }}")
            appendLine("Experience:")
            experiences.forEach { exp ->
                appendLine("- ${exp.role} at ${exp.company} (${exp.duration}): ${exp.description}")
            }
            appendLine("Projects:")
            projects.forEach { proj ->
                appendLine("- ${proj.title} using ${proj.techStack}: ${proj.description}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini AI Workbench", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // --- Welcome Banner ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(45.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Recruiter Audit Copilot", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            "Audit keyword scores, scan against expectations, and receive instant improvements to clear Applicant Tracking System (ATS) filters.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // --- ATS Compatibility Checker Panel ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ats_scanner_panel"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("ATS Compatibility Audit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    
                    OutlinedTextField(
                        value = targetRoleByAts,
                        onValueChange = { targetRoleByAts = it },
                        label = { Text("Enter Target Role / Job Title") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.aiAnalyzeAtsCompatibility(targetRoleByAts, resumePlainText)
                        },
                        enabled = !aiLoading && targetRoleByAts.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (aiLoading && atsScoreResult == null) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Icon(Icons.Default.CenterFocusStrong, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Analyze Match Score", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Score Display Outcome
                    atsScoreResult?.let { analysis ->
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Circular gauge
                            Box(
                                modifier = Modifier
                                    .size(75.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            analysis.score >= 80 -> Color(0xFF10B981).copy(alpha = 0.12f)
                                            analysis.score >= 50 -> Color(0xFFF59E0B).copy(alpha = 0.12f)
                                            else -> Color(0xFFEF4444).copy(alpha = 0.12f)
                                        }
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        color = when {
                                            analysis.score >= 80 -> Color(0xFF10B981)
                                            analysis.score >= 50 -> Color(0xFFF59E0B)
                                            else -> Color(0xFFEF4444)
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${analysis.score}%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = when {
                                        analysis.score >= 80 -> Color(0xFF10B981)
                                        analysis.score >= 50 -> Color(0xFFD97706)
                                        else -> Color(0xFFDC2626)
                                    }
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when {
                                        analysis.score >= 80 -> "Premium Match! High ATS Compatibility"
                                        analysis.score >= 50 -> "Medium Match. Consider adjustments"
                                        else -> "Low Match! Critical sections missing"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Based on keyword occurrence, section formatting density, and skill relevance checklist for '$targetRoleByAts'.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        // Matching list
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Matched Industry Keywords:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF10B981))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                analysis.matchingKeywords.forEach { kw ->
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF10B981).copy(alpha = 0.12f), RoundedCornerShape(100.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(kw, fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        // Missing keywords
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Expected Keywords Missing (Filter risks):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFEF4444))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                analysis.missingKeywords.forEach { kw ->
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFEF4444).copy(alpha = 0.12f), RoundedCornerShape(100.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(kw, fontSize = 10.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        // Actionable fixes checklist
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("ATS Improvement Recommendation Checklist:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            analysis.improvementsChecklist.forEach { bullet ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.OfflinePin, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier
                                        .size(15.dp)
                                        .padding(top = 2.dp, end = 3.dp))
                                    Text(bullet, fontSize = 11.sp, lineHeight = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // --- AI General Polisher Card ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("AI General Polishing Advice", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "Scans your contact details, objective statements, work history descriptions, and projects to generate bullet-by-bullet revision feedback.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    Button(
                        onClick = {
                            val skills = ResumeMoshiHelper.fromJson(resume.skillsJson, Skill::class.java).joinToString { it.name }
                            val experiences = ResumeMoshiHelper.fromJson(resume.experienceJson, Experience::class.java).joinToString { "${it.role} : ${it.description}" }
                            viewModel.aiAnalyzeImprovement(
                                fullname = resume.fullname,
                                summary = resume.summary,
                                skills = skills,
                                experiences = experiences
                            )
                        },
                        enabled = !aiLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                    ) {
                        if (aiLoading && aiResult == null) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Icon(Icons.Default.TipsAndUpdates, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Polish Entire Resume with AI", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Result
                    aiResult?.let { advice ->
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Text("AI Copilot Recommendations:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFEC4899))
                        Text(
                            text = advice,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}
