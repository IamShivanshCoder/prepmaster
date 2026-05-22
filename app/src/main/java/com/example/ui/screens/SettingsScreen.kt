package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundDeepNavy
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.PrimaryAccentAmber
import com.example.ui.theme.SecondaryViolet
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.PrepViewModel
import com.example.ui.viewmodel.SyncState

@Composable
fun SettingsScreen(
    viewModel: PrepViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeSession by viewModel.activeSession.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val configUrlInput by viewModel.configUrlInput.collectAsState()
    val whitelistedEmailsSet by viewModel.whitelistedEmails.collectAsState()
    val streakStats by viewModel.dailyStreakStats.collectAsState()
    val challengeNotificationEnabled by viewModel.challengeNotificationEnabled.collectAsState()

    var newMailInput by remember { mutableStateOf("") }
    var showAdminPanel by remember { mutableStateOf(false) }
    
    // Notifications toggle
    val currentStreak = streakStats?.currentStreak ?: 0

    // Auto-clear sync state after 4 seconds
    LaunchedEffect(syncState) {
        if (syncState is SyncState.Success || syncState is SyncState.Error) {
            kotlinx.coroutines.delay(4000)
            viewModel.clearSyncState()
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
            text = "Profile & Settings",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Configure study preferences and access controllers",
            fontSize = 13.sp,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // User profile summary card (Section 1)
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(SecondaryViolet.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Avatar",
                        tint = SecondaryViolet,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeSession?.displayName ?: "Candidate Student",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = activeSession?.email ?: "spam.iamshivanshcoder@gmail.com",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                // Sign Out Action Button
                IconButton(
                    onClick = {
                        viewModel.logout()
                        Toast.makeText(context, "Sign out completed", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Sign out action trigger",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Active Streak Stat Row (Section 2)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
            border = BoxBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔥", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "$currentStreak Days Active",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Current study momentum",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PrimaryAccentAmber.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Active",
                        color = PrimaryAccentAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Toggle Button "Challenge Reminder Notifications" (Section 3)
        Text(
            text = "PREFERENCES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Notifications bell",
                        tint = PrimaryAccentAmber,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Daily Challenge Reminders",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Remind me before streak expiry",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = challengeNotificationEnabled,
                    onCheckedChange = { viewModel.toggleNotificationSettings() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BackgroundDeepNavy,
                        checkedTrackColor = PrimaryAccentAmber,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.08f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // System Admin Tools Segment (Section 4 — Only active if admin)
        val isAdminUser = activeSession?.role == "admin" || 
                activeSession?.email?.trim()?.lowercase() == "spam.iamshivanshcoder@gmail.com" ||
                activeSession?.email?.trim()?.lowercase() == "exammanager@gmail.com"

        if (isAdminUser) {
            Text(
                text = "ADMINISTRATIVE ACCESS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryAccentAmber,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAdminPanel = !showAdminPanel },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
                border = BoxBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin panel shield",
                            tint = PrimaryAccentAmber,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "App Administration",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Icon(
                        imageVector = if (showAdminPanel) Icons.Default.ExpandLess else Icons.Default.ArrowForward,
                        contentDescription = "Arrow indicator",
                        tint = PrimaryAccentAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = showAdminPanel,
                enter = fadeIn() + expandVertically(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryAccentAmber.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Dynamic JSON Syllabus Database Sync",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Saves and extracts study PDFs + Whitelist hidden from end user.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // URL input string
                    OutlinedTextField(
                        value = configUrlInput,
                        onValueChange = { viewModel.updateConfigUrlInput(it) },
                        label = { Text("Remote Database Configuration Link") },
                        placeholder = { Text("https://example.com/syllabus.json") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryAccentAmber,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.08f)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sync action button
                    Button(
                        onClick = { 
                            viewModel.syncDatabase() 
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryAccentAmber,
                            contentColor = BackgroundDeepNavy
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(text = "Sync Database & Whitelist", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Display sync states
                    when (val currentStatus = syncState) {
                        is SyncState.Syncing -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = PrimaryAccentAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "Syncing with cloud hosts...", color = PrimaryAccentAmber, fontSize = 11.sp)
                            }
                        }
                        is SyncState.Success -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "✓ ${currentStatus.message}", color = CorrectGreen, fontSize = 11.sp)
                        }
                        is SyncState.Error -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "❌ Error: ${currentStatus.error}", color = Color(0xFFEF4444), fontSize = 11.sp)
                        }
                        else -> {}
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Whitelist Student Email manager text field & list
                    Text(
                        text = "Add Student to Whitelist",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Students must log in using an authorized Google email.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newMailInput,
                            onValueChange = { newMailInput = it },
                            placeholder = { Text("aspirant@gmail.com") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryAccentAmber,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.08f)
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (newMailInput.contains("@")) {
                                    viewModel.addLocalWhitelistedEmail(newMailInput)
                                    Toast.makeText(context, "Added!", Toast.LENGTH_SHORT).show()
                                    newMailInput = ""
                                } else {
                                    Toast.makeText(context, "Enter valid email", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SecondaryViolet,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(54.dp)
                        ) {
                            Text(text = "Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Whitelisted Student Accounts (${whitelistedEmailsSet.size})",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Scrollable list of current whitelisted student emails
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundDeepNavy),
                        border = BoxBorder()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (whitelistedEmailsSet.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "No current local whitelisted emails override", color = TextMuted, fontSize = 11.sp)
                                }
                            } else {
                                whitelistedEmailsSet.forEach { email ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.02f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = email,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )

                                        IconButton(
                                            onClick = {
                                                viewModel.removeLocalWhitelistedEmail(email)
                                                Toast.makeText(context, "Removed from whitelist", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove student email override",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(16.dp)
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

        Spacer(modifier = Modifier.height(32.dp))
    }
}
