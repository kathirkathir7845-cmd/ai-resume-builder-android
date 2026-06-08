package com.example.ui.screen

import android.content.Context
import kotlinx.coroutines.launch
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.templates.*
import com.example.ui.viewmodel.ResumeViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    viewModel: ResumeViewModel,
    resumeId: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentResume by viewModel.currentResume.collectAsState()

    // Workspace tab options: "Templates", "Styling", "Sections"
    var activeConfigSubTab by remember { mutableStateOf(0) }
    val menuList = listOf("Choose Template", "Global Styling", "Section Preferences")

    // Theme Custom RGB Slider states (Infinite options)
    var customR by remember { mutableStateOf(30) }
    var customG by remember { mutableStateOf(58) }
    var customB by remember { mutableStateOf(138) }

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

    // Parse list helpers for custom orders
    val sectionNames = remember {
        mutableStateListOf(
            "Summary", "Experience", "Projects", "Skills", "Education",
            "Certifications", "Achievements", "Languages", "Hobbies", "References"
        )
    }

    // Load active resume visibility state
    val visibilityMap = remember(resume.sectionVisibilityJson) {
        try {
            val moshi = com.squareup.moshi.Moshi.Builder().build()
            val adapter = moshi.adapter<Map<String, Boolean>>(
                com.squareup.moshi.Types.newParameterizedType(Map::class.java, String::class.java, java.lang.Boolean::class.java)
            )
            adapter.fromJson(resume.sectionVisibilityJson)?.toMutableMap() ?: mutableMapOf()
        } catch (e: Exception) {
            mutableMapOf()
        }
    }

    // Map custom ordering if already set
    LaunchedEffect(resume.sectionOrderJson) {
        try {
            val moshi = com.squareup.moshi.Moshi.Builder().build()
            val adapter = moshi.adapter<List<String>>(
                com.squareup.moshi.Types.newParameterizedType(List::class.java, String::class.java)
            )
            val list = adapter.fromJson(resume.sectionOrderJson)
            if (!list.isNullOrEmpty()) {
                sectionNames.clear()
                sectionNames.addAll(list)
            }
        } catch (e: Exception) {
            // keep standard
        }
    }

    fun saveSectionsAndConfig(updatedOrder: List<String>, updatedVisibility: Map<String, Boolean>) {
        val moshi = com.squareup.moshi.Moshi.Builder().build()
        
        val orderAdapter = moshi.adapter<List<String>>(
            com.squareup.moshi.Types.newParameterizedType(List::class.java, String::class.java)
        )
        val orderStr = orderAdapter.toJson(updatedOrder)

        val visAdapter = moshi.adapter<Map<String, Boolean>>(
            com.squareup.moshi.Types.newParameterizedType(Map::class.java, String::class.java, java.lang.Boolean::class.java)
        )
        val visStr = visAdapter.toJson(updatedVisibility)

        viewModel.updateCurrentResumeState(
            resume.copy(
                sectionOrderJson = orderStr,
                sectionVisibilityJson = visStr
            )
        )
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(310.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Dashboard, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Text(
                                text = "Templates Library",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { scope.launch { drawerState.close() } }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Library")
                        }
                    }
                    Text(
                        text = "Switch custom styles instantly in real-time:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search Templates...", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    val categories = listOf("All") + ResumeTemplatesList.map { it.category }.distinct()
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        items(categories) { cat ->
                            val isSel = selectedCategoryFilter == cat
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedCategoryFilter = cat },
                                label = { Text(cat, fontSize = 10.sp) }
                            )
                        }
                    }

                    val filteredTemplates = ResumeTemplatesList.filter {
                        (selectedCategoryFilter == "All" || it.category == selectedCategoryFilter) &&
                        (it.name.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true))
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredTemplates) { preset ->
                            val isSelected = resume.templateId == preset.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("template_sidebar_item_${preset.id}")
                                    .clickable {
                                        viewModel.updateCurrentResumeState(resume.copy(templateId = preset.id))
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp, 72.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (preset.id == 7 || preset.id == 18) Color(0xFF1E293B)
                                                else if (preset.id == 10) Color(0xFFFFFDE7)
                                                else Color.White
                                            )
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                    ) {
                                        TemplateSchematicThumbnail(templateId = preset.id, themeColorHex = resume.themeColor)
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = preset.category.uppercase(),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = preset.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = preset.description,
                                            fontSize = 8.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 10.sp,
                                            maxLines = 3
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Visual Studio & Print", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        FilledTonalButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp).testTag("toggle_templates_sidebar")
                        ) {
                            Icon(Icons.Default.Dashboard, contentDescription = "Templates Library", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Templates Library", fontSize = 12.sp)
                        }

                        // PDF Download trigger
                        Button(
                            onClick = {
                                exportResumeToPdf(context, resume) { success, filePath ->
                                    if (success) {
                                        Toast.makeText(context, "PDF Download Successful! Saved to Downloads.", Toast.LENGTH_LONG).show()
                                        viewModel.updateCurrentResumeState(resume.copy(downloadHistoryCount = resume.downloadHistoryCount + 1))
                                    } else {
                                        Toast.makeText(context, "Export error: $filePath", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.testTag("preview_export_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
            
            // Sub Tabs Selection Row
            TabRow(selectedTabIndex = activeConfigSubTab) {
                menuList.forEachIndexed { idx, label ->
                    Tab(
                        selected = activeConfigSubTab == idx,
                        onClick = { activeConfigSubTab = idx },
                        text = { Text(label, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                    )
                }
            }

            // Interactive Editor Studio Sidebar mapping height
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                when (activeConfigSubTab) {
                    0 -> { // --- 25 unique templates slider ---
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Select template layout style:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                FilledTonalButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open Library", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            LazyRow(
                                modifier = Modifier.fillMaxWidth().testTag("templates_slider").weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                items(ResumeTemplatesList) { preset ->
                                    val isSelected = resume.templateId == preset.id
                                    Card(
                                        modifier = Modifier
                                            .width(135.dp)
                                            .fillMaxHeight()
                                            .clickable {
                                                viewModel.updateCurrentResumeState(resume.copy(templateId = preset.id))
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                        ),
                                        border = if (isSelected) {
                                            CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary))
                                        } else null
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = when (preset.category) {
                                                        "Modern" -> Icons.Default.TrendingUp
                                                        "Corporate" -> Icons.Default.Business
                                                        "Executive" -> Icons.Default.MilitaryTech
                                                        "Minimal" -> Icons.Default.FilterNone
                                                        "Student" -> Icons.Default.School
                                                        "ATS Friendly" -> Icons.Default.CheckCircle
                                                        "Dark Theme" -> Icons.Default.DarkMode
                                                        else -> Icons.Default.Dashboard
                                                    },
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(preset.category.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                            Text(preset.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                            Text(preset.description, fontSize = 8.sp, maxLines = 2, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), lineHeight = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> { // --- Styling: Preset colors & RGB Pickers ---
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Infinite color customization
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Custom Theme Picker (R/G/B sliders)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                val finalHex = String.format("#%02X%02X%02X", customR, customG, customB)
                                Box(
                                    modifier = Modifier
                                        .size(45.dp, 24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(android.graphics.Color.parseColor(finalHex)))
                                        .clickable {
                                            viewModel.updateCurrentResumeState(resume.copy(themeColor = finalHex))
                                        }
                                )
                            }
                            
                            // Color preset bubbles (50 precalculated themes)
                            Column {
                                Text("Or select from 52 Designer Color presets:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(ResumeColorThemes) { palette ->
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(palette.primary)
                                                .border(2.dp,Color.White, CircleShape)
                                                .clickable {
                                                    val hex = String.format("#%02X%02X%02X", 
                                                        (palette.primary.red * 255).toInt(),
                                                        (palette.primary.green * 255).toInt(),
                                                        (palette.primary.blue * 255).toInt()
                                                    )
                                                    viewModel.updateCurrentResumeState(resume.copy(themeColor = hex))
                                                }
                                        )
                                    }
                                }
                            }

                            // Predefined Fonts & sizes picker
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Fonts dropdown trigger
                                val familiesList = ResumeFonts.map { it.first }
                                var isFontsDropdownExpanded by remember { mutableStateOf(false) }
                                
                                Box(modifier = Modifier.weight(1.2f)) {
                                    OutlinedButton(
                                        onClick = { isFontsDropdownExpanded = true },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.FontDownload, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(resume.fontFamily, fontSize = 11.sp, maxLines = 1)
                                    }
                                    DropdownMenu(
                                        expanded = isFontsDropdownExpanded,
                                        onDismissRequest = { isFontsDropdownExpanded = false }
                                    ) {
                                        familiesList.forEach { fontName ->
                                            DropdownMenuItem(
                                                text = { Text(fontName, fontFamily = getFontFamily(fontName), fontSize = 13.sp) },
                                                onClick = {
                                                    viewModel.updateCurrentResumeState(resume.copy(fontFamily = fontName))
                                                    isFontsDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Font size sliders
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Font A-", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Slider(
                                        value = resume.fontSize.toFloat(),
                                        onValueChange = { viewModel.updateCurrentResumeState(resume.copy(fontSize = it.toInt())) },
                                        valueRange = 10f..22f,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text("A+", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    2 -> { // --- Section ordering and visibilities ---
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Click blocks to show/hide, or re-order timeline hierarchy:", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                            // Quick Checkboxes items
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(sectionNames) { sec ->
                                    val isVisible = visibilityMap[sec] != false
                                    FilterChip(
                                        selected = isVisible,
                                        onClick = {
                                            val currentVal = visibilityMap[sec] != false
                                            visibilityMap[sec] = !currentVal
                                            saveSectionsAndConfig(sectionNames, visibilityMap)
                                        },
                                        label = { Text(sec, fontSize = 10.sp) },
                                        leadingIcon = {
                                            if (isVisible) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(10.dp))
                                            }
                                        }
                                    )
                                }
                            }

                            // Sorters order row buttons
                            Text("Timeline sequence: ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                sectionNames.forEachIndexed { idx, sec ->
                                    Card(
                                        modifier = Modifier.padding(horizontal = 2.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(sec, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(3.dp))
                                            if (idx > 0) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowBack,
                                                    contentDescription = "Move left",
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clickable {
                                                            // Swap left
                                                            val tmp = sectionNames[idx]
                                                            sectionNames[idx] = sectionNames[idx - 1]
                                                            sectionNames[idx - 1] = tmp
                                                            saveSectionsAndConfig(sectionNames, visibilityMap)
                                                        }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Real-Time Resume Sheet Rendering canvas!
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val pageScrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(pageScrollState)
                    ) {
                        ResumePreviewRenderer(resume = resume)
                    }

                    // Contact QR Code representation overlay (ATS contact scanner)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.size(60.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                // Draw mock dynamic QR code lines on a canvas matching candidate coordinates!
                                androidx.compose.foundation.Canvas(modifier = Modifier.size(45.dp)) {
                                    val sizeSq = size.width
                                    val boxesCount = 11
                                    val step = sizeSq / boxesCount
                                    for (i in 0 until boxesCount) {
                                        for (j in 0 until boxesCount) {
                                            // Seed pseudo-random lines based on Name initials to look like custom QR code
                                            val hash = (resume.fullname.hashCode() + i * 31 + j * 17) % 5
                                            val isFilled = (i == 0 || i == boxesCount - 1 || j == 0 || j == boxesCount - 1) || 
                                                           (i in 2..4 && j in 2..4) || 
                                                           (hash == 0 || hash == 2)
                                            if (isFilled) {
                                                drawRect(
                                                    color = Color.Black,
                                                    topLeft = androidx.compose.ui.geometry.Offset(i * step, j * step),
                                                    size = androidx.compose.ui.geometry.Size(step, step)
                                                )
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
    }
    }
}

@Composable
fun TemplateSchematicThumbnail(templateId: Int, themeColorHex: String) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(themeColorHex))
    } catch (e: Exception) {
        Color(0xFF1E3A8A)
    }

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val isDark = templateId == 7 || templateId == 18
        val lineCol = if (isDark) Color(0xFF64748B) else Color(0xFFCBD5E1)
        val textCol = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

        when (templateId) {
            0 -> { // Modern Tech - Left accent bar, badges
                drawRect(color = accentColor, topLeft = androidx.compose.ui.geometry.Offset(4f, 4f), size = androidx.compose.ui.geometry.Size(4f, h - 8f))
                drawRect(color = textCol, topLeft = androidx.compose.ui.geometry.Offset(12f, 8f), size = androidx.compose.ui.geometry.Size(30f, 6f))
                drawRect(color = lineCol, topLeft = androidx.compose.ui.geometry.Offset(12f, 18f), size = androidx.compose.ui.geometry.Size(40f, 4f))
                drawCircle(color = accentColor, radius = 3f, center = androidx.compose.ui.geometry.Offset(15f, 32f))
                drawCircle(color = accentColor, radius = 3f, center = androidx.compose.ui.geometry.Offset(25f, 32f))
                drawCircle(color = accentColor, radius = 3f, center = androidx.compose.ui.geometry.Offset(35f, 32f))
                
                drawRect(color = lineCol, topLeft = androidx.compose.ui.geometry.Offset(12f, 42f), size = androidx.compose.ui.geometry.Size(40f, 3f))
                drawRect(color = lineCol, topLeft = androidx.compose.ui.geometry.Offset(12f, 50f), size = androidx.compose.ui.geometry.Size(35f, 3f))
                drawRect(color = lineCol, topLeft = androidx.compose.ui.geometry.Offset(12f, 58f), size = androidx.compose.ui.geometry.Size(38f, 3f))
            }
            1 -> { // Corporate Prestige - Dynamic top banner
                drawRect(color = accentColor, topLeft = androidx.compose.ui.geometry.Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(w, 15f))
                drawRect(color = Color.White, topLeft = androidx.compose.ui.geometry.Offset(6f, 4f), size = androidx.compose.ui.geometry.Size(25f, 3f))
                drawRect(color = Color.White, topLeft = androidx.compose.ui.geometry.Offset(6f, 9f), size = androidx.compose.ui.geometry.Size(35f, 2f))
                drawRect(color = textCol, topLeft = androidx.compose.ui.geometry.Offset(6f, 22f), size = androidx.compose.ui.geometry.Size(48f, 5f))
                drawRect(color = lineCol, topLeft = androidx.compose.ui.geometry.Offset(6f, 32f), size = androidx.compose.ui.geometry.Size(45f, 3f))
                drawRect(color = lineCol, topLeft = androidx.compose.ui.geometry.Offset(6f, 38f), size = androidx.compose.ui.geometry.Size(40f, 3f))
                drawRect(color = textCol, topLeft = androidx.compose.ui.geometry.Offset(6f, 48f), size = androidx.compose.ui.geometry.Size(48f, 5f))
                drawRect(color = lineCol, topLeft = androidx.compose.ui.geometry.Offset(6f, 58f), size = androidx.compose.ui.geometry.Size(45f, 3f))
            }
            2, 8, 16, 20 -> { // Executive templates - double borders/golden/ Bordeaux navy traditional
                drawLine(color = accentColor, start = androidx.compose.ui.geometry.Offset(4f, 4f), end = androidx.compose.ui.geometry.Offset(w - 4f, 4f), strokeWidth = 2f)
                drawLine(color = accentColor, start = androidx.compose.ui.geometry.Offset(4f, 8f), end = androidx.compose.ui.geometry.Offset(w - 4f, 8f), strokeWidth = 1f)
                drawRect(color = textCol, topLeft = androidx.compose.ui.geometry.Offset(15f, 15f), size = androidx.compose.ui.geometry.Size(30f, 5f))
                drawRect(color = lineCol, topLeft = androidx.compose.ui.geometry.Offset(10f, 24f), size = androidx.compose.ui.geometry.Size(40f, 3f))
                drawLine(color = lineCol, start = androidx.compose.ui.geometry.Offset(w / 2, 32f), end = androidx.compose.ui.geometry.Offset(w / 2, h - 8f), strokeWidth = 1f)
                drawRect(color = accentColor, topLeft = androidx.compose.ui.geometry.Offset(6f, 36f), size = androidx.compose.ui.geometry.Size(20f, 4f))
                drawRect(color = accentColor, topLeft = androidx.compose.ui.geometry.Offset(w - 26f, 46f), size = androidx.compose.ui.geometry.Size(20f, 4f))
            }
            3, 19, 21, 24 -> { // Minimalist
                drawRect(color = textCol, topLeft = androidx.compose.ui.geometry.Offset(6f, 8f), size = androidx.compose.ui.geometry.Size(25f, 5f))
                drawLine(color = lineCol, start = androidx.compose.ui.geometry.Offset(6f, 18f), end = androidx.compose.ui.geometry.Offset(w - 6f, 18f), strokeWidth = 1f)
                drawRect(color = lineCol, topLeft = androidx.compose.ui.geometry.Offset(6f, 24f), size = androidx.compose.ui.geometry.Size(48f, 3f))
                drawRect(color = lineCol, topLeft = androidx.compose.ui.geometry.Offset(6f, 32f), size = androidx.compose.ui.geometry.Size(48f, 3f))
                drawLine(color = lineCol, start = androidx.compose.ui.geometry.Offset(6f, 42f), end = androidx.compose.ui.geometry.Offset(w - 6f, 42f), strokeWidth = 1f)
                drawRect(color = accentColor, topLeft = androidx.compose.ui.geometry.Offset(6f, 48f), size = androidx.compose.ui.geometry.Size(35f, 4f))
            }
            5, 15 -> { // ATS Friendly
                for (y in 8..72 step 8) {
                    val lineW = if (y == 8 || y == 32 || y == 56) 35f else 48f
                    val color = if (y == 8 || y == 32 || y == 56) textCol else lineCol
                    drawRect(color = color, topLeft = androidx.compose.ui.geometry.Offset(6f, y.toFloat()), size = androidx.compose.ui.geometry.Size(lineW, 2f))
                }
            }
            6, 9, 14 -> { // Creative Split / Metropolitan Sidebar style
                drawRect(color = accentColor.copy(alpha = 0.85f), topLeft = androidx.compose.ui.geometry.Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(20f, h))
                drawCircle(color = Color.White, radius = 2f, center = androidx.compose.ui.geometry.Offset(10f, 12f))
                drawRect(color = Color.White, topLeft = androidx.compose.ui.geometry.Offset(4f, 25f), size = androidx.compose.ui.geometry.Size(12f, 2f))
                drawRect(color = Color.White, topLeft = androidx.compose.ui.geometry.Offset(4f, 32f), size = androidx.compose.ui.geometry.Size(12f, 2f))
                drawRect(color = Color.White, topLeft = androidx.compose.ui.geometry.Offset(4f, 39f), size = androidx.compose.ui.geometry.Size(12f, 2f))
                
                drawRect(color = textCol, topLeft = androidx.compose.ui.geometry.Offset(26f, 8f), size = androidx.compose.ui.geometry.Size(28f, 5f))
                drawRect(color = lineCol, topLeft = androidx.compose.ui.geometry.Offset(26f, 18f), size = androidx.compose.ui.geometry.Size(28f, 3f))
                drawRect(color = lineCol, topLeft = androidx.compose.ui.geometry.Offset(26f, 26f), size = androidx.compose.ui.geometry.Size(25f, 3f))
                drawRect(color = lineCol, topLeft = androidx.compose.ui.geometry.Offset(26f, 34f), size = androidx.compose.ui.geometry.Size(28f, 3f))
            }
            7, 18 -> { // Dark Theme & Cyberpunk - Slate / pitch black background with neon accents
                val neonCol = if (templateId == 18) Color(0xFF00F0FF) else accentColor
                drawRect(color = neonCol, topLeft = androidx.compose.ui.geometry.Offset(6f, 8f), size = androidx.compose.ui.geometry.Size(28f, 5f))
                drawLine(color = neonCol.copy(alpha = 0.5f), start = androidx.compose.ui.geometry.Offset(6f, 18f), end = androidx.compose.ui.geometry.Offset(w - 6f, 18f), strokeWidth = 1f)
                drawRect(color = lineCol, topLeft = androidx.compose.ui.geometry.Offset(6f, 24f), size = androidx.compose.ui.geometry.Size(48f, 2f))
                drawRect(color = lineCol, topLeft = androidx.compose.ui.geometry.Offset(6f, 30f), size = androidx.compose.ui.geometry.Size(45f, 2f))
                drawRect(color = neonCol, topLeft = androidx.compose.ui.geometry.Offset(6f, 40f), size = androidx.compose.ui.geometry.Size(20f, 4f))
            }
            11 -> { // Slate Block - Header blocks
                drawRect(color = textCol, topLeft = androidx.compose.ui.geometry.Offset(6f, 6f), size = androidx.compose.ui.geometry.Size(30f, 6f))
                drawRect(color = accentColor, topLeft = androidx.compose.ui.geometry.Offset(6f, 18f), size = androidx.compose.ui.geometry.Size(48f, 8f))
                drawRect(color = Color.White, topLeft = androidx.compose.ui.geometry.Offset(10f, 21f), size = androidx.compose.ui.geometry.Size(25f, 2f))
                drawRect(color = lineCol, topLeft = androidx.compose.ui.geometry.Offset(6f, 32f), size = androidx.compose.ui.geometry.Size(45f, 3f))
                drawRect(color = lineCol, topLeft = androidx.compose.ui.geometry.Offset(6f, 40f), size = androidx.compose.ui.geometry.Size(40f, 3f))
            }
            22 -> { // Brutalist Monochrome - thick borders
                drawRect(color = Color.Black, topLeft = androidx.compose.ui.geometry.Offset(4f, 4f), size = androidx.compose.ui.geometry.Size(w - 8f, 16f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
                drawRect(color = accentColor, topLeft = androidx.compose.ui.geometry.Offset(8f, 8f), size = androidx.compose.ui.geometry.Size(25f, 8f))
                drawRect(color = Color.Black, topLeft = androidx.compose.ui.geometry.Offset(4f, 26f), size = androidx.compose.ui.geometry.Size(w - 8f, 24f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
                drawRect(color = textCol, topLeft = androidx.compose.ui.geometry.Offset(8f, 30f), size = androidx.compose.ui.geometry.Size(30f, 4f))
                drawRect(color = lineCol, topLeft = androidx.compose.ui.geometry.Offset(8f, 38f), size = androidx.compose.ui.geometry.Size(40f, 3f))
            }
            else -> { // Standard timeline or customized defaults
                drawRect(color = accentColor, topLeft = androidx.compose.ui.geometry.Offset(6f, 8f), size = androidx.compose.ui.geometry.Size(30f, 5f))
                drawLine(color = lineCol, start = androidx.compose.ui.geometry.Offset(6f, 18f), end = androidx.compose.ui.geometry.Offset(w - 6f, 18f), strokeWidth = 1f)
                drawRect(color = textCol, topLeft = androidx.compose.ui.geometry.Offset(6f, 24f), size = androidx.compose.ui.geometry.Size(48f, 4f))
                drawRect(color = lineCol, topLeft = androidx.compose.ui.geometry.Offset(6f, 34f), size = androidx.compose.ui.geometry.Size(45f, 3f))
                drawRect(color = lineCol, topLeft = androidx.compose.ui.geometry.Offset(6f, 42f), size = androidx.compose.ui.geometry.Size(40f, 3f))
            }
        }
    }
}

// --- Native High Quality PDF Compiler Adapter ---
fun exportResumeToPdf(context: Context, resume: Resume, onResult: (Boolean, String) -> Unit) {
    val document = PdfDocument()
    
    // Page dimension: Letter size standard in pixels (72 points/inch)
    val pageInfo = PdfDocument.PageInfo.Builder(612, 792, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas

    val textPaint = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = resume.fontSize.toFloat().coerceIn(12f, 16f)
        isAntiAlias = true
    }

    val primaryTitlePaint = Paint().apply {
        color = android.graphics.Color.parseColor(resume.themeColor)
        textSize = resume.fontSize.toFloat() + 8f
        isFakeBoldText = true
        isAntiAlias = true
    }

    val subtitlePaint = Paint().apply {
        color = android.graphics.Color.DKGRAY
        textSize = resume.fontSize.toFloat() - 1f
        isAntiAlias = true
    }

    val linePaint = Paint().apply {
        color = android.graphics.Color.LTGRAY
        strokeWidth = 1f
    }

    var yPos = 50f

    try {
        // Render Header details
        canvas.drawText(resume.fullname.ifBlank { "Client Name" }, 40f, yPos, primaryTitlePaint)
        yPos += 20f
        
        if (resume.objective.isNotBlank()) {
            canvas.drawText(resume.objective, 40f, yPos, subtitlePaint)
            yPos += 15f
        }

        // Contact info line mapping
        val contactList = mutableListOf<String>()
        if (resume.phone.isNotBlank()) contactList.add("Phone: ${resume.phone}")
        if (resume.email.isNotBlank()) contactList.add("Email: ${resume.email}")
        if (resume.address.isNotBlank()) contactList.add("Address: ${resume.address}")
        
        if (contactList.isNotEmpty()) {
            val contactStr = contactList.joinToString("  |  ")
            canvas.drawText(contactStr, 40f, yPos, textPaint)
            yPos += 15f
        }
        
        canvas.drawLine(40f, yPos, 572f, yPos, linePaint)
        yPos += 25f

        // --- Render Summary block ---
        if (resume.summary.isNotBlank()) {
            canvas.drawText("PROFILE SUMMARY", 40f, yPos, primaryTitlePaint.apply { textSize = resume.fontSize.toFloat() + 2f })
            yPos += 15f
            
            // Text word-wrap logic to fit width
            val summaryText = resume.summary
            val words = summaryText.split(" ")
            var lineStr = ""
            for (word in words) {
                if (textPaint.measureText("$lineStr $word") < 500f) {
                    lineStr = if (lineStr.isEmpty()) word else "$lineStr $word"
                } else {
                    canvas.drawText(lineStr, 40f, yPos, textPaint)
                    yPos += 14f
                    lineStr = word
                }
            }
            if (lineStr.isNotEmpty()) {
                canvas.drawText(lineStr, 40f, yPos, textPaint)
                yPos += 14f
            }
            yPos += 15f
        }

        // --- Render Experience Block ---
        val experiences = ResumeMoshiHelper.fromJson(resume.experienceJson, Experience::class.java)
        if (experiences.isNotEmpty()) {
            canvas.drawText("WORK EXPERIENCE", 40f, yPos, primaryTitlePaint)
            yPos += 15f
            experiences.forEach { exp ->
                val lineH = "${exp.role} - ${exp.company} (${exp.duration})"
                canvas.drawText(lineH, 40f, yPos, textPaint.apply { isFakeBoldText = true })
                yPos += 12f
                
                // Words wrapping simple layout
                val desc = exp.description
                val words = desc.split(" ")
                var lineStr = ""
                for (word in words) {
                    if (textPaint.measureText("$lineStr $word") < 500f) {
                        lineStr = if (lineStr.isEmpty()) word else "$lineStr $word"
                    } else {
                        canvas.drawText(lineStr, 50f, yPos, textPaint.apply { isFakeBoldText = false })
                        yPos += 14f
                        lineStr = word
                    }
                }
                if (lineStr.isNotEmpty()) {
                    canvas.drawText(lineStr, 50f, yPos, textPaint.apply { isFakeBoldText = false })
                    yPos += 14f
                }
                yPos += 10f
            }
            yPos += 10f
        }

        // --- Render Skills Grid Block ---
        val skills = ResumeMoshiHelper.fromJson(resume.skillsJson, Skill::class.java)
        if (skills.isNotEmpty()) {
            canvas.drawText("TECHNICAL & SOFT SKILLS", 40f, yPos, primaryTitlePaint)
            yPos += 15f
            val skillsLine = skills.joinToString { "${it.name} (${it.percentage}%)" }
            
            val words = skillsLine.split(" ")
            var lineStr = ""
            for (word in words) {
                if (textPaint.measureText("$lineStr $word") < 500f) {
                    lineStr = if (lineStr.isEmpty()) word else "$lineStr $word"
                } else {
                    canvas.drawText(lineStr, 40f, yPos, textPaint)
                    yPos += 14f
                    lineStr = word
                }
            }
            if (lineStr.isNotEmpty()) {
                canvas.drawText(lineStr, 40f, yPos, textPaint)
                yPos += 14f
            }
            yPos += 15f
        }

        // --- Render Education Block ---
        val education = ResumeMoshiHelper.fromJson(resume.educationJson, Education::class.java)
        if (education.isNotEmpty()) {
            canvas.drawText("ACADEMIC PREPARATIONS", 40f, yPos, primaryTitlePaint)
            yPos += 15f
            education.forEach { edu ->
                val lineE = "${edu.degree} · ${edu.school} (${edu.duration})"
                canvas.drawText(lineE, 40f, yPos, textPaint)
                yPos += 12f
                if (edu.gpa.isNotEmpty()) {
                    canvas.drawText("GPA: ${edu.gpa}", 40f, yPos, subtitlePaint)
                    yPos += 12f
                }
            }
        }

        document.finishPage(page)

        // Save PDF doc into download folders
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val formatTitle = resume.fullname.replace(" ", "_").lowercase()
        val file = File(downloadsDir, "resume_${formatTitle}_export.pdf")
        
        val outputStream = FileOutputStream(file)
        document.writeTo(outputStream)
        document.close()
        outputStream.close()
        
        onResult(true, file.absolutePath)
    } catch (e: Exception) {
        document.close()
        onResult(false, e.localizedMessage ?: "Unknown compilation error on PdfDocument device buffer.")
    }
}
