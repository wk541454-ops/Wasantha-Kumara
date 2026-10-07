package com.example.ui.screens.createaccount

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlowingBadgeLogo
import com.example.ui.components.NeonBackground
import com.example.ui.theme.*

@Composable
fun CreateAccountNameScreen(
    onNext: (String, String) -> Unit,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    val isFirstNameValid = firstName.isNotBlank() && firstName.all { it.isLetter() || it.isWhitespace() }
    val isLastNameValid = lastName.isNotBlank() && lastName.all { it.isLetter() || it.isWhitespace() }
    val isFormValid = isFirstNameValid && isLastNameValid

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
                    text = "What's Your Name?",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Step 1 of 4 • Enter your official full name",
                    color = NeonTextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Inputs
            Column(modifier = Modifier.fillMaxWidth()) {
                // First Name
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { input ->
                        if (input.all { it.isLetter() || it.isWhitespace() }) {
                            firstName = input
                        }
                    },
                    label = { Text("First Name", color = NeonTextSubtle) },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = NeonTextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NeonDarkSurface,
                        unfocusedContainerColor = NeonDarkSurface,
                        focusedBorderColor = NeonPinkGlow,
                        unfocusedBorderColor = NeonCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                )

                // Last Name
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { input ->
                        if (input.all { it.isLetter() || it.isWhitespace() }) {
                            lastName = input
                        }
                    },
                    label = { Text("Last Name", color = NeonTextSubtle) },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = NeonTextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NeonDarkSurface,
                        unfocusedContainerColor = NeonDarkSurface,
                        focusedBorderColor = NeonPinkGlow,
                        unfocusedBorderColor = NeonCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (firstName.isNotEmpty() && !isFirstNameValid) {
                    Text("Only letters are allowed in First Name", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
                if (lastName.isNotEmpty() && !isLastNameValid) {
                    Text("Only letters are allowed in Last Name", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bottom Buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        if (isFormValid) {
                            onNext(firstName.trim(), lastName.trim())
                        } else {
                            Toast.makeText(context, "Please enter valid first and last names", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFormValid) {
                                Brush.horizontalGradient(listOf(NeonPinkGlow, NeonPurpleGlow, NeonBlue))
                            } else {
                                Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray))
                            }
                        ),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Next: Birthday", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                    }
                }

                TextButton(
                    onClick = onBackToLogin,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Already have an account? Log In", color = NeonCyan, fontSize = 13.sp)
                }
            }
        }
    }
}
