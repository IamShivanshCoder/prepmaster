package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.PdfItemDetail
import com.example.ui.theme.BackgroundDeepNavy
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.PrimaryAccentAmber
import com.example.ui.theme.SecondaryViolet
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.PrepViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PapersLibraryScreen(
    viewModel: PrepViewModel,
    modifier: Modifier = Modifier
) {
    val allPdfs by viewModel.allPdfs.collectAsState()
    val bookmarkedByDb by viewModel.bookmarkedPdfs.collectAsState()
    val attempts by viewModel.allAttempts.collectAsState()
    val isLibraryLoading by viewModel.isLibraryLoading.collectAsState()

    // Top Exam Filters
    var selectedExamTab by remember { mutableStateOf("JEE") }
    val examOptions = listOf("JEE", "NEET", "Boards", "SAT")

    // Dynamic Sub-filters based on selected tab
    val subFilterOptions = remember(selectedExamTab) {
        when (selectedExamTab) {
            "JEE" -> listOf("All JEE", "JEE Mains", "JEE Advanced")
            "NEET" -> listOf("All NEET", "NEET UG", "NEET PG")
            "Boards" -> listOf("All Boards", "CBSE XII", "CBSE X", "ICSE XII", "ICSE X")
            "SAT" -> listOf("All SAT", "SAT General", "SAT Math")
            else -> listOf("All")
        }
    }
    var selectedSubFilter by remember(selectedExamTab) { mutableStateOf(subFilterOptions.first()) }

    // Subject Filter Chips
    var selectedSubjectChip by remember { mutableStateOf("All") }
    val subjectChips = listOf("All", "Physics", "Chemistry", "Maths", "Biology")

    // Year Slider Range
    var yearSliderValue by remember { mutableStateOf(2010f..2025f) }

    // Bookmark helpers
    val bookmarkedIds = remember(bookmarkedByDb) { bookmarkedByDb.map { it.pdfItem.id }.toSet() }
    val attemptedIds = remember(attempts) { attempts.map { it.pdfId }.toSet() }

    // Reactive filtering calculations
    val filteredPdfs = remember(allPdfs, selectedExamTab, selectedSubFilter, selectedSubjectChip, yearSliderValue) {
        allPdfs.filter { detail ->
            val pdf = detail.pdfItem

            // 1. Subject constraint
            val subjectMatch = if (selectedSubjectChip == "All") true else {
                pdf.subject.lowercase().contains(selectedSubjectChip.lowercase())
            }

            // 2. Year constraint
            val parsedYear = pdf.year.toIntOrNull() ?: 2025
            val yearMatch = parsedYear in yearSliderValue.start.toInt()..yearSliderValue.endInclusive.toInt()

            // 3. Exam Tabs matching (We have sample papers containing keyword, or all as fallback)
            val examMatch = when (selectedExamTab) {
                "JEE" -> pdf.title.lowercase().contains("jee") || pdf.subject != "Biology"
                "NEET" -> pdf.title.lowercase().contains("neet") || pdf.subject == "Biology" || pdf.subject == "Chemistry"
                "Boards" -> pdf.title.lowercase().contains("board") || pdf.title.lowercase().contains("mock") || pdf.title.lowercase().contains("national")
                "SAT" -> pdf.title.lowercase().contains("sat") || pdf.subject == "Mathematics" || pdf.subject == "Computer Science"
                else -> true
            }

            // 4. Sub filters
            val cleanSub = selectedSubFilter.lowercase()
            val subMatch = if (cleanSub.startsWith("all")) true else {
                pdf.title.lowercase().contains(cleanSub) || pdf.category.lowercase().contains(cleanSub)
            }

            subjectMatch && yearMatch && examMatch && subMatch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDeepNavy)
            .padding(16.dp)
    ) {
        // Headers Page
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Papers Library",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Dynamic Counter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(PrimaryAccentAmber.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${filteredPdfs.size} papers indexed",
                    color = PrimaryAccentAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Top Filter Pills (JEE NEET Boards SAT)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            examOptions.forEach { opt ->
                val isSelected = selectedExamTab == opt
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            if (isSelected) PrimaryAccentAmber else SurfaceNavy
                        )
                        .clickable { selectedExamTab = opt }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = opt,
                        color = if (isSelected) BackgroundDeepNavy else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sub-filter row below which changes dynamically per tab selected
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            subFilterOptions.forEach { subOpt ->
                val isSelected = selectedSubFilter == subOpt
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) SecondaryViolet.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.03f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) SecondaryViolet else Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedSubFilter = subOpt }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = subOpt,
                        color = if (isSelected) Color.White else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subject Horizontal scroll chips inside column
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            subjectChips.forEach { chipName ->
                val isSelected = selectedSubjectChip == chipName
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) PrimaryAccentAmber.copy(alpha = 0.15f) else Color.Transparent
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) PrimaryAccentAmber else Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedSubjectChip = chipName }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = chipName,
                        color = if (isSelected) PrimaryAccentAmber else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Year range labels above center
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${yearSliderValue.start.toInt()} – ${yearSliderValue.endInclusive.toInt()}",
                color = PrimaryAccentAmber,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        RangeSlider(
            value = yearSliderValue,
            onValueChange = { range -> yearSliderValue = range },
            valueRange = 2010f..2025f,
            steps = 14,
            colors = SliderDefaults.colors(
                activeTrackColor = PrimaryAccentAmber,
                inactiveTrackColor = Color.White.copy(alpha = 0.12f),
                thumbColor = PrimaryAccentAmber,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Labels: "2010" left, "2025" right
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "2010",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "${filteredPdfs.size} papers found",
                color = TextMuted,
                fontSize = 11.sp
            )

            Text(
                text = "2025",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Card Grid: 2 columns on mobile device layout
        if (isLibraryLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryAccentAmber)
            }
        } else if (filteredPdfs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Empty icon",
                        tint = TextMuted,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No exam papers matched these active filters.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredPdfs) { detail ->
                    val paper = detail.pdfItem
                    val isBookmarked = bookmarkedIds.contains(paper.id)
                    val isAttempted = attemptedIds.contains(paper.id)

                    val docType = remember(paper.category, paper.title) {
                        val cat = paper.category.lowercase()
                        val tit = paper.title.lowercase()
                        when {
                            cat.contains("notes") || tit.contains("notes") -> "NOTES"
                            cat.contains("formula") || tit.contains("formula") || tit.contains("sheets") -> "FORMULA SHEET"
                            cat.contains("mock") || tit.contains("mock") || cat.contains("test") -> "MOCK TEST"
                            else -> "PAST PAPER"
                        }
                    }
                    val docColor = when (docType) {
                        "PAST PAPER" -> PrimaryAccentAmber
                        "NOTES" -> SecondaryViolet
                        "FORMULA SHEET" -> Color(0xFF0EA5E9) // Teal
                        "MOCK TEST" -> CorrectGreen
                        else -> PrimaryAccentAmber
                    }

                    // Card block
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isAttempted) CorrectGreen.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f)
                        )
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Left border colored accent
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(4.dp)
                                    .background(docColor)
                            )

                            // Main card contents box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .drawBehind {
                                        if (isAttempted) {
                                            // Draw checkmark background watermark
                                            drawCircle(
                                                color = CorrectGreen.copy(alpha = 0.04f),
                                                radius = size.minDimension / 3f,
                                                center = center
                                            )
                                        }
                                    }
                                    .padding(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Year, docType badge and bookmark row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(
                                                        if (isAttempted) CorrectGreen.copy(alpha = 0.15f)
                                                        else PrimaryAccentAmber.copy(alpha = 0.15f)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = paper.year,
                                                    fontSize = 9.sp,
                                                    color = if (isAttempted) CorrectGreen else PrimaryAccentAmber,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            // Doc Type small badge chip
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(docColor.copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = docType,
                                                    fontSize = 8.sp,
                                                    color = docColor,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        // Bookmark Icon Toggle
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Filled.Star else Icons.Filled.StarBorder,
                                            contentDescription = "Bookmark",
                                            tint = if (isBookmarked) PrimaryAccentAmber else TextMuted,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clickable {
                                                    viewModel.toggleBookmark(paper.id, isBookmarked)
                                                }
                                        )
                                    }

                                    // Subject text
                                    Text(
                                        text = paper.subject,
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    // Title and duration
                                    Text(
                                        text = paper.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = "${paper.category} · ${paper.size}",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )

                                // Start Paper CTA or Reviewed indicator
                                if (isAttempted) {
                                    // ✓ Review (ghost button)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.03f))
                                            .clickable {
                                                viewModel.startExamAttempt(paper.id, paper.title, paper.subject)
                                            }
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Solved",
                                            tint = CorrectGreen,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Review Solved",
                                            fontSize = 10.sp,
                                            color = CorrectGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    // Start Paper (amber/gold fill)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(PrimaryAccentAmber)
                                            .clickable {
                                                viewModel.openPaper(paper.id, paper.title, paper.subject)
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Start Paper",
                                            fontSize = 10.sp,
                                            color = BackgroundDeepNavy,
                                            fontWeight = FontWeight.Bold
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
