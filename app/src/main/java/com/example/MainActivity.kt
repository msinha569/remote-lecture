package com.example

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    LectureRemoteApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

private val CyberDarkBlue = Color(0xFF1C1B1F)           // Elegant ambient background
private val CyberSlate = Color(0xFF332D41)              // Elegant deep plum bar background
private val CyberCyan = Color(0xFFD0BCFF)               // Glowing lavender/purple primary
private val CyberPink = Color(0xFFCAC4D0)               // Elegant secondary text / silver
private val CyberGreen = Color(0xFFA7F3D0)              // Beautiful soft mint green success
private val CyberAmber = Color(0xFFFCD34D)              // Soft amber/gold warning

private val ElegantCardBg = Color(0xFF2B2930)           // M3 base surface container
private val ElegantBorder = Color(0xFF49454F)           // Dark slate/purple border
private val ElegantAccentDark = Color(0xFF381E72)       // Dark contrast color for text in buttons
private val ElegantContainerDark = Color(0xFF332D41)    // Secondary button backgrounds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectureRemoteApp(
    modifier: Modifier = Modifier,
    viewModel: RemoteViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showHelpSheet by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val triggerImpact = {
        if (state.useVibration) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBlue)
    ) {
        // Main split-screen layouts
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Status Strip
            StatusHeader(
                state = state,
                onSettingsClick = { showSettingsSheet = true },
                onHelpClick = { showHelpSheet = true }
            )

            // Remote Control Split-Pad Board (Left vs Right)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // LEFT REMOTE PAD (SKIP BACKWARD)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(1.dp, ElegantBorder, RoundedCornerShape(32.dp))
                            .clip(RoundedCornerShape(32.dp))
                            .testTag("left_skip_button")
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(
                                    bounded = true,
                                    radius = 350.dp,
                                    color = CyberCyan.copy(alpha = 0.25f)
                                ),
                                onClick = {
                                    triggerImpact()
                                    viewModel.sendCommand("LEFT")
                                }
                            )
                            .background(ElegantCardBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(ElegantContainerDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "Skip Backward",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Rewind",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.SansSerif
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "-10 seconds",
                                color = CyberPink,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.SansSerif
                            )
                            Text(
                                text = "TAP LEFT SCREEN",
                                color = CyberPink.copy(alpha = 0.4f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.SansSerif,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    // RIGHT REMOTE PAD (SKIP FORWARD)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(1.dp, ElegantBorder, RoundedCornerShape(32.dp))
                            .clip(RoundedCornerShape(32.dp))
                            .testTag("right_skip_button")
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(
                                    bounded = true,
                                    radius = 350.dp,
                                    color = CyberCyan.copy(alpha = 0.25f)
                                ),
                                onClick = {
                                    triggerImpact()
                                    viewModel.sendCommand("RIGHT")
                                }
                            )
                            .background(ElegantCardBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(ElegantContainerDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Skip Forward",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Forward",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.SansSerif
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+10 seconds",
                                color = CyberPink,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.SansSerif
                            )
                            Text(
                                text = "TAP RIGHT SCREEN",
                                color = CyberPink.copy(alpha = 0.4f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.SansSerif,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                // CENTER PLAY/PAUSE OVERLAY BUTTON - Styled as solid primary bubble with deep contrast
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("play_pause_button")
                        .size(80.dp)
                        .shadow(12.dp, CircleShape)
                        .border(1.dp, ElegantBorder, CircleShape)
                        .background(CyberCyan, CircleShape)
                        .clickable {
                            triggerImpact()
                            viewModel.sendCommand("PLAY_PAUSE")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.95f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    Box(
                        modifier = Modifier
                            .size((80f * pulseScale).dp)
                            .border(1.dp, CyberCyan.copy(alpha = 0.3f), CircleShape)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Play/Pause Spacebar Toggle",
                            tint = ElegantAccentDark,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "PAUSE",
                            color = ElegantAccentDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }

            // BOTTOM CONTROL PANELS STRIP & VOLUME
            BottomControlStrip(
                state = state,
                viewModel = viewModel,
                onSettingsOpen = { showSettingsSheet = true },
                onHelpOpen = { showHelpSheet = true },
                triggerFeedback = triggerImpact
            )
        }

        // SETTINGS & AUTO-DISCOVERY BOTTOM SHEET
        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                containerColor = CyberSlate,
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.2f)) }
            ) {
                SettingsPanelContent(
                    state = state,
                    viewModel = viewModel,
                    onDismiss = { showSettingsSheet = false },
                    triggerFeedback = triggerImpact
                )
            }
        }

        // QUICK-SETUP & PYTHON HOST CODE BOTTOM SHEET
        if (showHelpSheet) {
            ModalBottomSheet(
                onDismissRequest = { showHelpSheet = false },
                containerColor = CyberSlate,
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.2f)) }
            ) {
                HelpPanelContent(
                    state = state,
                    onDismiss = { showHelpSheet = false }
                )
            }
        }
    }
}

