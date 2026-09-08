package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TurkishAirlinesAppState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilesScreen(
    state: TurkishAirlinesAppState,
    onTopUpWallet: (Double, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var subTab by remember { mutableStateOf("MILES") } // MILES, WALLET
    var selectedCurrency by remember { mutableStateOf("GBP") } // GBP, TRY, EUR, USD
    var showTopUpDialog by remember { mutableStateOf(false) }
    var topUpAmountStr by remember { mutableStateOf("100") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Upper Title Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "LOYALTY & WALLET",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // Switch sub-tabs (Miles&Smiles vs TK Wallet)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(TKLightGrey)
                    .padding(2.dp)
            ) {
                listOf("MILES", "WALLET").forEach { tab ->
                    val isSel = subTab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.secondary else Color.Transparent)
                            .clickable { subTab = tab }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tab,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (subTab == "MILES") {
            // --- MILES & SMILES DASHBOARD (Section 25 & 26) ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Elite Plus Dashboard Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("loyalty_tier_card"),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = TKBlack)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            TKGraphite,
                                            TKBlack
                                        )
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MILES&SMILES",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(TKGold.copy(alpha = 0.2f))
                                        .border(1.dp, TKGold, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("ELITE PLUS", color = TKGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text("TOTAL MILES", color = Color.LightGray, fontSize = 11.sp)
                            Text(
                                text = "84,250",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Progress gauge to maintain Elite Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("STATUS MILES: 23,400", color = Color.LightGray, fontSize = 11.sp)
                                Text("TARGET: 40,000", color = TKGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { 23400 / 40000.0f },
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = TKGold,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                        }
                    }
                }

                // Award Quick actions
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionPill(icon = Icons.Default.ConfirmationNumber, label = "Award Tickets")
                        QuickActionPill(icon = Icons.Default.Upgrade, label = "Upgrades")
                        QuickActionPill(icon = Icons.Default.LocalOffer, label = "Offers")
                    }
                }

                // Miles Transactions History
                item {
                    Text(
                        "MILES TRANSACTIONS HISTORY",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    // Static list or Mock ledger logs (Section 26)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, TKSilverBorder)
                    ) {
                        Column {
                            MilesLedgerItem(amount = "+8,420", desc = "Flight TK1988 LHR-IST", date = "12 OCT", isEarned = true)
                            HorizontalDivider()
                            MilesLedgerItem(amount = "+4,100", desc = "Flight TK1991 IST-LHR", date = "19 SEP", isEarned = true)
                            HorizontalDivider()
                            MilesLedgerItem(amount = "-25,000", desc = "Award Ticket Redeem", date = "01 AUG", isEarned = false)
                            HorizontalDivider()
                            MilesLedgerItem(amount = "+1,200", desc = "Partner Car Hire", date = "14 JUL", isEarned = true)
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        } else {
            // --- TK WALLET SCREEN (Section 27) ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Wallet Balance card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("wallet_balance_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("TK WALLET SECURE", color = Color.White, fontWeight = FontWeight.Bold)
                                Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, tint = TKGold)
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text("CURRENT BALANCE", color = Color.LightGray, fontSize = 11.sp)
                            
                            val currencySymbol = when (selectedCurrency) {
                                "TRY" -> "₺"
                                "EUR" -> "€"
                                "USD" -> "$"
                                else -> "£"
                            }
                            val currentBalance = when (selectedCurrency) {
                                "TRY" -> 10800.0
                                "EUR" -> 280.0
                                "USD" -> 320.0
                                else -> 245.0
                            }

                            Text(
                                text = "$currencySymbol${currentBalance.toInt()}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Currency Chips selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("GBP", "TRY", "EUR", "USD").forEach { cur ->
                                    val isSelected = selectedCurrency == cur
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) TKRed else Color.White.copy(alpha = 0.15f))
                                            .clickable { selectedCurrency = cur }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(cur, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Wallet Quick controls
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showTopUpDialog = true },
                            modifier = Modifier.weight(1f).height(48.dp).testTag("wallet_topup_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = TKRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TOP UP WALLET")
                        }

                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TKLightGrey),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("TRANSACTIONS", color = TKBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Wallet Transaction logs
                item {
                    Text(
                        "WALLET TRANSACTION HISTORY",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, TKSilverBorder)
                    ) {
                        Column {
                            WalletLedgerItem(amount = "+£100", desc = "Top Up Secured", date = "08 SEP", isAdd = true)
                            HorizontalDivider()
                            WalletLedgerItem(amount = "-£284", desc = "Flight TK1988 Ticket", date = "01 SEP", isAdd = false)
                            HorizontalDivider()
                            WalletLedgerItem(amount = "+£45", desc = "Refund Credit", date = "14 AUG", isAdd = true)
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }

        // Top-up dialog
        if (showTopUpDialog) {
            AlertDialog(
                onDismissRequest = { showTopUpDialog = false },
                title = { Text("Top Up Wallet") },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = topUpAmountStr.toDoubleOrNull() ?: 100.0
                            onTopUpWallet(amt, selectedCurrency)
                            showTopUpDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TKRed)
                    ) {
                        Text("Top Up Now")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTopUpDialog = false }) {
                        Text("Cancel")
                    }
                },
                text = {
                    Column {
                        Text("Enter the load amount to transfer securely into your TK Wallet:", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = topUpAmountStr,
                            onValueChange = { topUpAmountStr = it },
                            label = { Text("Amount ($selectedCurrency)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun QuickActionPill(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(TKLightGrey)
            .border(1.dp, TKSilverBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = TKRed, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TKBlack)
        }
    }
}

@Composable
fun MilesLedgerItem(amount: String, desc: String, date: String, isEarned: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(desc, fontWeight = FontWeight.Bold)
            Text(date, fontSize = 11.sp, color = Color.Gray)
        }

        Text(
            text = amount,
            fontWeight = FontWeight.Black,
            color = if (isEarned) Color(0xFF4CAF50) else TKRed,
            fontSize = 16.sp
        )
    }
}

@Composable
fun WalletLedgerItem(amount: String, desc: String, date: String, isAdd: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(desc, fontWeight = FontWeight.Bold)
            Text(date, fontSize = 11.sp, color = Color.Gray)
        }

        Text(
            text = amount,
            fontWeight = FontWeight.Black,
            color = if (isAdd) Color(0xFF4CAF50) else TKBlack,
            fontSize = 16.sp
        )
    }
}
