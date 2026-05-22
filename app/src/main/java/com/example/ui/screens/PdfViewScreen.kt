package com.example.ui.screens

import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.BackgroundDeepNavy
import com.example.ui.theme.PrimaryAccentAmber
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.PrepViewModel
import java.net.URLEncoder
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewScreen(
    viewModel: PrepViewModel,
    modifier: Modifier = Modifier
) {
    val detail by viewModel.activePdfDetail.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // Let's run a timer for study duration in timed mode!
    var timerSeconds by remember { mutableStateOf(1200) } // 20 minutes countdown!
    var showTimeUpDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (timerSeconds > 0) {
            kotlinx.coroutines.delay(1000)
            timerSeconds--
        }
        showTimeUpDialog = true
    }

    if (showTimeUpDialog) {
        AlertDialog(
            onDismissRequest = { showTimeUpDialog = false; viewModel.navigateBack() },
            title = { Text("Time's Up!", color = Color.White) },
            text = { Text("Your 20-minute study session has ended.", color = TextMuted) },
            confirmButton = {
                TextButton(onClick = { showTimeUpDialog = false; viewModel.navigateBack() }) {
                    Text("Go Back", color = PrimaryAccentAmber)
                }
            },
            containerColor = SurfaceNavy
        )
    }

    if (detail == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDeepNavy),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryAccentAmber)
        }
        return
    }

    val item = detail!!.pdfItem

    val encodedUrl = remember(item.url) {
        try {
            URLEncoder.encode(item.url, "UTF-8")
        } catch (e: Exception) {
            item.url
        }
    }
    val finalUrl = "https://docs.google.com/viewer?url=$encodedUrl&embedded=true"

    val minutes = timerSeconds / 60
    val seconds = timerSeconds % 60
    val timeString = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = item.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.navigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Timer indicator
                    Surface(
                        color = Color(0xFF13132B),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = timeString,
                            color = if (timerSeconds < 60) Color.Red else PrimaryAccentAmber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D0D1F),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        contentWindowInsets = WindowInsets(0.dp) // Edge to edge
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDeepNavy)
                .padding(innerPadding)
        ) {
            if (!isError) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isLoading = true
                                    isError = false
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    super.onReceivedError(view, request, error)
                                    if (request?.isForMainFrame == true) {
                                        isError = true
                                        isLoading = false
                                    }
                                }
                            }
                            webViewInstance = this
                            loadUrl(finalUrl)
                        }
                    },
                    update = {
                        webViewInstance = it
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundDeepNavy.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryAccentAmber)
                }
            }

            if (isError) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .background(BackgroundDeepNavy),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Could not load PDF. Check connection.",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            isError = false
                            isLoading = true
                            webViewInstance?.loadUrl(finalUrl)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryAccentAmber,
                            contentColor = Color(0xFF0A0A1A)
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry")
                    }
                }
            }
        }
    }
}