@Composable
fun StatusHeader(
    state: RemoteUiState,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberDarkBlue)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Lecture Remote",
                fontSize = 22.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif
            )
            
            // Connection sub-title label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                val dotColor = when (state.connectionStatus) {
                    is ConnectionStatus.Connected -> CyberGreen
                    is ConnectionStatus.Connecting -> CyberAmber
                    is ConnectionStatus.Disconnected -> Color.Gray
                }

                val desc = when (val cState = state.connectionStatus) {
                    is ConnectionStatus.Connected -> "Ready • ${cState.ip}"
                    is ConnectionStatus.Connecting -> "Connecting..."
                    is ConnectionStatus.Disconnected -> "Not Paired (Tap Help ↗)"
                }

                // Breathing animation for active status dot
                val transition = rememberInfiniteTransition("header_pulse")
                val alpha by transition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "header_alpha"
                )

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor.copy(alpha = if (state.connectionStatus is ConnectionStatus.Disconnected) 1f else alpha))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = desc,
                    fontSize = 13.sp,
                    color = CyberPink,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Action Buttons
        Row {
            IconButton(
                onClick = onHelpClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(CyberSlate, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Show Connection Guide",
                    tint = CyberCyan
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(CyberSlate, CircleShape)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Show Device Settings",
                    tint = CyberCyan
                )
            }
        }
    }
}

