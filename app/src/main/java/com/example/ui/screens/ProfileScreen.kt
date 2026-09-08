package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TurkishAirlinesAppState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: TurkishAirlinesAppState,
    onLanguageSelected: (String) -> Unit,
    onSimulateAction: (String) -> Unit, // delay, gate_change, boarding_announcement, payment_success, payment_fail, offline
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedLanguage by remember { mutableStateOf("English") }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showSimulationPanel by remember { mutableStateOf(false) }

    val languages = listOf("English", "Türkçe", "Deutsch", "Français", "Español", "Italiano", "Русский", "العربية", "日本語")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Upper Title
        Text(
            text = "PROFILE & SETTINGS",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Branded Profile Header
            Card(
                modifier = Modifier.fillMaxWidth().testTag("profile_user_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, TKSilverBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(TKRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${state.user?.firstName?.firstOrNull() ?: 'A'}${state.user?.lastName?.firstOrNull() ?: 'M'}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "${state.user?.firstName ?: "Alex"} ${state.user?.lastName ?: "Morgan"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Miles&Smiles ID: ${state.user?.milesAndSmilesNumber ?: "TK84250119"}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TKGold.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("ELITE PLUS MEMBER", color = TKGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Text("PREFERENCES & CONFIG", fontWeight = FontWeight.Bold, fontSize = 14.sp)

            // Preference items list
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, TKSilverBorder)
            ) {
                Column {
                    ProfilePrefItem(
                        icon = Icons.Default.Language,
                        title = "Language Selection",
                        subtitle = selectedLanguage,
                        onClick = { showLanguageDialog = true }
                    )
                    HorizontalDivider()
                    ProfilePrefItem(
                        icon = Icons.Default.Fingerprint,
                        title = "Biometric Login Secure",
                        subtitle = "Enabled (Face ID / Fingerprint)",
                        onClick = {}
                    )
                    HorizontalDivider()
                    ProfilePrefItem(
                        icon = Icons.Default.PrivacyTip,
                        title = "Privacy & Security",
                        subtitle = "Manage secure preferences",
                        onClick = {}
                    )
                }
            }

            Text("HELP & SUPPORT DESK", fontWeight = FontWeight.Bold, fontSize = 14.sp)

            // Support Center
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, TKSilverBorder)
            ) {
                Column {
                    ProfilePrefItem(
                        icon = Icons.Default.HelpOutline,
                        title = "FAQs & Help Guides",
                        subtitle = "Baggage counter guidelines, refunds",
                        onClick = {}
                    )
                    HorizontalDivider()
                    ProfilePrefItem(
                        icon = Icons.Default.PhoneCallback,
                        title = "Contact Support Desk",
                        subtitle = "24/7 Priority Elite Plus helpline",
                        onClick = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // OPEN QA SIMULATION SHEET BUTTON
            Button(
                onClick = { showSimulationPanel = !showSimulationPanel },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("simulation_menu_trigger"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Construction, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("OPEN QA SIMULATION PANEL", fontWeight = FontWeight.Bold)
            }

            // QA Simulation panel body
            AnimatedVisibility(
                visible = showSimulationPanel,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("simulation_panel_sheet"),
                    colors = CardDefaults.cardColors(containerColor = TKLightGrey),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "QA SIMULATION WORKBENCH",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TKRed
                        )
                        Text(
                            "Simulate live changes and mock conditions to inspect real-time notifications in state.",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        SimulationButton(label = "Simulate 2h Flight Delay") {
                            onSimulateAction("delay")
                        }
                        SimulationButton(label = "Simulate Gate Change to Gate B18") {
                            onSimulateAction("gate_change")
                        }
                        SimulationButton(label = "Trigger Boarding Announcement") {
                            onSimulateAction("boarding")
                        }
                        SimulationButton(label = "Enforce Offline Cache Mode") {
                            onSimulateAction("offline")
                        }
                        SimulationButton(label = "Simulate Payment Failure Scenario") {
                            onSimulateAction("payment_fail")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Language dialogue
        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = { Text("Select Language") },
                confirmButton = {
                    Button(onClick = { showLanguageDialog = false }) {
                        Text("Dismiss")
                    }
                },
                text = {
                    Column {
                        languages.forEach { lang ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedLanguage = lang
                                        onLanguageSelected(lang)
                                        showLanguageDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(lang, fontWeight = FontWeight.SemiBold)
                                if (selectedLanguage == lang) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = TKRed)
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ProfilePrefItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(TKLightGrey),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = TKRed, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
        }

        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}

@Composable
fun SimulationButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(40.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(label, color = TKBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}
