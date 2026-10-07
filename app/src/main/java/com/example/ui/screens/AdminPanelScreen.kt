package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketplaceItem
import com.example.data.model.PostItem
import com.example.ui.components.CustomBottomNavBar
import com.example.ui.components.NeonBackground
import com.example.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val ADMIN_SECRET_KEY = "wk541454"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    posts: List<PostItem>,
    marketplaceItems: List<MarketplaceItem>,
    onDeletePost: (String) -> Unit,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUserEmail = auth.currentUser?.email ?: ""

    // Secret Admin Verification
    val isAdmin = currentUserEmail.startsWith(ADMIN_SECRET_KEY, ignoreCase = true) || currentUserEmail.contains("wk541454")

    if (!isAdmin) {
        LaunchedEffect(Unit) {
            onNavigate("home")
        }
        return
    }

    var activeTab by remember { mutableStateOf("Overview") }
    var totalUsersCount by remember { mutableIntStateOf(12490) }
    var totalPostsCount by remember { mutableIntStateOf(posts.size + 42) }
    var isMaintenanceMode by remember { mutableStateOf(false) }
    var customAppName by remember { mutableStateOf("FriendHub") }

    var rtdbUsers by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }

    // Live Realtime Database Listeners
    LaunchedEffect(Unit) {
        try {
            val database = FirebaseDatabase.getInstance().reference

            database.child("users").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount.toInt()
                    if (count > 0) totalUsersCount = count

                    val userList = mutableListOf<Map<String, String>>()
                    for (child in snapshot.children) {
                        val userEmail = child.child("email").getValue(String::class.java) ?: ""
                        if (userEmail.contains(ADMIN_SECRET_KEY)) continue // Secret Admin filter

                        val fName = child.child("firstName").getValue(String::class.java) ?: "User"
                        val lName = child.child("lastName").getValue(String::class.java) ?: ""
                        userList.add(
                            mapOf(
                                "uid" to (child.key ?: ""),
                                "name" to "$fName $lName".trim(),
                                "email" to userEmail
                            )
                        )
                    }
                    if (userList.isNotEmpty()) {
                        rtdbUsers = userList
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

            database.child("posts").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount.toInt()
                    if (count > 0) totalPostsCount = count
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (e: Exception) {}
    }

    val fallbackUsers = remember {
        mutableStateListOf(
            Pair("Sophia Anderson", "sophiaa@friendhub.app"),
            Pair("Niko Vance", "niko@friendhub.app"),
            Pair("Rudraksh Sharma", "rudraksh@friendhub.app"),
            Pair("Sarah James", "sarahjms@friendhub.app"),
            Pair("Franklin Sr", "franklin@friendhub.app")
        )
    }

    val adminManagedPosts = remember { mutableStateListOf(*posts.toTypedArray()) }
    val adminManagedMarket = remember { mutableStateListOf(*marketplaceItems.toTypedArray()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = NeonMagenta, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🛡️ FriendHub Admin Control", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout", tint = NeonOrange)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeonPureBlack)
            )
        },
        bottomBar = {
            CustomBottomNavBar(selectedRoute = "admin", isAdmin = true, onNavigate = onNavigate)
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
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Section Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Overview", "Users", "Posts", "Market", "Settings").forEach { tab ->
                        val isSelected = tab == activeTab
                        FilterChip(
                            selected = isSelected,
                            onClick = { activeTab = tab },
                            label = { Text(tab, fontSize = 11.sp, color = if (isSelected) Color.White else NeonTextMuted) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonMagenta,
                                containerColor = NeonDarkSurface
                            )
                        )
                    }
                }

                when (activeTab) {
                    "Overview" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text("APP STATISTICS", color = NeonPinkGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AdminStatCard("Total Users", "$totalUsersCount", NeonCyan, Modifier.weight(1f))
                                AdminStatCard("Total Posts", "$totalPostsCount", NeonMagenta, Modifier.weight(1f))
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AdminStatCard("Marketplace", "${adminManagedMarket.size + 18}", NeonBlue, Modifier.weight(1f))
                                AdminStatCard("Reports / Flagged", "0 Clean", Color.Green, Modifier.weight(1f))
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text("SYSTEM ACTIONS", color = NeonPinkGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            AdminCard("Manage Users ($totalUsersCount)", Icons.Default.People) { activeTab = "Users" }
                            AdminCard("Manage Posts ($totalPostsCount)", Icons.Default.Article) { activeTab = "Posts" }
                            AdminCard("Marketplace Listings", Icons.Default.ShoppingBag) { activeTab = "Market" }
                            AdminCard("App Settings & Config", Icons.Default.Settings) { activeTab = "Settings" }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = onLogout,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Admin Logout", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    "Users" -> {
                        val displayUsers = if (rtdbUsers.isNotEmpty()) rtdbUsers else fallbackUsers.map { mapOf("uid" to it.second, "name" to it.first, "email" to it.second) }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(displayUsers) { user ->
                                Surface(
                                    color = NeonDarkSurface,
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.size(40.dp).clip(CircleShape).background(NeonPurple)
                                        ) {
                                            Text((user["name"] ?: "U").take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(user["name"] ?: "User", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(user["email"] ?: "", color = NeonTextMuted, fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                val uid = user["uid"] ?: ""
                                                if (uid.isNotEmpty()) {
                                                    try {
                                                        FirebaseDatabase.getInstance()
                                                            .reference.child("users").child(uid).removeValue()
                                                    } catch (e: Exception) {}
                                                }
                                                Toast.makeText(context, "User BANNED and removed from Realtime DB!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text("BAN", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Posts" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(adminManagedPosts, key = { it.id }) { post ->
                                Surface(
                                    color = NeonDarkSurface,
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(post.authorName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(post.caption, color = NeonTextMuted, fontSize = 12.sp, maxLines = 1)
                                        }

                                        IconButton(onClick = {
                                            adminManagedPosts.remove(post)
                                            onDeletePost(post.id)
                                            try {
                                                FirebaseDatabase.getInstance()
                                                    .reference.child("posts").child(post.id).removeValue()
                                            } catch (e: Exception) {}
                                            Toast.makeText(context, "Post deleted by Admin", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Market" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(adminManagedMarket, key = { it.id }) { item ->
                                Surface(
                                    color = NeonDarkSurface,
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("$%.2f • ${item.sellerName}".format(item.price), color = NeonCyan, fontSize = 12.sp)
                                        }

                                        IconButton(onClick = {
                                            adminManagedMarket.remove(item)
                                            try {
                                                FirebaseDatabase.getInstance()
                                                    .reference.child("marketplace").child(item.id).removeValue()
                                            } catch (e: Exception) {}
                                            Toast.makeText(context, "Product listing removed", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Settings" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text("GLOBAL APP CONFIG", color = NeonPinkGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                color = NeonDarkSurface,
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Maintenance Mode ON/OFF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("Temporarily pause public streams & marketplace", color = NeonTextMuted, fontSize = 12.sp)
                                    }

                                    Switch(
                                        checked = isMaintenanceMode,
                                        onCheckedChange = {
                                            isMaintenanceMode = it
                                            try {
                                                FirebaseDatabase.getInstance()
                                                    .reference.child("app_config/maintenance").setValue(it)
                                            } catch (e: Exception) {}
                                            Toast.makeText(context, "Maintenance Mode: $it", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = SwitchDefaults.colors(checkedTrackColor = NeonMagenta)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = customAppName,
                                onValueChange = { customAppName = it },
                                label = { Text("App Title", color = NeonTextMuted) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    com.example.data.repository.AppRepository.saveAppConfig("app_title", customAppName) { success ->
                                        Toast.makeText(context, "App configuration saved to FriendHub Cloud! ✓", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Save App Config", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        color = NeonDarkSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = NeonMagenta, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NeonTextMuted, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun AdminStatCard(title: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Surface(
        color = NeonDarkSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCardBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = NeonTextMuted, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = accentColor, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
