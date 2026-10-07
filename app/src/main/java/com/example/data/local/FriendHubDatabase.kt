package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PostEntity::class,
        NotificationEntity::class,
        MarketplaceEntity::class,
        StoryEntity::class,
        ProfileEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FriendHubDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun notificationDao(): NotificationDao
    abstract fun marketplaceDao(): MarketplaceDao
    abstract fun storyDao(): StoryDao
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: FriendHubDatabase? = null

        fun getDatabase(context: Context): FriendHubDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FriendHubDatabase::class.java,
                    "friendhub_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
