package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundDeepNavy
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryAccentAmber
import com.example.ui.theme.SecondaryViolet
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.PrepViewModel

@Composable
fun DailyChallengeScreen(
    viewModel: PrepViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val challengeQuestion by viewModel.dailyChallengeQuestion.collectAsState()
    val selectedOption by viewModel.dailyChallengeSelectedOption.collectAsState()
    val isAnswered by viewModel.dailyChallengeAnswered.collectAsState()
    val challengeState by viewModel.dailyChallengeState.collectAsState()

    if (challengeQuestion == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundDeepNavy),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "No challenge icon",
                    tint = TextMuted,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No daily challenge available today.",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
        }
        return
    }

    val question = challengeQuestion!!

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDeepNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = if (isAnswered) 160.dp else 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Title and header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Flash icon",
                    tint = PrimaryAccentAmber,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Daily Challenge",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Text(
                text = "Solve today's question to keep your study streak active!",
                color = TextMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Subject tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SecondaryViolet.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "SUBJECT · ${question.text.take(30)}",
                    color = SecondaryViolet,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Question Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
                border = BoxBorder()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = question.text,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Options list
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        question.options.forEachIndexed { optIdx, optionText ->
                            val isSelected = selectedOption == optIdx
                            
                            val isCorrect = optIdx == question.correctAnswerIndex
                            val isWrongSelected = isAnswered && isSelected && challengeState?.correct == false

                            val containerColor = when {
                                isAnswered && isCorrect -> CorrectGreen
                                isWrongSelected -> ErrorRed
                                isSelected -> PrimaryAccentAmber
                                else -> SurfaceNavy
                            }

                            val textColor = when {
                                isAnswered && isCorrect -> BackgroundDeepNavy
                                isWrongSelected -> Color.White
                                isSelected -> BackgroundDeepNavy
                                else -> Color.White
                            }

                            val circleBgColor = when {
                                isAnswered && isCorrect -> BackgroundDeepNavy.copy(alpha = 0.15f)
                                isWrongSelected -> Color.White.copy(alpha = 0.2f)
                                isSelected -> BackgroundDeepNavy.copy(alpha = 0.15f)
                                else -> BackgroundDeepNavy
                            }

                            val circleTextColor = when {
                                isAnswered && isCorrect -> BackgroundDeepNavy
                                isWrongSelected -> Color.White
                                isSelected -> BackgroundDeepNavy
                                else -> Color.White
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isAnswered) {
                                        viewModel.selectDailyChallengeOption(optIdx)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = containerColor),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected || (isAnswered && isCorrect)) containerColor else Color.White.copy(alpha = 0.05f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Letter Circle Indicator
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(circleBgColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = ('A' + optIdx).toString(),
                                            color = circleTextColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Text(
                                        text = optionText,
                                        color = textColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit answer CTA
                    if (!isAnswered) {
                        val hasSelection = selectedOption != null
                        Button(
                            onClick = { viewModel.submitDailyChallenge() },
                            enabled = hasSelection,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryAccentAmber,
                                contentColor = BackgroundDeepNavy,
                                disabledContainerColor = Color.White.copy(alpha = 0.05f),
                                disabledContentColor = TextMuted
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Submit Answer",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        // Solved banner status indicators
                        val correct = challengeState?.correct ?: false
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (correct) CorrectGreen.copy(alpha = 0.12f)
                                    else ErrorRed.copy(alpha = 0.12f)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (correct) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = "Status marks",
                                tint = if (correct) CorrectGreen else ErrorRed
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (correct) "Excellent! You picked the correct option." else "Daily challenge completed. See review explanation below.",
                                color = if (correct) CorrectGreen else ErrorRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Animated explanation card that slides up from bottom
        AnimatedVisibility(
            visible = isAnswered,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryAccentAmber.copy(alpha = 0.2f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SOLVED EXPLANATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAccentAmber,
                            letterSpacing = 1.2.sp
                        )
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CorrectGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            val correctLetter = ('A' + question.correctAnswerIndex).toString()
                            Text(
                                text = "CORRECT ANSWER: $correctLetter",
                                fontSize = 10.sp,
                                color = CorrectGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = question.explanation,
                        color = TextMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
