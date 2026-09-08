package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BoardingPass
import com.example.ui.theme.TKBlack
import com.example.ui.theme.TKGold
import com.example.ui.theme.TKLightGrey
import com.example.ui.theme.TKRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardingPassScreen(
    boardingPass: BoardingPass,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondary)
            .statusBarsPadding()
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "BOARDING PASS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Offline storage badge
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Green, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("OFFLINE LOG SAVED - LAST SYNCED JUST NOW", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ticket visual container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp)
                .testTag("boarding_pass_ticket_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Branded top section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TKRed)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TURKISH AIRLINES",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 14.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TKGold)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = boardingPass.cabinClass.uppercase(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Flight details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("FLIGHT", fontSize = 10.sp, color = Color.Gray)
                            Text(boardingPass.flightNumber, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TKBlack)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("DATE", fontSize = 10.sp, color = Color.Gray)
                            Text(boardingPass.departureDate, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TKBlack)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("FROM", fontSize = 10.sp, color = Color.Gray)
                            Text(boardingPass.originCode, fontSize = 32.sp, fontWeight = FontWeight.Black, color = TKBlack)
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(TKLightGrey),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = TKRed, modifier = Modifier.size(20.dp))
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("TO", fontSize = 10.sp, color = Color.Gray)
                            Text(boardingPass.destCode, fontSize = 32.sp, fontWeight = FontWeight.Black, color = TKRed)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("BOARDING GATE", fontSize = 10.sp, color = Color.Gray)
                            Text(boardingPass.gate, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TKBlack)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SEAT", fontSize = 10.sp, color = Color.Gray)
                            Text(boardingPass.seat, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TKRed)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("BOARDING TIME", fontSize = 10.sp, color = Color.Gray)
                            Text(boardingPass.departureTime, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TKBlack)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column {
                        Text("PASSENGER", fontSize = 10.sp, color = Color.Gray)
                        Text(boardingPass.passengerName.uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TKBlack)
                    }
                }

                // Dashed separator line representing tear-off
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                ) {
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        pathEffect = pathEffect
                    )
                }

                // QR barcode scanner mock code
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SCAN AT THE GATE", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Draw a detailed barcode/QR simulation
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .border(1.dp, Color.LightGray)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw high contrast mockup QR code blocks
                            val blocksCount = 7
                            val blockW = size.width / blocksCount
                            val blockH = size.height / blocksCount
                            for (i in 0 until blocksCount) {
                                for (j in 0 until blocksCount) {
                                    // Make a dummy QR matrix pattern
                                    val shouldColor = (i + j) % 2 == 0 || (i < 2 && j < 2) || (i > 4 && j < 2) || (i < 2 && j > 4)
                                    if (shouldColor) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(i * blockW, j * blockH),
                                            size = androidx.compose.ui.geometry.Size(blockW, blockH)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(boardingPass.qrCodeData, fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        // Action controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TKGold),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("ADD TO WALLET", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Button(
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("SHARE", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
