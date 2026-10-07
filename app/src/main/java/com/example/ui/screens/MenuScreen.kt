package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.components.CustomBottomNavBar
import com.example.ui.components.NeonBackground
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    userProfile: UserProfile,
    isDarkMode: Boolean,
    notificationsEnabled: Boolean,
    isAdmin: Boolean = false,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onConfigureSupabaseClick: () -> Unit,
    onAdminPanelClick: () -> Unit = {},
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings & Menu",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("home") }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeonPureBlack)
            )
        },
        bottomBar = {
            CustomBottomNavBar(selectedRoute = "menu", onNavigate = onNavigate)
        },
        containerColor = NeonPureBlack
    ) { innerPadding ->
        NeonBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // User Quick Profile Card
                Surface(
                    color = NeonDarkSurface,
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate("profile") }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(NeonPurple)
                        ) {
                            Text(
                                text = userProfile.name.take(2).uppercase(),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userProfile.name,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "@${userProfile.handle}",
                                color = NeonTextMuted,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = onEditProfileClick,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Edit", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Full Settings Hub Card
                Surface(
                    color = Color(0xFF1E1B2E),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleMain.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate("settings") }
                        .padding(bottom = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(PurpleMain.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = PurpleMain,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Settings & Privacy",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Manage account, notifications, security & theme",
                                color = NeonTextMuted,
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = PurpleMain
                        )
                    }
                }

                // Section 1: Preferences
                Text(
                    text = "PREFERENCES",
                    color = NeonPinkGlow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                SettingsToggleRow(
                    icon = Icons.Outlined.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Neon Dark UI Theme",
                    checked = isDarkMode,
                    onCheckedChange = onToggleDarkMode
                )

                SettingsToggleRow(
                    icon = Icons.Outlined.Notifications,
                    title = "Push Notifications",
                    subtitle = "Likes, comments, live streams",
                    checked = notificationsEnabled,
                    onCheckedChange = onToggleNotifications
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Section 2: Account & Connection
                Text(
                    text = "ACCOUNT & BACKEND",
                    color = NeonPinkGlow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                SettingsActionRow(
                    icon = Icons.Outlined.Person,
                    title = "Edit Profile",
                    subtitle = "Name, bio, handle, profile picture",
                    onClick = onEditProfileClick
                )

                SettingsActionRow(
                    icon = Icons.Outlined.Cloud,
                    title = "Configure FriendHub Cloud Server",
                    subtitle = "Set custom private cloud endpoint URL & Key",
                    onClick = onConfigureSupabaseClick
                )

                if (isAdmin) {
                    SettingsActionRow(
                        icon = Icons.Default.Shield,
                        title = "Admin Control Panel",
                        subtitle = "Manage users, delete posts, system status",
                        onClick = onAdminPanelClick
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 3: App Information & Legal
                Text(
                    text = "ABOUT & LEGAL",
                    color = NeonPinkGlow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                SettingsActionRow(
                    icon = Icons.Outlined.HelpOutline,
                    title = "Help & Support",
                    subtitle = "Contact us at ${com.example.config.AppConfig.CONTACT_EMAIL}",
                    onClick = {
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                data = android.net.Uri.parse("mailto:${com.example.config.AppConfig.CONTACT_EMAIL}")
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "FriendHub Support Request")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Contact Support: ${com.example.config.AppConfig.CONTACT_EMAIL}", Toast.LENGTH_LONG).show()
                        }
                    }
                )

                SettingsActionRow(
                    icon = Icons.Outlined.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "Data encryption & user rights",
                    onClick = { showPrivacyDialog = true }
                )

                SettingsActionRow(
                    icon = Icons.Outlined.Info,
                    title = "About FriendHub",
                    subtitle = "Where Friends Connect",
                    onClick = { showAboutDialog = true }
                )

                SettingsActionRow(
                    icon = Icons.Outlined.Smartphone,
                    title = "App Version",
                    subtitle = "Version 1.0.0 (Build 2026.10)",
                    onClick = {
                        Toast.makeText(context, "FriendHub v1.0.0 is up to date!", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Section 4: Critical Actions (Logout & Delete Account)
                Button(
                    onClick = onLogoutClick,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCardBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = NeonOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Log Out of Account", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = true },
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Delete Account",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Delete Account", color = Color.Red, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "FriendHub values your privacy. All user credentials and direct messages are transmitted with SSL encryption. Your uploaded posts and images are stored securely on FriendHub Cloud with end-to-end encryption. We never sell your personal data.\n\nFor privacy inquiries: ${com.example.config.AppConfig.CONTACT_EMAIL}",
                    color = NeonTextMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close", color = NeonMagenta, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = NeonDarkSurface
        )
    }

    // About Us Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About FriendHub", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "FriendHub is the premier next-gen social network powered by FriendHub Cloud. Designed for instant video sharing, live streams, real-time messaging, and community marketplace trading.\n\n${com.example.config.AppConfig.COPYRIGHT_NOTICE}\n\nVersion 1.0.0",
                    color = NeonTextMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Awesome!", color = NeonMagenta, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = NeonDarkSurface
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Account?", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to permanently delete your FriendHub account? This action cannot be undone and all posts, marketplace listings, and messages will be purged.",
                    color = Color.White,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteAccountClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete Permanently", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = NeonDarkSurface
        )
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        color = NeonDarkSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NeonCardSurface)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = NeonMagenta, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, color = NeonTextMuted, fontSize = 12.sp)
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = NeonMagenta,
                    uncheckedThumbColor = NeonTextMuted,
                    uncheckedTrackColor = NeonCardSurface
                )
            )
        }
    }
}

@Composable
fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        color = NeonDarkSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NeonCardSurface)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, color = NeonTextMuted, fontSize = 12.sp)
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = NeonTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
