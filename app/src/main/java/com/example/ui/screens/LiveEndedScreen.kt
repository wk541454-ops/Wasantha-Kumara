package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LiveSession
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveEndedScreen(
    session: LiveSession,
    onNavigate: (String) -> Unit
) {
    val durationMinutes = ((System.currentTimeMillis() - session.startedAt) / 60000L).coerceAtLeast(1L)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Broadcast Ended", color = WhiteText, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BlackBg)
            )
        },
        containerColor = BlackBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(PurpleMain.copy(alpha = 0.2f), CircleShape)
                    .border(2.dp, PurpleMain, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = PurpleMain, modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Great Live Broadcast!", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text(text = "Here is a summary of your session stats", color = GrayText, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    StatRow(label = "Duration", value = "$durationMinutes minutes")
                    HorizontalDivider(color = BorderGray)
                    StatRow(label = "Peak Viewers", value = "${session.viewerCount + 12}")
                    HorizontalDivider(color = BorderGray)
                    StatRow(label = "Total Viewers", value = "${session.viewerCount + 45}")
                    HorizontalDivider(color = BorderGray)
                    StatRow(label = "New Followers Gained", value = "+12")
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = { onNavigate("live") },
                colors = ButtonDefaults.buttonColors(containerColor = PurpleMain),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Back to Live Hub", color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = GrayText, fontSize = 14.sp)
        Text(text = value, color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
