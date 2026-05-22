package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.ui.theme.CorrectGreen
import androidx.compose.foundation.border
import com.example.ui.viewmodel.PrepViewModel

@Composable
fun DashboardScreen(
    viewModel: PrepViewModel,
    modifier: Modifier = Modifier
) {
    val session by viewModel.activeSession.collectAsState()
    val attempts by viewModel.allAttempts.collectAsState()
    val streakStats by viewModel.dailyStreakStats.collectAsState()
    val allPdfs by viewModel.allPdfs.collectAsState()

    // Calculated metrics
    val solvedCount = attempts.size
    val avgAccuracy = remember(attempts) {
        if (attempts.isEmpty()) 0 else {
            val totalCorrect = attempts.sumOf { it.score }
            val totalQ = attempts.sumOf { it.totalQuestions }
            if (totalQ == 0) 0 else (totalCorrect * 100) / totalQ
        }
    }
    val currentStreak = streakStats?.currentStreak ?: 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDeepNavy)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top Greeting Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Good morning, ${session?.displayName ?: "Shivansh"}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = session?.email ?: "spam.iamshivanshcoder@gmail.com",
                    fontSize = 13.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Three Stat Pills in a Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Streak Pill
            StatPill(
                icon = "🔥",
                title = "Day Streak",
                value = "$currentStreak days",
                modifier = Modifier.weight(1f)
            )
            // Papers Solved Pill
            StatPill(
                icon = "📝",
                title = "Papers Solved",
                value = "$solvedCount papers",
                modifier = Modifier.weight(1f)
            )
            // Accuracy Pill
            StatPill(
                icon = "🎯",
                title = "Avg Accuracy",
                value = "$avgAccuracy%",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Today's Challenge Section
        Text(
            text = "TODAY'S CHALLENGE",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = PrimaryAccentAmber,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
            border = BoxBorder()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SecondaryViolet.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Physics · Mechanics",
                            color = SecondaryViolet,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Avg time: 1.5 min",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "A block of mass 2 kg rests on a rough horizontal drag surface. Determine static equilibrium balances...",
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.navigateTo("challenge") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryAccentAmber,
                        contentColor = BackgroundDeepNavy
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(
                        text = "Solve Now",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Weekly Streak Dots Section
        Text(
            text = "STREAK PROGRESSION",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = PrimaryAccentAmber,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
            border = BoxBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val days = listOf("M", "T", "W", "T", "F", "S", "S")
                val completed = listOf(
                    streakStats?.mon ?: false,
                    streakStats?.tue ?: false,
                    streakStats?.wed ?: false,
                    streakStats?.thu ?: false,
                    streakStats?.fri ?: false,
                    streakStats?.sat ?: false,
                    streakStats?.sun ?: false
                )

                val todayIdx = remember {
                    val dayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
                    when (dayOfWeek) {
                        java.util.Calendar.MONDAY -> 0
                        java.util.Calendar.TUESDAY -> 1
                        java.util.Calendar.WEDNESDAY -> 2
                        java.util.Calendar.THURSDAY -> 3
                        java.util.Calendar.FRIDAY -> 4
                        java.util.Calendar.SATURDAY -> 5
                        java.util.Calendar.SUNDAY -> 6
                        else -> 0
                    }
                }

                days.forEachIndexed { idx, day ->
                    val isToday = idx == todayIdx
                    val isCompleted = completed[idx]
                    val isMissed = idx < todayIdx && !isCompleted

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCompleted -> PrimaryAccentAmber
                                        isMissed -> Color(0xFF323246)
                                        else -> Color.White.copy(alpha = 0.05f)
                                    }
                                )
                                .then(
                                    if (isToday) {
                                        Modifier.border(width = 2.dp, color = PrimaryAccentAmber, shape = CircleShape)
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed mark",
                                    tint = BackgroundDeepNavy,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else if (isMissed) {
                                Text(
                                    text = "✕",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = day,
                            color = when {
                                isCompleted -> PrimaryAccentAmber
                                isToday -> PrimaryAccentAmber
                                isMissed -> TextMuted.copy(alpha = 0.5f)
                                else -> TextMuted
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Papers Highlight Area
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECENT PAPERS",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryAccentAmber,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "View All",
                fontSize = 12.sp,
                color = PrimaryAccentAmber,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { viewModel.navigateTo("library") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal scrolling past papers
        if (allPdfs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No study papers indexed.", color = TextMuted)
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(allPdfs.take(5)) { detail ->
                    val paper = detail.pdfItem
                    
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

                    Card(
                        modifier = Modifier
                            .width(225.dp)
                            .clickable { viewModel.openPaper(paper.id, paper.title, paper.subject) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
                        border = BoxBorder()
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Left border colored accent
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(4.dp)
                                    .background(docColor)
                            )

                            // Main body of Card
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(PrimaryAccentAmber.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = paper.year,
                                                fontSize = 9.sp,
                                                color = PrimaryAccentAmber,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Badge
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

                                    Text(
                                        text = paper.size,
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = paper.title,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.height(34.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = paper.subject,
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        text = "Start Paper →",
                                        color = PrimaryAccentAmber,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable {
                                            viewModel.openPaper(paper.id, paper.title, paper.subject)
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

@Composable
fun StatPill(
    icon: String,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
        border = BoxBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = title,
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Global aesthetic card border helper to matches dark luxury theme
@Composable
fun BoxBorder() = androidx.compose.foundation.BorderStroke(
    width = 1.dp,
    color = Color.White.copy(alpha = 0.05f)
)
