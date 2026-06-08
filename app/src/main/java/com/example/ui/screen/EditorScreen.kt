package com.example.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.templates.ResumePreviewRenderer
import com.example.ui.viewmodel.ResumeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: ResumeViewModel,
    resumeId: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val currentResume by viewModel.currentResume.collectAsState()
    val aiLoading by viewModel.aiLoading.collectAsState()
    val context = LocalContext.current

    // Active Category Tab
    var activeTab by remember { mutableStateOf(0) }
    val tabsList = listOf("Contact", "Biography", "Work History", "Education", "Projects", "Skills", "Extras")

    // Inline generator states
    var isSummaryBuilderOpen by remember { mutableStateOf(false) }
    var targetRoleBySummary by remember { mutableStateOf("") }
    var experienceHighlights by remember { mutableStateOf("") }

    var isProjectGeneratorOpen by remember { mutableStateOf(false) }
    var targetProjectTitle by remember { mutableStateOf("") }
    var targetProjectTech by remember { mutableStateOf("") }

    var isSkillsSuggesterOpen by remember { mutableStateOf(false) }
    var targetRoleBySkills by remember { mutableStateOf("") }
    var skillsCategory by remember { mutableStateOf("Technical") }

    // System Image Picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            currentResume?.let { resume ->
                viewModel.updateCurrentResumeState(resume.copy(photo = uri.toString()))
            }
        }
    }

    // Toggle Preview Sheet Option (split panel simulation for smaller devices)
    var isLivePreviewVisible by remember { mutableStateOf(false) }

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

    // Deserialized list items
    val educations = remember(resume.educationJson) { ResumeMoshiHelper.fromJson(resume.educationJson, Education::class.java) }
    val skills = remember(resume.skillsJson) { ResumeMoshiHelper.fromJson(resume.skillsJson, Skill::class.java) }
    val experiences = remember(resume.experienceJson) { ResumeMoshiHelper.fromJson(resume.experienceJson, Experience::class.java) }
    val projects = remember(resume.projectsJson) { ResumeMoshiHelper.fromJson(resume.projectsJson, Project::class.java) }
    val customSections = remember(resume.customSectionsJson) { ResumeMoshiHelper.fromJson(resume.customSectionsJson, CustomSection::class.java) }

    val certifications = remember(resume.certificationsJson) { ResumeMoshiHelper.fromJson(resume.certificationsJson, String::class.java) }
    val achievements = remember(resume.achievementsJson) { ResumeMoshiHelper.fromJson(resume.achievementsJson, String::class.java) }
    val languages = remember(resume.languagesJson) { ResumeMoshiHelper.fromJson(resume.languagesJson, String::class.java) }
    val hobbies = remember(resume.hobbiesJson) { ResumeMoshiHelper.fromJson(resume.hobbiesJson, String::class.java) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        var isEditingName by remember { mutableStateOf(false) }
                        if (isEditingName) {
                            OutlinedTextField(
                                value = resume.title,
                                onValueChange = { viewModel.updateCurrentResumeState(resume.copy(title = it)) },
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = { isEditingName = false }) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981))
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .padding(vertical = 4.dp)
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { isEditingName = true }) {
                                Text(resume.title, fontWeight = FontWeight.Bold, maxLines = 1)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Edit, contentDescription = "Edit title", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Auto-saving", fontSize = 10.sp, color = Color(0xFF10B981))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back to dashboards")
                    }
                },
                actions = {
                    // ATS rating widget
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(
                                color = when {
                                    resume.score >= 80 -> Color(0xFF10B981).copy(alpha = 0.12f)
                                    resume.score >= 50 -> Color(0xFFF59E0B).copy(alpha = 0.12f)
                                    else -> Color(0xFFEF4444).copy(alpha = 0.12f)
                                },
                                shape = RoundedCornerShape(100.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = when {
                                    resume.score >= 80 -> Color(0xFF10B981).copy(alpha = 0.3f)
                                    resume.score >= 50 -> Color(0xFFF59E0B).copy(alpha = 0.3f)
                                    else -> Color(0xFFEF4444).copy(alpha = 0.3f)
                                },
                                shape = RoundedCornerShape(100.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Score: ${resume.score}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = when {
                                resume.score >= 80 -> Color(0xFF10B981)
                                resume.score >= 50 -> Color(0xFFD97706)
                                else -> Color(0xFFDC2626)
                            }
                        )
                    }

                    // Bottom sheet preview trigger
                    IconButton(
                        onClick = { isLivePreviewVisible = !isLivePreviewVisible },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isLivePreviewVisible) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = if (isLivePreviewVisible) Icons.Default.Preview else Icons.Default.Visibility,
                            contentDescription = "Toggle live preview sidepanel"
                        )
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
        ) {
            // Horizontal Categories Scroll Tab Slider
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabsList.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                }
            }

            // Main Layout Workspace Grid split (if live preview is on)
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                // Left Scrolling Editor Panel
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        when (activeTab) {
                            0 -> { // Contact Category Screen
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    SectionHeaderCard("Profile Picture Studio") {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Box(
                                                    modifier = Modifier.size(100.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (!resume.photo.isNullOrBlank()) {
                                                        val shapeOfPhoto = when (resume.photoStyle) {
                                                            "Circular" -> CircleShape
                                                            "Rounded" -> RoundedCornerShape(12.dp)
                                                            else -> RoundedCornerShape(0.dp)
                                                        }
                                                        AsyncImage(
                                                            model = resume.photo,
                                                            contentDescription = null,
                                                            modifier = Modifier
                                                                .size(resume.photoSize.dp.coerceIn(40.dp, 100.dp))
                                                                .clip(shapeOfPhoto)
                                                                .border(2.dp, MaterialTheme.colorScheme.primary, shapeOfPhoto),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(90.dp)
                                                                .clip(CircleShape)
                                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(30.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                                        }
                                                    }
                                                }
                                                
                                                Spacer(modifier = Modifier.width(16.dp))
                                                
                                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Button(
                                                            onClick = { imagePickerLauncher.launch("image/*") },
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                                        ) {
                                                            Text("Upload", fontSize = 12.sp)
                                                        }
                                                        if (!resume.photo.isNullOrBlank()) {
                                                            OutlinedButton(
                                                                onClick = { viewModel.updateCurrentResumeState(resume.copy(photo = null)) },
                                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                                            ) {
                                                                Text("Remove", fontSize = 12.sp)
                                                            }
                                                        }
                                                    }
                                                    
                                                    // Dropdown / styles picker
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text("Shape: ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        listOf("Circular", "Rounded", "Square").forEach { style ->
                                                            Box(
                                                                modifier = Modifier
                                                                    .padding(horizontal = 4.dp)
                                                                    .background(
                                                                        color = if (resume.photoStyle == style) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                                        shape = RoundedCornerShape(100.dp)
                                                                    )
                                                                    .clickable { viewModel.updateCurrentResumeState(resume.copy(photoStyle = style)) }
                                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                                            ) {
                                                                Text(style, fontSize = 10.sp, color = if (resume.photoStyle == style) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            // Slider for photo sizing
                                            if (!resume.photo.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text("Adjust Photo Size: ${resume.photoSize}dp", fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
                                                Slider(
                                                    value = resume.photoSize.toFloat(),
                                                    onValueChange = { viewModel.updateCurrentResumeState(resume.copy(photoSize = it.toInt())) },
                                                    valueRange = 50f..120f
                                                )
                                            }
                                        }
                                    }

                                    SectionHeaderCard("Core Contact Details") {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            OutlinedTextField(
                                                value = resume.fullname,
                                                onValueChange = { viewModel.updateCurrentResumeState(resume.copy(fullname = it)) },
                                                label = { Text("Candidate Full Name") },
                                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Text,
                                                    imeAction = ImeAction.Next
                                                ),
                                                keyboardActions = KeyboardActions(
                                                    onNext = { focusManager.moveFocus(FocusDirection.Next) }
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth().testTag("editor_fullname")
                                            )

                                            OutlinedTextField(
                                                value = resume.phone,
                                                onValueChange = { viewModel.updateCurrentResumeState(resume.copy(phone = it)) },
                                                label = { Text("Phone Number") },
                                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Phone,
                                                    imeAction = ImeAction.Next
                                                ),
                                                keyboardActions = KeyboardActions(
                                                    onNext = { focusManager.moveFocus(FocusDirection.Next) }
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            OutlinedTextField(
                                                value = resume.email,
                                                onValueChange = { viewModel.updateCurrentResumeState(resume.copy(email = it)) },
                                                label = { Text("Email Address") },
                                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Email,
                                                    imeAction = ImeAction.Next
                                                ),
                                                keyboardActions = KeyboardActions(
                                                    onNext = { focusManager.moveFocus(FocusDirection.Next) }
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            OutlinedTextField(
                                                value = resume.address,
                                                onValueChange = { viewModel.updateCurrentResumeState(resume.copy(address = it)) },
                                                label = { Text("City, State, Zip") },
                                                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Text,
                                                    imeAction = ImeAction.Done
                                                ),
                                                keyboardActions = KeyboardActions(
                                                    onDone = { focusManager.clearFocus() }
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                            1 -> { // Biography Category Screen
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    SectionHeaderCard("Profile Summary & Career Objective") {
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            OutlinedTextField(
                                                value = resume.objective,
                                                onValueChange = { viewModel.updateCurrentResumeState(resume.copy(objective = it)) },
                                                label = { Text("Job Target / Objectives Heading") },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Text,
                                                    imeAction = ImeAction.Next
                                                ),
                                                keyboardActions = KeyboardActions(
                                                    onNext = { focusManager.moveFocus(FocusDirection.Next) }
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Professional bio highlights", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    AssistChip(
                                                        onClick = { isSummaryBuilderOpen = true },
                                                        label = { Text("Custom AI") },
                                                        leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp)) }
                                                    )
                                                    AssistChip(
                                                        onClick = {
                                                            if (resume.objective.isNotBlank()) {
                                                                viewModel.aiGenerateProfileSummary(
                                                                    role = resume.objective,
                                                                    experienceYears = "5+",
                                                                    highlights = ""
                                                                ) { generated ->
                                                                    viewModel.updateCurrentResumeState(resume.copy(summary = generated))
                                                                }
                                                            } else {
                                                                android.widget.Toast.makeText(context, "Please enter Job Target / Objectives first!", android.widget.Toast.LENGTH_SHORT).show()
                                                            }
                                                        },
                                                        label = { 
                                                            if (aiLoading) {
                                                                    Text("Writing...")
                                                            } else {
                                                                Text("Quick AI Fill")
                                                            }
                                                        },
                                                        leadingIcon = { 
                                                            if (aiLoading) {
                                                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                                                            } else {
                                                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(13.dp))
                                                            }
                                                        },
                                                        enabled = !aiLoading
                                                    )
                                                }
                                            }

                                            OutlinedTextField(
                                                value = resume.summary,
                                                onValueChange = { viewModel.updateCurrentResumeState(resume.copy(summary = it)) },
                                                minLines = 4,
                                                maxLines = 8,
                                                label = { Text("Enter Profile Summary") },
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth().testTag("editor_summary")
                                            )
                                        }
                                    }

                                    // Inline AI Summary builder dialog
                                    if (isSummaryBuilderOpen) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                    Text("AI Executive Summary Writer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    IconButton(onClick = { isSummaryBuilderOpen = false }, modifier = Modifier.size(20.dp)) {
                                                        Icon(Icons.Default.Close, contentDescription = null)
                                                    }
                                                }
                                                OutlinedTextField(
                                                    value = targetRoleBySummary,
                                                    onValueChange = { targetRoleBySummary = it },
                                                    label = { Text("Target Job Title") },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                OutlinedTextField(
                                                    value = experienceHighlights,
                                                    onValueChange = { experienceHighlights = it },
                                                    minLines = 2,
                                                    label = { Text("Points/highlights (e.g. 5 yrs in Kotlin, led team)") },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.aiGenerateProfileSummary(
                                                                role = targetRoleBySummary,
                                                                experienceYears = "5+",
                                                                highlights = experienceHighlights
                                                            ) { generated ->
                                                                viewModel.updateCurrentResumeState(resume.copy(summary = generated))
                                                                isSummaryBuilderOpen = false
                                                            }
                                                        },
                                                        enabled = !aiLoading && targetRoleBySummary.isNotBlank()
                                                    ) {
                                                        if (aiLoading) {
                                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                                        } else {
                                                            Text("Generate Summary", fontSize = 11.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            2 -> { // Work History Category Screen
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${experiences.size} Work Experience Logs", fontWeight = FontWeight.Bold)
                                        IconButton(onClick = {
                                            val updatedList = experiences + Experience()
                                            viewModel.updateCurrentResumeState(resume.copy(experienceJson = ResumeMoshiHelper.toJson(updatedList, Experience::class.java)))
                                        }) {
                                            Icon(Icons.Default.AddCircle, contentDescription = "Add experience log", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }

                                    // List cards
                                    experiences.forEachIndexed { idx, exp ->
                                        Card(modifier = Modifier.fillMaxWidth()) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                    Text("Experience Block #${idx + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete item",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier
                                                            .size(16.dp)
                                                            .clickable {
                                                                val updatedList = experiences.filterIndexed { index, _ -> index != idx }
                                                                viewModel.updateCurrentResumeState(resume.copy(experienceJson = ResumeMoshiHelper.toJson(updatedList, Experience::class.java)))
                                                            }
                                                    )
                                                }
                                                OutlinedTextField(
                                                    value = exp.company,
                                                    onValueChange = { company ->
                                                        val updatedList = experiences.toMutableList()
                                                        updatedList[idx] = exp.copy(company = company)
                                                        viewModel.updateCurrentResumeState(resume.copy(experienceJson = ResumeMoshiHelper.toJson(updatedList, Experience::class.java)))
                                                    },
                                                    label = { Text("Company Name") },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Text,
                                                        imeAction = ImeAction.Next
                                                    ),
                                                    keyboardActions = KeyboardActions(
                                                        onNext = { focusManager.moveFocus(FocusDirection.Next) }
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                OutlinedTextField(
                                                    value = exp.role,
                                                    onValueChange = { role ->
                                                        val updatedList = experiences.toMutableList()
                                                        updatedList[idx] = exp.copy(role = role)
                                                        viewModel.updateCurrentResumeState(resume.copy(experienceJson = ResumeMoshiHelper.toJson(updatedList, Experience::class.java)))
                                                    },
                                                    label = { Text("Job Title / Role") },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Text,
                                                        imeAction = ImeAction.Next
                                                    ),
                                                    keyboardActions = KeyboardActions(
                                                        onNext = { focusManager.moveFocus(FocusDirection.Next) }
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                OutlinedTextField(
                                                    value = exp.duration,
                                                    onValueChange = { duration ->
                                                        val updatedList = experiences.toMutableList()
                                                        updatedList[idx] = exp.copy(duration = duration)
                                                        viewModel.updateCurrentResumeState(resume.copy(experienceJson = ResumeMoshiHelper.toJson(updatedList, Experience::class.java)))
                                                    },
                                                    label = { Text("Duration (e.g. 2021 - Present)") },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Text,
                                                        imeAction = ImeAction.Next
                                                    ),
                                                    keyboardActions = KeyboardActions(
                                                        onNext = { focusManager.moveFocus(FocusDirection.Next) }
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("Responsibilities & Projects", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                                    AssistChip(
                                                        onClick = {
                                                            if (exp.role.isNotBlank()) {
                                                                viewModel.aiGenerateJobDescription(
                                                                    role = exp.role,
                                                                    company = exp.company
                                                                ) { generated ->
                                                                    val updatedList = experiences.toMutableList()
                                                                    updatedList[idx] = exp.copy(description = generated)
                                                                    viewModel.updateCurrentResumeState(resume.copy(experienceJson = ResumeMoshiHelper.toJson(updatedList, Experience::class.java)))
                                                                }
                                                            } else {
                                                                android.widget.Toast.makeText(context, "Please enter Job Title / Role first!", android.widget.Toast.LENGTH_SHORT).show()
                                                            }
                                                        },
                                                        label = { 
                                                            if (aiLoading) {
                                                                Text("Suggesting...")
                                                            } else {
                                                                Text("AI Suggest")
                                                            }
                                                        },
                                                        leadingIcon = { 
                                                            if (aiLoading) {
                                                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                                                            } else {
                                                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp))
                                                            }
                                                        },
                                                        enabled = !aiLoading
                                                    )
                                                }
                                                OutlinedTextField(
                                                    value = exp.description,
                                                    onValueChange = { description ->
                                                        val updatedList = experiences.toMutableList()
                                                        updatedList[idx] = exp.copy(description = description)
                                                        viewModel.updateCurrentResumeState(resume.copy(experienceJson = ResumeMoshiHelper.toJson(updatedList, Experience::class.java)))
                                                    },
                                                    minLines = 3,
                                                    label = { Text("Responsibilities & Projects (suggested or custom)") },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            3 -> { // Education Category Screen
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${educations.size} Education Background Records", fontWeight = FontWeight.Bold)
                                        IconButton(onClick = {
                                            val updatedList = educations + Education()
                                            viewModel.updateCurrentResumeState(resume.copy(educationJson = ResumeMoshiHelper.toJson(updatedList, Education::class.java)))
                                        }) {
                                            Icon(Icons.Default.AddCircle, contentDescription = "Add educational degree", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }

                                    educations.forEachIndexed { idx, edu ->
                                        Card(modifier = Modifier.fillMaxWidth()) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                    Text("Education Block #${idx + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier
                                                            .size(16.dp)
                                                            .clickable {
                                                                val updatedList = educations.filterIndexed { index, _ -> index != idx }
                                                                viewModel.updateCurrentResumeState(resume.copy(educationJson = ResumeMoshiHelper.toJson(updatedList, Education::class.java)))
                                                            }
                                                    )
                                                }
                                                OutlinedTextField(
                                                    value = edu.school,
                                                    onValueChange = { school ->
                                                        val updatedList = educations.toMutableList()
                                                        updatedList[idx] = edu.copy(school = school)
                                                        viewModel.updateCurrentResumeState(resume.copy(educationJson = ResumeMoshiHelper.toJson(updatedList, Education::class.java)))
                                                    },
                                                    label = { Text("School / university") },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Text,
                                                        imeAction = ImeAction.Next
                                                    ),
                                                    keyboardActions = KeyboardActions(
                                                        onNext = { focusManager.moveFocus(FocusDirection.Next) }
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                OutlinedTextField(
                                                    value = edu.degree,
                                                    onValueChange = { degree ->
                                                        val updatedList = educations.toMutableList()
                                                        updatedList[idx] = edu.copy(degree = degree)
                                                        viewModel.updateCurrentResumeState(resume.copy(educationJson = ResumeMoshiHelper.toJson(updatedList, Education::class.java)))
                                                    },
                                                    label = { Text("Degree / Majors") },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Text,
                                                        imeAction = ImeAction.Next
                                                    ),
                                                    keyboardActions = KeyboardActions(
                                                        onNext = { focusManager.moveFocus(FocusDirection.Next) }
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    OutlinedTextField(
                                                        value = edu.duration,
                                                        onValueChange = { duration ->
                                                            val updatedList = educations.toMutableList()
                                                            updatedList[idx] = edu.copy(duration = duration)
                                                            viewModel.updateCurrentResumeState(resume.copy(educationJson = ResumeMoshiHelper.toJson(updatedList, Education::class.java)))
                                                        },
                                                        label = { Text("Year") },
                                                        singleLine = true,
                                                        keyboardOptions = KeyboardOptions(
                                                            keyboardType = KeyboardType.Text,
                                                            imeAction = ImeAction.Next
                                                        ),
                                                        keyboardActions = KeyboardActions(
                                                            onNext = { focusManager.moveFocus(FocusDirection.Next) }
                                                        ),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    OutlinedTextField(
                                                        value = edu.gpa,
                                                        onValueChange = { gpa ->
                                                            val updatedList = educations.toMutableList()
                                                            updatedList[idx] = edu.copy(gpa = gpa)
                                                            viewModel.updateCurrentResumeState(resume.copy(educationJson = ResumeMoshiHelper.toJson(updatedList, Education::class.java)))
                                                        },
                                                        label = { Text("GPA") },
                                                        singleLine = true,
                                                        keyboardOptions = KeyboardOptions(
                                                            keyboardType = KeyboardType.Text,
                                                            imeAction = ImeAction.Done
                                                        ),
                                                        keyboardActions = KeyboardActions(
                                                            onDone = { focusManager.clearFocus() }
                                                        ),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            4 -> { // Projects Category Screen
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${projects.size} Product/Engineering Projects", fontWeight = FontWeight.Bold)
                                        IconButton(onClick = {
                                            val updatedList = projects + Project()
                                            viewModel.updateCurrentResumeState(resume.copy(projectsJson = ResumeMoshiHelper.toJson(updatedList, Project::class.java)))
                                        }) {
                                            Icon(Icons.Default.AddCircle, contentDescription = "Add project log", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }

                                    // List cards
                                    projects.forEachIndexed { idx, proj ->
                                        Card(modifier = Modifier.fillMaxWidth()) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                    Text("Project Block #${idx + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.AutoAwesome,
                                                            contentDescription = "AI Project Generator",
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier
                                                                .size(16.dp)
                                                                .clickable {
                                                                    targetProjectTitle = proj.title
                                                                    targetProjectTech = proj.techStack
                                                                    isProjectGeneratorOpen = true
                                                                }
                                                        )
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Delete item",
                                                            tint = MaterialTheme.colorScheme.error,
                                                            modifier = Modifier
                                                                .size(16.dp)
                                                                .clickable {
                                                                    val updatedList = projects.filterIndexed { index, _ -> index != idx }
                                                                    viewModel.updateCurrentResumeState(resume.copy(projectsJson = ResumeMoshiHelper.toJson(updatedList, Project::class.java)))
                                                                }
                                                        )
                                                    }
                                                }
                                                OutlinedTextField(
                                                    value = proj.title,
                                                    onValueChange = { title ->
                                                        val updatedList = projects.toMutableList()
                                                        updatedList[idx] = proj.copy(title = title)
                                                        viewModel.updateCurrentResumeState(resume.copy(projectsJson = ResumeMoshiHelper.toJson(updatedList, Project::class.java)))
                                                    },
                                                    label = { Text("Project Title") },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Text,
                                                        imeAction = ImeAction.Next
                                                    ),
                                                    keyboardActions = KeyboardActions(
                                                        onNext = { focusManager.moveFocus(FocusDirection.Next) }
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                OutlinedTextField(
                                                    value = proj.techStack,
                                                    onValueChange = { ts ->
                                                        val updatedList = projects.toMutableList()
                                                        updatedList[idx] = proj.copy(techStack = ts)
                                                        viewModel.updateCurrentResumeState(resume.copy(projectsJson = ResumeMoshiHelper.toJson(updatedList, Project::class.java)))
                                                    },
                                                    label = { Text("Tech Stack (e.g. Jetpack Compose, Retrofit)") },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Text,
                                                        imeAction = ImeAction.Next
                                                    ),
                                                    keyboardActions = KeyboardActions(
                                                        onNext = { focusManager.moveFocus(FocusDirection.Next) }
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                OutlinedTextField(
                                                    value = proj.description,
                                                    onValueChange = { desc ->
                                                        val updatedList = projects.toMutableList()
                                                        updatedList[idx] = proj.copy(description = desc)
                                                        viewModel.updateCurrentResumeState(resume.copy(projectsJson = ResumeMoshiHelper.toJson(updatedList, Project::class.java)))
                                                    },
                                                    minLines = 3,
                                                    label = { Text("Project description / bullet notes") },
                                                    modifier = Modifier.fillMaxWidth()
                                                )

                                                // AI Bullet helper
                                                if (isProjectGeneratorOpen && targetProjectTitle == proj.title) {
                                                    Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                                                    ) {
                                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                            Text("AI description generator: ", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                            Button(
                                                                onClick = {
                                                                    viewModel.aiSuggestProjectDescription(targetProjectTitle, targetProjectTech) { bullets ->
                                                                        val updatedList = projects.toMutableList()
                                                                        updatedList[idx] = proj.copy(description = bullets)
                                                                        viewModel.updateCurrentResumeState(resume.copy(projectsJson = ResumeMoshiHelper.toJson(updatedList, Project::class.java)))
                                                                        isProjectGeneratorOpen = false
                                                                    }
                                                                },
                                                                enabled = !aiLoading
                                                            ) {
                                                                if (aiLoading) {
                                                                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                                                } else {
                                                                    Text("Generate Action Bullets", fontSize = 10.sp)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            5 -> { // Skills Category Screen
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${skills.size} Configured Skills", fontWeight = FontWeight.Bold)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { isSkillsSuggesterOpen = !isSkillsSuggesterOpen }) {
                                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Skill Suggestions", tint = MaterialTheme.colorScheme.primary)
                                            }
                                            IconButton(onClick = {
                                                val updatedList = skills + Skill(name = "Kotlin Skill", percentage = 85)
                                                viewModel.updateCurrentResumeState(resume.copy(skillsJson = ResumeMoshiHelper.toJson(updatedList, Skill::class.java)))
                                            }) {
                                                Icon(Icons.Default.AddCircle, contentDescription = "Add skill entry", tint = Color(0xFF10B981))
                                            }
                                        }
                                    }

                                    // AI Skills Suggestion workspace
                                    if (isSkillsSuggesterOpen) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text("AI Skill suggest tool", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                OutlinedTextField(
                                                    value = targetRoleBySkills,
                                                    onValueChange = { targetRoleBySkills = it },
                                                    label = { Text("Target Technical Field") },
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("Type:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Row {
                                                        listOf("Technical", "Soft", "General").forEach { cat ->
                                                            FilterChip(
                                                                selected = skillsCategory == cat,
                                                                onClick = { skillsCategory = cat },
                                                                label = { Text(cat, fontSize = 10.sp) },
                                                                modifier = Modifier.padding(horizontal = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Button(
                                                    onClick = {
                                                        viewModel.aiSuggestSkills(targetRoleBySkills, skillsCategory) { commaStr ->
                                                            // Split standard and load
                                                            val terms = commaStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                                            val newSkillsList = skills.toMutableList()
                                                            terms.forEach { term ->
                                                                if (!newSkillsList.any { it.name.equals(term, true) }) {
                                                                    newSkillsList.add(Skill(name = term, percentage = 80, category = skillsCategory))
                                                                }
                                                            }
                                                            viewModel.updateCurrentResumeState(resume.copy(skillsJson = ResumeMoshiHelper.toJson(newSkillsList, Skill::class.java)))
                                                            isSkillsSuggesterOpen = false
                                                        }
                                                    },
                                                    enabled = !aiLoading && targetRoleBySkills.isNotBlank(),
                                                    modifier = Modifier.align(Alignment.End)
                                                ) {
                                                    if (aiLoading) {
                                                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                                    } else {
                                                        Text("Fetch suggestions", fontSize = 10.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Skill adjust grid
                                    skills.forEachIndexed { idx, skill ->
                                        Card(modifier = Modifier.fillMaxWidth()) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                                    OutlinedTextField(
                                                        value = skill.name,
                                                        onValueChange = { n ->
                                                            val updatedList = skills.toMutableList()
                                                            updatedList[idx] = skill.copy(name = n)
                                                            viewModel.updateCurrentResumeState(resume.copy(skillsJson = ResumeMoshiHelper.toJson(updatedList, Skill::class.java)))
                                                        },
                                                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                                                        label = { Text("Skill Term") },
                                                        singleLine = true,
                                                        keyboardOptions = KeyboardOptions(
                                                            keyboardType = KeyboardType.Text,
                                                            imeAction = ImeAction.Done
                                                        ),
                                                        keyboardActions = KeyboardActions(
                                                            onDone = { focusManager.clearFocus() }
                                                        ),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    IconButton(onClick = {
                                                        val updatedList = skills.filterIndexed { index, _ -> index != idx }
                                                        viewModel.updateCurrentResumeState(resume.copy(skillsJson = ResumeMoshiHelper.toJson(updatedList, Skill::class.java)))
                                                    }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                                    }
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                                    Text("Competency: ${skill.percentage}%", fontSize = 11.sp, modifier = Modifier.weight(0.3f))
                                                    Slider(
                                                        value = skill.percentage.toFloat(),
                                                        onValueChange = { pct ->
                                                            val updatedList = skills.toMutableList()
                                                            updatedList[idx] = skill.copy(percentage = pct.toInt())
                                                            viewModel.updateCurrentResumeState(resume.copy(skillsJson = ResumeMoshiHelper.toJson(updatedList, Skill::class.java)))
                                                        },
                                                        valueRange = 10f..100f,
                                                        modifier = Modifier.weight(0.7f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            6 -> { // Extras Category Screen (Certs, Achievements, Languages, Hobbies, custom Section etc.)
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    SectionHeaderCard("Certifications, Metrics & Awards") {
                                        // Certifications
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            var newCertText by remember { mutableStateOf("") }
                                            Text("Certifications list: ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            certifications.forEachIndexed { idx, cert ->
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                    Text("• $cert", fontSize = 12.sp, modifier = Modifier.weight(1f))
                                                    IconButton(onClick = {
                                                        val updated = certifications.filterIndexed { i, _ -> i != idx }
                                                        viewModel.updateCurrentResumeState(resume.copy(certificationsJson = ResumeMoshiHelper.toJson(updated, String::class.java)))
                                                    }) { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                                }
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                                OutlinedTextField(
                                                    value = newCertText,
                                                    onValueChange = { newCertText = it },
                                                    label = { Text("Add Certification") },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Text,
                                                        imeAction = ImeAction.Done
                                                    ),
                                                    keyboardActions = KeyboardActions(
                                                        onDone = {
                                                            if (newCertText.isNotBlank()) {
                                                                val updated = certifications + newCertText
                                                                viewModel.updateCurrentResumeState(resume.copy(certificationsJson = ResumeMoshiHelper.toJson(updated, String::class.java)))
                                                                newCertText = ""
                                                            }
                                                            focusManager.clearFocus()
                                                        }
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                IconButton(onClick = {
                                                    if (newCertText.isNotBlank()) {
                                                        val updated = certifications + newCertText
                                                        viewModel.updateCurrentResumeState(resume.copy(certificationsJson = ResumeMoshiHelper.toJson(updated, String::class.java)))
                                                        newCertText = ""
                                                    }
                                                }) { Icon(Icons.Default.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                            }
                                        }
                                    }

                                    SectionHeaderCard("Achievements & Milestones") {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            var newAchieveText by remember { mutableStateOf("") }
                                            Text("Key Achievements:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            achievements.forEachIndexed { idx, ac ->
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                    Text("• $ac", fontSize = 12.sp, modifier = Modifier.weight(1f))
                                                    IconButton(onClick = {
                                                        val updated = achievements.filterIndexed { i, _ -> i != idx }
                                                        viewModel.updateCurrentResumeState(resume.copy(achievementsJson = ResumeMoshiHelper.toJson(updated, String::class.java)))
                                                    }) { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                                }
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                                OutlinedTextField(
                                                    value = newAchieveText,
                                                    onValueChange = { newAchieveText = it },
                                                    label = { Text("Add Achievement") },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Text,
                                                        imeAction = ImeAction.Done
                                                     ),
                                                    keyboardActions = KeyboardActions(
                                                        onDone = {
                                                            if (newAchieveText.isNotBlank()) {
                                                                val updated = achievements + newAchieveText
                                                                viewModel.updateCurrentResumeState(resume.copy(achievementsJson = ResumeMoshiHelper.toJson(updated, String::class.java)))
                                                                newAchieveText = ""
                                                            }
                                                            focusManager.clearFocus()
                                                        }
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                IconButton(onClick = {
                                                    if (newAchieveText.isNotBlank()) {
                                                        val updated = achievements + newAchieveText
                                                        viewModel.updateCurrentResumeState(resume.copy(achievementsJson = ResumeMoshiHelper.toJson(updated, String::class.java)))
                                                        newAchieveText = ""
                                                    }
                                                }) { Icon(Icons.Default.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                            }
                                        }
                                    }

                                    SectionHeaderCard("Academic Languages & Hobbies") {
                                        // Simple mapping fields
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            var newLang by remember { mutableStateOf("") }
                                            Text("Languages:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                languages.forEach { lng ->
                                                    SuggestionChip(onClick = {}, label = { Text(lng) })
                                                }
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                OutlinedTextField(
                                                    value = newLang,
                                                    onValueChange = { newLang = it },
                                                    label = { Text("Language keyword") },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Text,
                                                        imeAction = ImeAction.Done
                                                    ),
                                                    keyboardActions = KeyboardActions(
                                                        onDone = {
                                                            if (newLang.isNotBlank()) {
                                                                viewModel.updateCurrentResumeState(resume.copy(languagesJson = ResumeMoshiHelper.toJson(languages + newLang, String::class.java)))
                                                                newLang = ""
                                                            }
                                                            focusManager.clearFocus()
                                                        }
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                IconButton(onClick = {
                                                    if (newLang.isNotBlank()) {
                                                        viewModel.updateCurrentResumeState(resume.copy(languagesJson = ResumeMoshiHelper.toJson(languages + newLang, String::class.java)))
                                                        newLang = ""
                                                    }
                                                }) { Icon(Icons.Default.AddCircle, contentDescription = null) }
                                            }
                                        }
                                    }
                                    
                                    SectionHeaderCard("References & Custom Section") {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            var customSecTitle by remember { mutableStateOf("") }
                                            var customSecContent by remember { mutableStateOf("") }
                                            
                                            Text("Direct References & Endorsements", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            // References UI can be simple fields
                                            OutlinedTextField(
                                                value = if (customSections.isNotEmpty()) customSections.first().sectionTitle else "",
                                                onValueChange = { title ->
                                                    val obj = CustomSection(sectionTitle = title, content = if (customSections.isNotEmpty()) customSections.first().content else "")
                                                    viewModel.updateCurrentResumeState(resume.copy(customSectionsJson = ResumeMoshiHelper.toJson(listOf(obj), CustomSection::class.java)))
                                                },
                                                label = { Text("Custom Section Category") },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Text,
                                                    imeAction = ImeAction.Next
                                                ),
                                                keyboardActions = KeyboardActions(
                                                    onNext = { focusManager.moveFocus(FocusDirection.Next) }
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            OutlinedTextField(
                                                value = if (customSections.isNotEmpty()) customSections.first().content else "",
                                                onValueChange = { content ->
                                                    val obj = CustomSection(sectionTitle = if (customSections.isNotEmpty()) customSections.first().sectionTitle else "Highlights", content = content)
                                                    viewModel.updateCurrentResumeState(resume.copy(customSectionsJson = ResumeMoshiHelper.toJson(listOf(obj), CustomSection::class.java)))
                                                },
                                                label = { Text("Custom details content...") },
                                                minLines = 3,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Right Panel: Sticky Live Preview Canvas sheet (for landscape or tablet-like experience)
                AnimatedVisibility(
                    visible = isLivePreviewVisible,
                    enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.dividerColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Text("Live Canvas Sheet", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            FilledIconButton(
                                onClick = { isLivePreviewVisible = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Preview rendering
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                val scrollState = androidx.compose.foundation.rememberScrollState()
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White)
                                        .verticalScroll(scrollState)
                                ) {
                                    ResumePreviewRenderer(resume = resume, fontSizeAdjustment = -2)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeaderCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

val ColorScheme.dividerColor: Color
    @Composable
    get() = if (UserThemeState.isDarkTheme) Color(0xFF374151) else Color(0xFFE5E7EB)
