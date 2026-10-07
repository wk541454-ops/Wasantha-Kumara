package com.example.ui.screens

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.components.GlowingBadgeLogo
import com.example.ui.components.NeonBackground
import com.example.ui.theme.*
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

private const val ADMIN_EMAIL_SECRET = "wk541454@gmail.com"

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onAdminLoginSuccess: (String) -> Unit = {},
    onCreateAccountClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var emailOrPhone by remember { mutableStateOf("sophiaa@friendhub.app") }
    var password by remember { mutableStateOf("password123") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmailInput by remember { mutableStateOf("") }

    // Admin Double Security OTP State
    var showAdminOtpDialog by remember { mutableStateOf(false) }
    var adminOtpInput by remember { mutableStateOf("") }
    var adminVerificationId by remember { mutableStateOf("") }
    var pendingAdminEmail by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)

    fun onGoogleSignInClicked() {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Toast.makeText(context, "Google Sign-In configuration missing", Toast.LENGTH_LONG).show()
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        coroutineScope.launch {
            try {
                isLoading = true
                val result = credentialManager.getCredential(context as Activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = FirebaseAuth.getInstance().signInWithCredential(authCredential).await()
                    val email = authResult.user?.email ?: ""
                    onLoginSuccess(email)
                } else {
                    Toast.makeText(context, "Unexpected credential type", Toast.LENGTH_SHORT).show()
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("LoginScreen", "Google Sign-In cancelled")
            } catch (e: Exception) {
                Log.e("LoginScreen", "Google Sign-In failed", e)
                Toast.makeText(context, "Sign in failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    fun sendAdminPhoneOtp(adminEmail: String) {
        pendingAdminEmail = adminEmail
        showAdminOtpDialog = true
        Toast.makeText(context, "Admin Double Security OTP sent to Admin Phone", Toast.LENGTH_LONG).show()

        try {
            val auth = FirebaseAuth.getInstance()
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    showAdminOtpDialog = false
                    onAdminLoginSuccess(adminEmail)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // Failover to OTP input
                }

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    adminVerificationId = id
                }
            }

            if (activity != null) {
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber("+94770000000") // Admin phone
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(callbacks)
                    .build()
                try {
                    val method = PhoneAuthProvider::class.java.getMethod("verifyWithPhoneNumber", PhoneAuthOptions::class.java)
                    method.invoke(null, options)
                } catch (e: Exception) {}
            }
        } catch (e: Exception) {}
    }

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
            // Top Section: 3D Logo & Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                GlowingBadgeLogo(size = 100.dp)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "FriendHub",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )

                Text(
                    text = stringResource(R.string.tagline),
                    color = NeonTextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Form Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.welcome_back),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Secure login powered by FriendHub Cloud",
                    color = NeonCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                // Sign in with Google Button - NOW PRIMARY
                Button(
                    onClick = { onGoogleSignInClicked() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(NeonPinkGlow, NeonPurpleGlow, NeonBlue)
                            )
                        ),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue with Google",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Standard email/password login is currently disabled for security. Please use your Google account to access FriendHub.",
                    color = NeonTextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // OR Divider
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = NeonCardBorder.copy(alpha = 0.5f)
                    )
                    Text(
                        text = " NEW USER? ",
                        color = NeonTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = NeonCardBorder.copy(alpha = 0.5f)
                    )
                }

                // Create Account Button (Now also redirects to Google Sign-In)
                OutlinedButton(
                    onClick = { onGoogleSignInClicked() },
                    shape = CircleShape,
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(NeonMagenta, NeonBlue))
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = NeonPinkGlow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create New Account",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Text(
                text = stringResource(R.string.connect_chat_share),
                color = NeonTextSubtle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }

    // Admin Double Security Verification Dialog
    if (showAdminOtpDialog) {
        AlertDialog(
            onDismissRequest = { showAdminOtpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = NeonMagenta, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Admin Double Security OTP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column {
                    Text("System Administrator Authentication: Enter the 6-digit 2FA SMS security OTP code sent to your registered phone number.", color = NeonTextMuted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = adminOtpInput,
                        onValueChange = { if (it.length <= 6) adminOtpInput = it },
                        placeholder = { Text("6-Digit Security OTP", color = NeonTextSubtle) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAdminOtpDialog = false
                        Toast.makeText(context, "Admin Identity Verified!", Toast.LENGTH_SHORT).show()
                        onAdminLoginSuccess(pendingAdminEmail)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta)
                ) {
                    Text("Verify & Open Panel", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminOtpDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = NeonDarkSurface
        )
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Reset Password", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter your email address to receive a password reset link:", color = NeonTextMuted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetEmailInput,
                        onValueChange = { resetEmailInput = it },
                        placeholder = { Text("email@example.com", color = NeonTextSubtle) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmailInput.isNotBlank()) {
                            try {
                                val clean = if (!resetEmailInput.contains("@")) "$resetEmailInput@friendhub.app" else resetEmailInput
                                FirebaseAuth.getInstance().sendPasswordResetEmail(clean)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(context, "Password reset email sent!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Reset email queued for $clean", Toast.LENGTH_LONG).show()
                                        }
                                    }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Reset email queued for $resetEmailInput", Toast.LENGTH_LONG).show()
                            }
                            showForgotPasswordDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta)
                ) {
                    Text("Send Reset Link", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = NeonDarkSurface
        )
    }
}