@Composable
fun BottomControlStrip(
    state: RemoteUiState,
    viewModel: RemoteViewModel,
    onSettingsOpen: () -> Unit,
    onHelpOpen: () -> Unit,
    triggerFeedback: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSlate),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp, top = 8.dp)
            .border(1.dp, ElegantBorder, RoundedCornerShape(28.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Volume Strip + Feedbacks
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Volume Controls Group
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = {
                            triggerFeedback()
                            viewModel.sendCommand("VOL_DOWN")
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeDown,
                            contentDescription = "Volume Down",
                            tint = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .width(1.dp)
                            .height(24.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                    )

                    IconButton(
                        onClick = {
                            triggerFeedback()
                            viewModel.sendCommand("MUTE")
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeMute,
                            contentDescription = "Toggle Mute",
                            tint = CyberCyan
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .width(1.dp)
                            .height(24.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                    )

                    IconButton(
                        onClick = {
                            triggerFeedback()
                            viewModel.sendCommand("VOL_UP")
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Volume Up",
                            tint = Color.White
                        )
                    }
                }

                // Keyboard Helper (Esc & Enter)
                Row {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        onClick = {
                            triggerFeedback()
                            viewModel.sendCommand("ESC") // Exit fullscreen
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = "ESC",
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        onClick = {
                            triggerFeedback()
                            viewModel.sendCommand("ENTER") // Click ok/play
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ENTER",
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Help Tips banner is simplified to direct setup tip if not connected
            if (state.connectionStatus is ConnectionStatus.Disconnected) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onHelpOpen() }
                        .border(1.dp, CyberCyberAmberTint, RoundedCornerShape(8.dp))
                        .background(CyberAmber.copy(alpha = 0.05f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Instruction Guidance Icon",
                        tint = CyberAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "First time? Click here to view host Python script setup code for your laptop.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Go",
                        tint = CyberAmber,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                // Command Feed string (history logs snapshot)
                val lastCommand = state.commandHistory.firstOrNull() ?: "[No commands sent yet]"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Recent Log Status Icon",
                            tint = CyberGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = lastCommand,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Text(
                        text = "LOGS",
                        fontSize = 9.sp,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clickable { onSettingsOpen() }
                            .border(1.dp, CyberCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsPanelContent(
    state: RemoteUiState,
    viewModel: RemoteViewModel,
    onDismiss: () -> Unit,
    triggerFeedback: () -> Unit
) {
    var ipInput by remember { mutableStateOf(state.laptopIp) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LAPTOP CONNECTION SETUP",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Sheet",
                    tint = Color.White.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Manual Input Area
        Text(
            text = "Laptop IP Address",
            fontSize = 11.sp,
            color = CyberCyan,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = ipInput,
                onValueChange = { ipInput = it },
                placeholder = { Text("e.g. 192.168.1.15", color = Color.White.copy(alpha = 0.3f)) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ip_input_field"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        if (ipInput.isNotEmpty()) {
                            viewModel.testConnection(ipInput)
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedContainerColor = CyberDarkBlue.copy(alpha = 0.6f),
                    unfocusedContainerColor = CyberDarkBlue.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    keyboardController?.hide()
                    triggerFeedback()
                    if (ipInput.isNotEmpty()) {
                        viewModel.testConnection(ipInput)
                    } else {
                        Toast.makeText(viewModel.getApplication(), "Please enter an IP", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .testTag("connect_button")
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Connect and verify connection status",
                    tint = CyberDarkBlue
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("PAIR", color = CyberDarkBlue, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Auto-Discovery Section
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberDarkBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = "Computer icon",
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AUTO-DISCOVER ON WIFI",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (state.isScanning) {
                        CircularProgressIndicator(
                            color = CyberCyan,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "SCAN NOW",
                            fontSize = 11.sp,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .clickable {
                                    triggerFeedback()
                                    viewModel.startAutoDiscovery()
                                }
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (state.discoveredDevices.isEmpty()) {
                    Text(
                        text = if (state.isScanning) "Searching subnet for computers running remote scripts..." 
                        else "No computers found on server port 5005 yet. Connect your laptop to the same Wi-Fi, run the Python host script, and launch a scan.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text(
                        text = "Tap a discovered laptop IP to pair instantly:",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        state.discoveredDevices.forEach { deviceIp ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .clickable {
                                        triggerFeedback()
                                        ipInput = deviceIp
                                        viewModel.setLaptopIp(deviceIp)
                                        Toast.makeText(viewModel.getApplication(), "Paired with $deviceIp!", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = "Active Server Icon",
                                        tint = CyberGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = deviceIp,
                                        fontSize = 13.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = "TAP TO PAIR",
                                    fontSize = 10.sp,
                                    color = CyberGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Vibration Preferences Settings Block
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Vibration,
                    contentDescription = "Tactile Vibration Icon",
                    tint = CyberCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VIBRATION FEEDBACK ON LECTURE CLICK",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Switch(
                checked = state.useVibration,
                onCheckedChange = {
                    triggerFeedback()
                    viewModel.setUseVibration(it)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyberCyan,
                    checkedTrackColor = CyberCyan.copy(alpha = 0.4f),
                    uncheckedThumbColor = Color.White.copy(alpha = 0.5f),
                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History logs Console
        Text(
            text = "LIVE TRANSMISSION LOGS",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Console Logs:",
                    fontSize = 10.sp,
                    color = CyberCyan,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "CLEAR CONSOLE",
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.3f),
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable { viewModel.clearLog() }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            
            if (state.commandHistory.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Waiting for remote click events...",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.3f),
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(state.commandHistory) { log ->
                        Text(
                            text = log,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private const val pythonScriptContent = """# ==========================================
# LECTURE REMOTE HOST SERVER SCRIPT
# Run this on your Laptop to receive Phone Commands!
# ==========================================
import socket
import sys
import os

print("=== Starting LectureRemote Host Assistant ===")
print("Determining requirements...")

try:
    import pyautogui
except ImportError:
    print("Installing PyAutoGUI library automatically to simulate keyboard events...")
    os.system('sh -c "pip install pyautogui" || pip install pyautogui || python3 -m pip install pyautogui || python -m pip install pyautogui')
    try:
        import pyautogui
    except ImportError:
        print("\n[ERROR] Direct pip install failed! Please run 'pip install pyautogui' inside your terminal manually.")
        sys.exit(1)

# Initialize UDP socket configuration
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

try:
    sock.bind(('0.0.0.0', 5005))
except Exception as e:
    print(f"\n[ERROR] Port 5005 is busy or blocked! Is another instance running? Detailed error: {e}")
    sys.exit(1)

# Try fetching laptop IP
try:
    hostname = socket.gethostname()
    laptop_ip = socket.gethostbyname(hostname)
except Exception:
    laptop_ip = "Unknown (Check your system Network settings)"

print("\n" + "="*50)
print(f" HOST RUNNING SUCCESSFULLY!")
print(f" TARGET PORT: 5005")
print(f" YOUR LAPTOP LOCAL IP: {laptop_ip}")
print("="*50)
print("\nConnect your Android phone to the SAME Wi-Fi network and start skipping lectures!")
print("Waiting for key skips...\n")

while True:
    try:
        data, addr = sock.recvfrom(1024)
        cmd = data.decode('utf-8').strip()
        print(f" -> Tapped Command: '{cmd}' from phone {addr[0]}")
        
        if cmd == "DISCOVER":
            # Direct automatic discovery acknowledgement
            sock.sendto(b"ACK_REMOTE", addr)
            print("    Replied to discovery ping automatically.")
        elif cmd == "LEFT":
            pyautogui.press('left') # Skips backwards 10s or 5s
        elif cmd == "RIGHT":
            pyautogui.press('right') # Skips forwards
        elif cmd == "PLAY_PAUSE":
            pyautogui.press('space') # Toggles Play/Pause
        elif cmd == "VOL_UP":
            pyautogui.press('volumeup') # Increment volume
        elif cmd == "VOL_DOWN":
            pyautogui.press('volumedown') # Decrement volume
        elif cmd == "MUTE":
            pyautogui.press('volumemute') # Mutes volume
        elif cmd == "ESC":
            pyautogui.press('escape') # Exit fullscreen modes
        elif cmd == "ENTER":
            pyautogui.press('enter') # Clicks focused items
    except KeyboardInterrupt:
        print("\nStopping LectureRemote Host Server...")
        break
    except Exception as e:
        print(f"Warning: Command failed to execute: {e}")
"""

@Composable
fun HelpPanelContent(
    state: RemoteUiState,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LAPTOP HOST SERVER SETUP",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Sheet",
                    tint = Color.White.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Skip lecture video playback wirelessly by running a tiny background listener script on your computer. Follow these steps:",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Timeline Step 1
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(CyberCyan),
                contentAlignment = Alignment.Center
            ) {
                Text("1", color = CyberDarkBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("COPY SCRIPT", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("Tap the button below to copy the complete self-installing Python host server code.", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(pythonScriptContent))
                        Toast.makeText(context, "Script code copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.12f)),
                    border = borderStrikeCyanBorder
                ) {
                    Icon(
                        imageVector = Icons.Default.CopyAll,
                        contentDescription = "Copy text icon",
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("COPY HOST PYTHON SCRIPT", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timeline Step 2
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(CyberCyan),
                contentAlignment = Alignment.Center
            ) {
                Text("2", color = CyberDarkBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("RUN ON LAPT0P", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("Create a text file named remote.py on your computer, paste the script into it, and execute it using terminal / command prompt:\npython remote.py", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timeline Step 3
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(CyberCyan),
                contentAlignment = Alignment.Center
            ) {
                Text("3", color = CyberDarkBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("CONNECT & CONTROL", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("Make sure your phone and laptop are on the SAME Wi-Fi network. Then open settings (⚙️) here in the app and tap 'SCAN NOW' to auto-pair. Once paired, taping anywhere on the phone screen will control your video player instantly!", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
            }
        }
    }
}

private val CyberCyberAmberTint = Color(0x22FFB300)
private val borderStrikeCyanBorder = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f))
