package com.example.ui.theme

import androidx.compose.runtime.Composable

@Composable
fun Theme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    FriendHubTheme(darkTheme = darkTheme, content = content)
}
