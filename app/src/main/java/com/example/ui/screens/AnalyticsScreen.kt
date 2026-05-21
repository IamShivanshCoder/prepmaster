package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundDeepNavy
import com.example.ui.theme.CustomGrey
import com.example.ui.theme.PrimaryAccentAmber
import com.example.ui.theme.SecondaryViolet
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.PrepViewModel

@Composable
fun AnalyticsScreen(
    viewModel: PrepViewModel,
    modifier: Modifier = Modifier
) {
    val attempts by viewModel.allAttempts.collectAsState()

    // 1. Calculate overall stats based on attempts list
    val totalSolved = attempts.size
    
    val overallAccuracy = remember(attempts) {
        if (attempts.isEmpty()) 72 else { // default realistic placeholder if no attempts
            val correct = attempts.sumOf { it.score }
            val total = attempts.sumOf { it.totalQuestions }
            if (total == 0) 72 else (correct * 100) / total
        }
    }

    val avgTimePerQSec = remember(attempts) {
        if (attempts.isEmpty()) 42 else {
            val totalTime = attempts.sumOf { it.timeSpentSeconds }
            val totalQ = attempts.sumOf { it.totalQuestions }
            if (totalQ == 0) 42 else totalTime / totalQ
        }
    }

    val bestSubject = remember(attempts) {
        if (attempts.isEmpty()) "Physics" else {
            val scoreBySubj = attempts.groupBy { it.subject }
                .mapValues { (_, value) ->
                    val totalC = value.sumOf { it.score }
                    val totalQ = value.sumOf { it.totalQuestions }
                    if (totalQ == 0) 0f else totalC.toFloat() / totalQ
                }
            scoreBySubj.maxByOrNull { it.value }?.key ?: "Mathematics"
        }
    }

    val weakestSubject = remember(attempts) {
        if (attempts.isEmpty()) "Chemistry" else {
            val scoreBySubj = attempts.groupBy { it.subject }
                .mapValues { (_, value) ->
                    val totalC = value.sumOf { it.score }
                    val totalQ = value.sumOf { it.totalQuestions }
                    if (totalQ == 0) 0f else totalC.toFloat() / totalQ
                }
            scoreBySubj.minByOrNull { it.value }?.key ?: "Chemistry"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDeepNavy)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Performance Analytics",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Visualizing your JEE & Board study metrics",
            fontSize = 13.sp,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 5 Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Overall Accuracy",
                value = "$overallAccuracy%",
                color = PrimaryAccentAmber,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Avg Time / Q",
                value = "$avgTimePerQSec s",
                color = SecondaryViolet,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Best Subject",
                value = bestSubject,
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Weakest Topic",
                value = weakestSubject,
                color = Color(0xFFEF4444),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        StatCard(
            title = "Questions Solved",
            value = "${attempts.sumOf { it.totalQuestions }.ifZero(120)} Qs",
            color = Color(0xFF3B82F6),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Chart 1: Topic-wise accuracy Bar Chart
        Text(
            text = "ACCURACY BY ACC REDUCTION",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryAccentAmber,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
            border = BoxBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Topic-wise accuracy broken down",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bar chart drawing on Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    val topics = listOf(
                        Pair("Mechanics", 84f),
                        Pair("Kinetics", 72f),
                        Pair("Organic", 63f),
                        Pair("Calculus", 90f),
                        Pair("Algebra", 78f)
                    )

                    val barWidth = 24.dp.toPx()
                    val spacing = (size.width - (barWidth * topics.size)) / (topics.size + 1)

                    topics.forEachIndexed { index, (topic, pct) ->
                        val x = spacing + index * (barWidth + spacing)
                        val barHeight = (pct / 100f) * size.height

                        // Draw golden bar
                        drawRoundRect(
                            color = PrimaryAccentAmber,
                            topLeft = Offset(x, size.height - barHeight),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }

                // Bar Labels Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val topics = listOf("Mech", "Kin", "Org", "Calc", "Alg")
                    topics.forEach { name ->
                        Text(
                            text = name,
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(36.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Chart 2: Accuracy Over last 30 days Line Graph
        Text(
            text = "30-DAY PRACTICE TRACKS",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryAccentAmber,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
            border = BoxBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Historical test performance margins",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Winding golden line on custom Canvas with dark grid lines
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    val points = listOf(65f, 72f, 68f, 80f, 75f, 87f, 83f, 92f)
                    val stepX = size.width / (points.size - 1)
                    val path = Path()

                    // Grid Horizontal lines
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = (size.height / gridLines) * i
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Line construction
                    points.forEachIndexed { idx, pct ->
                        val x = idx * stepX
                        val y = size.height - (pct / 100f * size.height)
                        if (idx == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    // Draw faint amber gradient shape underneath line graph
                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        color = PrimaryAccentAmber.copy(alpha = 0.08f)
                    )

                    // Draw golden curve line
                    drawPath(
                        path = path,
                        color = PrimaryAccentAmber,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Draw golden joints circular dots
                    points.forEachIndexed { idx, pct ->
                        val x = idx * stepX
                        val y = size.height - (pct / 100f * size.height)
                        drawCircle(
                            color = PrimaryAccentAmber,
                            radius = 3.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "30d ago", color = TextMuted, fontSize = 9.sp)
                    Text(text = "15d ago", color = TextMuted, fontSize = 9.sp)
                    Text(text = "Today", color = PrimaryAccentAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Heatmap: GitHub style daily practice squares
        Text(
            text = "CONSISTENCY HEATMAP",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryAccentAmber,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
            border = BoxBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Daily practice contribution intensity",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Custom grid of squares representing a calendar month
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val rows = 5
                    val cols = 15
                    
                    // We simulate random intense values around typical study intervals
                    for (r in 0 until rows) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (c in 0 until cols) {
                                val intensity = remember { (0..3).random() }
                                val tileColor = when (intensity) {
                                    1 -> PrimaryAccentAmber.copy(alpha = 0.2f)
                                    2 -> PrimaryAccentAmber.copy(alpha = 0.5f)
                                    3 -> PrimaryAccentAmber // Super heavy study day
                                    else -> Color.White.copy(alpha = 0.05f) // Rest day
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(tileColor)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Less", color = TextMuted, fontSize = 9.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.size(10.dp).background(Color.White.copy(alpha = 0.05f)))
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(modifier = Modifier.size(10.dp).background(PrimaryAccentAmber.copy(alpha = 0.3f)))
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(modifier = Modifier.size(10.dp).background(PrimaryAccentAmber))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "More Study", color = PrimaryAccentAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
        border = BoxBorder()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = title, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(text = value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Inline fallback utilities
private fun Int.ifZero(default: Int): Int = if (this == 0) default else this
private val Int.avgTimePerQSecs: Int get() = this
