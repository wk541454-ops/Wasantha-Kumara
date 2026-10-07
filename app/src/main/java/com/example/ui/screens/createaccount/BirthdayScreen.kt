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
import androidx.compose.material.icons.filled.Cake
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
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayScreen(
    firstName: String,
    onNext: (String, Int) -> Unit,
    onBack: () -> Unit
) {
    val currentCal = Calendar.getInstance()
    var selectedYear by remember { mutableIntStateOf(2002) }
    var selectedMonth by remember { mutableIntStateOf(5) } // 1..12
    var selectedDay by remember { mutableIntStateOf(15) }

    var yearExpanded by remember { mutableStateOf(false) }
    var monthExpanded by remember { mutableStateOf(false) }
    var dayExpanded by remember { mutableStateOf(false) }

    // Calculate age
    val age = remember(selectedYear, selectedMonth, selectedDay) {
        val today = Calendar.getInstance()
        var calculatedAge = today.get(Calendar.YEAR) - selectedYear
        val currentMonth = today.get(Calendar.MONTH) + 1
        val currentDay = today.get(Calendar.DAY_OF_MONTH)
        if (currentMonth < selectedMonth || (currentMonth == selectedMonth && currentDay < selectedDay)) {
            calculatedAge--
        }
        calculatedAge
    }

    val is18Plus = age >= 18

    val years = (1940..currentCal.get(Calendar.YEAR)).toList().reversed()
    val months = (1..12).toList()
    val days = (1..31).toList()

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
                    text = "When's Your Birthday?",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Step 2 of 4 • Hi $firstName, you must be 18+ to join",
                    color = NeonTextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Date Pickers Grid
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Day Picker
                    ExposedDropdownMenuBox(
                        expanded = dayExpanded,
                        onExpandedChange = { dayExpanded = !dayExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = "Day: $selectedDay",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = NeonDarkSurface,
                                unfocusedContainerColor = NeonDarkSurface,
                                focusedBorderColor = NeonPinkGlow,
                                unfocusedBorderColor = NeonCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = dayExpanded,
                            onDismissRequest = { dayExpanded = false },
                            modifier = Modifier.background(NeonDarkSurface)
                        ) {
                            days.forEach { day ->
                                DropdownMenuItem(
                                    text = { Text("$day", color = Color.White) },
                                    onClick = {
                                        selectedDay = day
                                        dayExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Month Picker
                    ExposedDropdownMenuBox(
                        expanded = monthExpanded,
                        onExpandedChange = { monthExpanded = !monthExpanded },
                        modifier = Modifier.weight(1.2f)
                    ) {
                        OutlinedTextField(
                            value = "Month: $selectedMonth",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = NeonDarkSurface,
                                unfocusedContainerColor = NeonDarkSurface,
                                focusedBorderColor = NeonPinkGlow,
                                unfocusedBorderColor = NeonCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = monthExpanded,
                            onDismissRequest = { monthExpanded = false },
                            modifier = Modifier.background(NeonDarkSurface)
                        ) {
                            months.forEach { month ->
                                DropdownMenuItem(
                                    text = { Text("Month $month", color = Color.White) },
                                    onClick = {
                                        selectedMonth = month
                                        monthExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Year Picker
                    ExposedDropdownMenuBox(
                        expanded = yearExpanded,
                        onExpandedChange = { yearExpanded = !yearExpanded },
                        modifier = Modifier.weight(1.2f)
                    ) {
                        OutlinedTextField(
                            value = "$selectedYear",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = NeonDarkSurface,
                                unfocusedContainerColor = NeonDarkSurface,
                                focusedBorderColor = NeonPinkGlow,
                                unfocusedBorderColor = NeonCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = yearExpanded,
                            onDismissRequest = { yearExpanded = false },
                            modifier = Modifier.background(NeonDarkSurface)
                        ) {
                            years.forEach { year ->
                                DropdownMenuItem(
                                    text = { Text("$year", color = Color.White) },
                                    onClick = {
                                        selectedYear = year
                                        yearExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Age Card Indicator
                Surface(
                    color = if (is18Plus) NeonDarkSurface else Color(0xFF330811),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (is18Plus) NeonCyan else Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your calculated age: $age years old",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (!is18Plus) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "ඔබට අවුරුදු 18 සම්පූර්ණ විය යුතුයි",
                                color = Color.Red,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
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

                Button(
                    onClick = {
                        if (is18Plus) {
                            val formattedDate = "%04d-%02d-%02d".format(selectedYear, selectedMonth, selectedDay)
                            onNext(formattedDate, age)
                        }
                    },
                    enabled = is18Plus,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(52.dp)
                        .clip(CircleShape)
                        .background(
                            if (is18Plus) {
                                Brush.horizontalGradient(listOf(NeonPinkGlow, NeonPurpleGlow, NeonBlue))
                            } else {
                                Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray))
                            }
                        ),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Next: Gender", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    }
}
