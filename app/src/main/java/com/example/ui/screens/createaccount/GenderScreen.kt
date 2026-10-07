package com.example.ui.screens.createaccount

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlowingBadgeLogo
import com.example.ui.components.NeonBackground
import com.example.ui.theme.*

@Composable
fun GenderScreen(
    onNext: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedGender by remember { mutableStateOf("") } // "Male" or "Female"

    NeonBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                GlowingBadgeLogo(size = 90.dp)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Select Gender",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "ඔබ ගැහැණුද? පිරිමිද?",
                    color = NeonCyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = "Step 3 of 4 • Choose one to continue",
                    color = NeonTextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Two Big Selectable Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Male Card ("පිරිමි")
                val isMaleSelected = selectedGender == "Male"
                Surface(
                    color = if (isMaleSelected) NeonDarkSurface else NeonPureBlack,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isMaleSelected) 2.5.dp else 1.dp,
                        color = if (isMaleSelected) NeonCyan else NeonCardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedGender = "Male" }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isMaleSelected) NeonCyan else NeonDarkSurface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Male,
                                contentDescription = "Male",
                                tint = if (isMaleSelected) Color.Black else NeonCyan,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column {
                            Text(
                                text = "පිරිමි (Male)",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Select if you are male",
                                color = NeonTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Female Card ("ගැහැණු")
                val isFemaleSelected = selectedGender == "Female"
                Surface(
                    color = if (isFemaleSelected) NeonDarkSurface else NeonPureBlack,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isFemaleSelected) 2.5.dp else 1.dp,
                        color = if (isFemaleSelected) NeonPinkGlow else NeonCardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedGender = "Female" }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isFemaleSelected) NeonPinkGlow else NeonDarkSurface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Female,
                                contentDescription = "Female",
                                tint = if (isFemaleSelected) Color.White else NeonPinkGlow,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column {
                            Text(
                                text = "ගැහැණු (Female)",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Select if you are female",
                                color = NeonTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.weight(1f).height(52.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Back")
                }

                val canProceed = selectedGender.isNotEmpty()
                Button(
                    onClick = {
                        if (canProceed) {
                            onNext(selectedGender)
                        }
                    },
                    enabled = canProceed,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(52.dp)
                        .clip(CircleShape)
                        .background(
                            if (canProceed) {
                                Brush.horizontalGradient(listOf(NeonPinkGlow, NeonPurpleGlow, NeonBlue))
                            } else {
                                Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray))
                            }
                        ),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Next: Verify", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    }
}
