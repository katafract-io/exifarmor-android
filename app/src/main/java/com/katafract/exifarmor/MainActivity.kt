package com.katafract.exifarmor

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.katafract.exifarmor.ui.HomeScreen
import com.katafract.exifarmor.ui.PreviewScreen
import com.katafract.exifarmor.ui.ResultsScreen
import com.katafract.exifarmor.ui.theme.ExifArmorTheme
import com.katafract.exifarmor.viewmodel.MainViewModel
import com.katafract.exifarmor.viewmodel.Screen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle incoming intents (share/send)
        handleIntent(intent)

        setContent {
            ExifArmorTheme {
                MainScreenContent(viewModel, activity = this@MainActivity)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent?) {
        if (intent == null) return

        when (intent.action) {
            android.content.Intent.ACTION_SEND -> {
                val uri = intent.getParcelableExtra<Uri>(android.content.Intent.EXTRA_STREAM)
                if (uri != null) {
                    viewModel.loadPhotos(listOf(uri))
                }
            }

            android.content.Intent.ACTION_SEND_MULTIPLE -> {
                val uris = intent.getParcelableArrayListExtra<Uri>(android.content.Intent.EXTRA_STREAM)
                if (uris != null && uris.isNotEmpty()) {
                    viewModel.loadPhotos(uris)
                }
            }
        }
    }
}

@Composable
private fun MainScreenContent(
    viewModel: MainViewModel,
    activity: ComponentActivity,
) {
    val screen = viewModel.screen.collectAsState()
    val photoList = viewModel.photoList.collectAsState()
    val stripResults = viewModel.stripResults.collectAsState()
    val stripOptions = viewModel.stripOptions.collectAsState()
    val processingProgress = viewModel.processingProgress.collectAsState()
    val isPro = viewModel.isPro.collectAsState()
    val error = viewModel.error.collectAsState()
    val showUpgradeSheet = viewModel.showUpgradeSheet.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Show error snackbar
    LaunchedEffect(error.value) {
        error.value?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short,
                )
                viewModel.clearError()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            when (screen.value) {
                Screen.HOME -> {
                    HomeScreen(
                        isPro = isPro.value,
                        onPhotosSelected = { uris ->
                            viewModel.loadPhotos(uris)
                        },
                        onUpgradeClick = {
                            viewModel.launchBilling(activity)
                        },
                    )
                }

                Screen.PREVIEW -> {
                    PreviewScreen(
                        photos = photoList.value,
                        currentOptions = stripOptions.value,
                        isPro = isPro.value,
                        onOptionsChanged = { options ->
                            viewModel.updateOptions(options)
                        },
                        onStrip = {
                            viewModel.startStrip()
                        },
                        onBack = {
                            viewModel.reset()
                        },
                    )
                }

                Screen.PROCESSING -> {
                    ProcessingScreen(progress = processingProgress.value)
                }

                Screen.DONE -> {
                    ResultsScreen(
                        results = stripResults.value,
                        onScanAgain = {
                            viewModel.reset()
                        },
                        onShare = {
                            viewModel.shareResults(activity)
                        },
                    )
                }
            }

            // Upgrade sheet
            if (showUpgradeSheet.value) {
                Dialog(
                    onDismissRequest = { viewModel.dismissUpgrade() },
                    properties = DialogProperties(
                        usePlatformDefaultWidth = false,
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true,
                    ),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.32f))
                            .padding(16.dp),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(0.7f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            // Close button
                            IconButton(
                                onClick = { viewModel.dismissUpgrade() },
                                modifier = Modifier.align(Alignment.End),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close",
                                )
                            }

                            Text(
                                text = "Unlock Pro",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )

                            // This is a stub; actual upgrade UI would go here
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun ProcessingScreen(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = "Processing...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.padding(horizontal = 32.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackgroundVariant,
            )
        }
    }
}
