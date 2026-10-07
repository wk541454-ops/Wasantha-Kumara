package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "friendhub_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val THEME_MODE = stringPreferencesKey("theme_mode") // "dark", "light", "system"
        val APP_LANGUAGE = stringPreferencesKey("app_language") // "en", "si", "ta"
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val REMEMBER_ME = booleanPreferencesKey("remember_me")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PHONE = stringPreferencesKey("user_phone")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val IS_ADMIN = booleanPreferencesKey("is_admin")
        val SUPABASE_URL = stringPreferencesKey("supabase_url")
        val SUPABASE_KEY = stringPreferencesKey("supabase_key")

        // Privacy
        val PRIVACY_PROFILE_VISIBILITY = stringPreferencesKey("privacy_profile_visibility") // "Everyone", "Friends", "Only me"
        val PRIVACY_FRIEND_REQUESTS = stringPreferencesKey("privacy_friend_requests") // "Everyone", "Friends of friends", "No one"
        val PRIVACY_MESSAGES = stringPreferencesKey("privacy_messages") // "Everyone", "Friends", "No one"
        val PRIVACY_POST_VISIBILITY = stringPreferencesKey("privacy_post_visibility")
        val PRIVACY_STORY_VISIBILITY = stringPreferencesKey("privacy_story_visibility")
        val PRIVACY_ONLINE_STATUS = booleanPreferencesKey("privacy_online_status")
        val PRIVACY_LAST_ACTIVE = booleanPreferencesKey("privacy_last_active")
        val PRIVACY_READ_RECEIPTS = booleanPreferencesKey("privacy_read_receipts")
        val BLOCKED_USERS = stringSetPreferencesKey("blocked_users")

        // Notification Preferences
        val NOTIF_PUSH = booleanPreferencesKey("notif_push")
        val NOTIF_MESSAGES = booleanPreferencesKey("notif_messages")
        val NOTIF_FRIEND_REQUESTS = booleanPreferencesKey("notif_friend_requests")
        val NOTIF_LIKES = booleanPreferencesKey("notif_likes")
        val NOTIF_COMMENTS = booleanPreferencesKey("notif_comments")
        val NOTIF_MENTIONS = booleanPreferencesKey("notif_mentions")
        val NOTIF_STORIES = booleanPreferencesKey("notif_stories")
        val NOTIF_EMAIL = booleanPreferencesKey("notif_email")

        // Content & Media
        val MEDIA_AUTOPLAY = booleanPreferencesKey("media_autoplay")
        val MEDIA_DATA_SAVER = booleanPreferencesKey("media_data_saver")
        val MEDIA_UPLOAD_QUALITY = stringPreferencesKey("media_upload_quality") // "High", "Standard"
        val MEDIA_AUTO_DOWNLOAD = booleanPreferencesKey("media_auto_download")
        val SENSITIVE_CONTENT_WARNING = booleanPreferencesKey("sensitive_content_warning")

        // Security
        val SECURITY_TWO_FACTOR = booleanPreferencesKey("security_two_factor")
        val SECURITY_LOGIN_ALERTS = booleanPreferencesKey("security_login_alerts")
        val SECURITY_APP_LOCK = booleanPreferencesKey("security_app_lock")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.DARK_MODE] ?: true
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE] ?: "system"
    }

    val appLanguage: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.APP_LANGUAGE] ?: "en"
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
    }

    val rememberMe: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.REMEMBER_ME] ?: true
    }

    val userEmail: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.USER_EMAIL] ?: "sophiaa@friendhub.app"
    }

    val userPhone: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.USER_PHONE] ?: "+94 77 123 4567"
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_LOGGED_IN] ?: false
    }

    val isAdmin: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_ADMIN] ?: false
    }

    val supabaseUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.SUPABASE_URL] ?: ""
    }

    val supabaseKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.SUPABASE_KEY] ?: ""
    }

    // Privacy Flows
    val profileVisibility: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.PRIVACY_PROFILE_VISIBILITY] ?: "Everyone"
    }

    val friendRequestPermission: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.PRIVACY_FRIEND_REQUESTS] ?: "Everyone"
    }

    val messagePermission: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.PRIVACY_MESSAGES] ?: "Friends"
    }

    val postVisibility: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.PRIVACY_POST_VISIBILITY] ?: "Everyone"
    }

    val storyVisibility: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.PRIVACY_STORY_VISIBILITY] ?: "Friends"
    }

    val onlineStatusEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.PRIVACY_ONLINE_STATUS] ?: true
    }

    val lastActiveEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.PRIVACY_LAST_ACTIVE] ?: true
    }

    val readReceiptsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.PRIVACY_READ_RECEIPTS] ?: true
    }

    val blockedUsers: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.BLOCKED_USERS] ?: emptySet()
    }

    // Notification Flows
    val notifPush: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[Keys.NOTIF_PUSH] ?: true }
    val notifMessages: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[Keys.NOTIF_MESSAGES] ?: true }
    val notifFriendRequests: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[Keys.NOTIF_FRIEND_REQUESTS] ?: true }
    val notifLikes: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[Keys.NOTIF_LIKES] ?: true }
    val notifComments: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[Keys.NOTIF_COMMENTS] ?: true }
    val notifMentions: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[Keys.NOTIF_MENTIONS] ?: true }
    val notifStories: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[Keys.NOTIF_STORIES] ?: true }
    val notifEmail: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[Keys.NOTIF_EMAIL] ?: false }

    // Content & Media Flows
    val mediaAutoplay: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[Keys.MEDIA_AUTOPLAY] ?: true }
    val mediaDataSaver: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[Keys.MEDIA_DATA_SAVER] ?: false }
    val mediaUploadQuality: Flow<String> = context.dataStore.data.map { prefs -> prefs[Keys.MEDIA_UPLOAD_QUALITY] ?: "High" }
    val mediaAutoDownload: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[Keys.MEDIA_AUTO_DOWNLOAD] ?: true }
    val sensitiveContentWarning: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[Keys.SENSITIVE_CONTENT_WARNING] ?: true }

    // Security Flows
    val securityTwoFactor: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[Keys.SECURITY_TWO_FACTOR] ?: false }
    val securityLoginAlerts: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[Keys.SECURITY_LOGIN_ALERTS] ?: true }
    val securityAppLock: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[Keys.SECURITY_APP_LOCK] ?: false }

    // Setters
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DARK_MODE] = enabled
            prefs[Keys.THEME_MODE] = if (enabled) "dark" else "light"
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode
        }
    }

    suspend fun setAppLanguage(lang: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.APP_LANGUAGE] = lang
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setRememberMe(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.REMEMBER_ME] = enabled
        }
    }

    suspend fun setIsAdmin(admin: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_ADMIN] = admin
        }
    }

    suspend fun setUserPhone(phone: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_PHONE] = phone
        }
    }

    suspend fun setLoggedIn(loggedIn: Boolean, email: String = "") {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = loggedIn
            if (email.isNotEmpty()) {
                prefs[Keys.USER_EMAIL] = email
            }
            if (!loggedIn) {
                prefs[Keys.IS_ADMIN] = false
            }
        }
    }

    suspend fun saveSupabaseCredentials(url: String, key: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SUPABASE_URL] = url
            prefs[Keys.SUPABASE_KEY] = key
        }
    }

    // Privacy Setters
    suspend fun setProfileVisibility(v: String) = context.dataStore.edit { it[Keys.PRIVACY_PROFILE_VISIBILITY] = v }
    suspend fun setFriendRequestPermission(v: String) = context.dataStore.edit { it[Keys.PRIVACY_FRIEND_REQUESTS] = v }
    suspend fun setMessagePermission(v: String) = context.dataStore.edit { it[Keys.PRIVACY_MESSAGES] = v }
    suspend fun setPostVisibility(v: String) = context.dataStore.edit { it[Keys.PRIVACY_POST_VISIBILITY] = v }
    suspend fun setStoryVisibility(v: String) = context.dataStore.edit { it[Keys.PRIVACY_STORY_VISIBILITY] = v }
    suspend fun setOnlineStatus(enabled: Boolean) = context.dataStore.edit { it[Keys.PRIVACY_ONLINE_STATUS] = enabled }
    suspend fun setLastActive(enabled: Boolean) = context.dataStore.edit { it[Keys.PRIVACY_LAST_ACTIVE] = enabled }
    suspend fun setReadReceipts(enabled: Boolean) = context.dataStore.edit { it[Keys.PRIVACY_READ_RECEIPTS] = enabled }

    suspend fun blockUser(username: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.BLOCKED_USERS] ?: emptySet()
            prefs[Keys.BLOCKED_USERS] = current + username
        }
    }

    suspend fun unblockUser(username: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.BLOCKED_USERS] ?: emptySet()
            prefs[Keys.BLOCKED_USERS] = current - username
        }
    }

    // Notification Setters
    suspend fun setNotifPush(v: Boolean) = context.dataStore.edit { it[Keys.NOTIF_PUSH] = v }
    suspend fun setNotifMessages(v: Boolean) = context.dataStore.edit { it[Keys.NOTIF_MESSAGES] = v }
    suspend fun setNotifFriendRequests(v: Boolean) = context.dataStore.edit { it[Keys.NOTIF_FRIEND_REQUESTS] = v }
    suspend fun setNotifLikes(v: Boolean) = context.dataStore.edit { it[Keys.NOTIF_LIKES] = v }
    suspend fun setNotifComments(v: Boolean) = context.dataStore.edit { it[Keys.NOTIF_COMMENTS] = v }
    suspend fun setNotifMentions(v: Boolean) = context.dataStore.edit { it[Keys.NOTIF_MENTIONS] = v }
    suspend fun setNotifStories(v: Boolean) = context.dataStore.edit { it[Keys.NOTIF_STORIES] = v }
    suspend fun setNotifEmail(v: Boolean) = context.dataStore.edit { it[Keys.NOTIF_EMAIL] = v }

    // Content & Media Setters
    suspend fun setMediaAutoplay(v: Boolean) = context.dataStore.edit { it[Keys.MEDIA_AUTOPLAY] = v }
    suspend fun setMediaDataSaver(v: Boolean) = context.dataStore.edit { it[Keys.MEDIA_DATA_SAVER] = v }
    suspend fun setMediaUploadQuality(v: String) = context.dataStore.edit { it[Keys.MEDIA_UPLOAD_QUALITY] = v }
    suspend fun setMediaAutoDownload(v: Boolean) = context.dataStore.edit { it[Keys.MEDIA_AUTO_DOWNLOAD] = v }
    suspend fun setSensitiveContentWarning(v: Boolean) = context.dataStore.edit { it[Keys.SENSITIVE_CONTENT_WARNING] = v }

    // Security Setters
    suspend fun setSecurityTwoFactor(v: Boolean) = context.dataStore.edit { it[Keys.SECURITY_TWO_FACTOR] = v }
    suspend fun setSecurityLoginAlerts(v: Boolean) = context.dataStore.edit { it[Keys.SECURITY_LOGIN_ALERTS] = v }
    suspend fun setSecurityAppLock(v: Boolean) = context.dataStore.edit { it[Keys.SECURITY_APP_LOCK] = v }
}
