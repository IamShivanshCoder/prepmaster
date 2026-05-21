package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundDeepNavy
import com.example.ui.theme.PrimaryAccentAmber
import com.example.ui.theme.SecondaryViolet
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.PrepViewModel

@Composable
fun ExamAttemptScreen(
    viewModel: PrepViewModel,
    modifier: Modifier = Modifier
) {
    val activePdfDetail by viewModel.activePdfDetail.collectAsState()
    val examQuestions by viewModel.examQuestions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedOptions by viewModel.examSelectedOptions.collectAsState()
    val flaggedSet by viewModel.examFlaggedQuestions.collectAsState()
    val secondsRemaining by viewModel.examTimeRemaining.collectAsState()

    var isDrawerOpen by remember { mutableStateOf(false) }

    val examTitle = activePdfDetail?.pdfItem?.title ?: "Board Exam Practice Series"
    val subject = activePdfDetail?.pdfItem?.subject ?: "Applied Sciences"

    val currentQuestion = examQuestions.getOrNull(currentIndex)
    val totalCount = examQuestions.size

    // Time Formatter
    val formattedTime = remember(secondsRemaining) {
        val mins = secondsRemaining / 60
        val secs = secondsRemaining % 60
        String.format("%02d:%02d", mins, secs)
    }
    val alarmActive = secondsRemaining < 300 // Amber warning if under 5 minutes

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDeepNavy)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceNavy)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Exam and Subject (Left)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = { isDrawerOpen = !isDrawerOpen }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Toggle Grid Drawer",
                            tint = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Text(
                            text = examTitle,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subject,
                            color = PrimaryAccentAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Countdown Timer (Center)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (alarmActive) PrimaryAccentAmber.copy(alpha = 0.15f)
                            else Color.White.copy(alpha = 0.05f)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formattedTime,
                        color = if (alarmActive) PrimaryAccentAmber else Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // End Test Button (Right)
                Button(
                    onClick = { viewModel.endExamAttempt() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryAccentAmber,
                        contentColor = BackgroundDeepNavy
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(text = "End Test", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Main Workspace Row (Supports Side Drawer sliding overlay layout)
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                
                // Left Grid Menu Drawer - Collapsible width!
                if (isDrawerOpen) {
                    Card(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(180.dp),
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "QUESTION GRID",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(totalCount) { idx ->
                                    val isCurrent = idx == currentIndex
                                    val isAnswered = selectedOptions.containsKey(idx)
                                    val isFlagged = flaggedSet.contains(idx)

                                    Box(
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when {
                                                    isAnswered -> PrimaryAccentAmber // Attempted = Amber fill
                                                    isCurrent -> Color.White.copy(alpha = 0.15f)
                                                    else -> BackgroundDeepNavy // Unattempted = Dark Card
                                                }
                                            )
                                            .border(
                                                width = if (isFlagged) 2.dp else 1.dp,
                                                color = when {
                                                    isFlagged -> PrimaryAccentAmber // Outlined if flagged
                                                    isCurrent -> Color.White
                                                    else -> Color.White.copy(alpha = 0.08f)
                                                },
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { viewModel.selectExamQuestionIndex(idx) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${idx + 1}",
                                            color = if (isAnswered) BackgroundDeepNavy else Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Main Content Area
                if (currentQuestion == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Loading exam question materials...", color = TextMuted)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Question Card
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Question ${currentIndex + 1} of $totalCount",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryAccentAmber
                                )

                                if (flaggedSet.contains(currentIndex)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(PrimaryAccentAmber.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "FLAGGED FOR REVIEW",
                                            fontSize = 9.sp,
                                            color = PrimaryAccentAmber,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = currentQuestion.text,
                                fontSize = 16.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // MCQ Options as full-width list
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                currentQuestion.options.forEachIndexed { optIdx, optionText ->
                                    val isSelected = selectedOptions[currentIndex] == optIdx
                                    
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.selectExamOption(optIdx) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) PrimaryAccentAmber else SurfaceNavy
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = 1.dp,
                                            color = if (isSelected) PrimaryAccentAmber else Color.White.copy(alpha = 0.05f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Alphabet Label: A, B, C, D
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected) BackgroundDeepNavy.copy(alpha = 0.2f)
                                                        else BackgroundDeepNavy
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = ('A' + optIdx).toString(),
                                                    color = if (isSelected) BackgroundDeepNavy else Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(16.dp))

                                            Text(
                                                text = optionText,
                                                color = if (isSelected) BackgroundDeepNavy else Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom Action Row: Previous · Flag for Review · Next
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Previous CTA
                            OutlinedButton(
                                onClick = { viewModel.previousExamQuestion() },
                                enabled = currentIndex > 0,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Prev icon", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Previous", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Flag for Review Button
                            OutlinedButton(
                                onClick = { viewModel.toggleExamFlagQuestion() },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryAccentAmber.copy(alpha = 0.3f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryAccentAmber),
                                modifier = Modifier.height(44.dp)
                            ) {
                                val isFlagged = flaggedSet.contains(currentIndex)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isFlagged) Icons.Filled.Flag else Icons.Outlined.Flag,
                                        contentDescription = "Flag review icon",
                                        tint = PrimaryAccentAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Flag", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Next CTA
                            val isLast = currentIndex == totalCount - 1
                            Button(
                                onClick = {
                                    if (isLast) viewModel.endExamAttempt() else viewModel.nextExamQuestion()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLast) SecondaryViolet else PrimaryAccentAmber,
                                    contentColor = if (isLast) Color.White else BackgroundDeepNavy
                                ),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isLast) "Finish Exam" else "Next",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (!isLast) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.ChevronRight, contentDescription = "Next icon", modifier = Modifier.size(16.dp))
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
