package com.example.utils

import java.text.SimpleDateFormat
import java.util.*

object PresenceUtils {
    /**
     * Formats last-seen timestamp according to FriendHub presence guidelines.
     * Examples:
     * - "Last seen just now"
     * - "Last seen 5 min ago"
     * - "Last seen 10:42 AM"
     * - "Last seen yesterday at 8:15 PM"
     * - "Last seen Oct 6 at 8:15 PM"
     */
    fun formatLastSeen(lastSeenTimestamp: Long): String {
        if (lastSeenTimestamp <= 0L) return "Offline"

        val now = System.currentTimeMillis()
        val diffMs = now - lastSeenTimestamp
        val diffMin = diffMs / (1000 * 60)

        val dateLastSeen = Date(lastSeenTimestamp)
        val calendarNow = Calendar.getInstance()
        val calendarLast = Calendar.getInstance().apply { time = dateLastSeen }

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dateTimeFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
        val dateOnlyFormat = SimpleDateFormat("MMM d 'at' h:mm a", Locale.getDefault())

        return when {
            diffMin < 1 -> "Last seen just now"
            diffMin < 60 -> "Last seen $diffMin min ago"
            calendarNow.get(Calendar.YEAR) == calendarLast.get(Calendar.YEAR) &&
            calendarNow.get(Calendar.DAY_OF_YEAR) == calendarLast.get(Calendar.DAY_OF_YEAR) -> {
                "Last seen ${timeFormat.format(dateLastSeen)}"
            }
            calendarNow.get(Calendar.YEAR) == calendarLast.get(Calendar.YEAR) &&
            calendarNow.get(Calendar.DAY_OF_YEAR) - calendarLast.get(Calendar.DAY_OF_YEAR) == 1 -> {
                "Last seen yesterday at ${timeFormat.format(dateLastSeen)}"
            }
            calendarNow.get(Calendar.YEAR) == calendarLast.get(Calendar.YEAR) -> {
                "Last seen ${dateOnlyFormat.format(dateLastSeen)}"
            }
            else -> {
                "Last seen ${dateTimeFormat.format(dateLastSeen)}"
            }
        }
    }
}
