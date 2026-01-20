package com.example.hikermanagementapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * FEATURE B: Store, View and Delete Hike Details
 *
 * SQLite database configuration using Room Persistence Library.
 *
 * Database name: "mhike.db"
 * Version: 4
 * Tables: hikes, observations
 *
 * Room provides:
 * - Automatic object mapping (no manual cursor handling)
 * - LiveData/Flow support for reactive UI updates
 * - Migration support for database schema changes
 */
@Database(
    entities = [Hike::class, Observation::class],
    version = 4,
    exportSchema = true  // Exports schema to app/schemas/ for version control
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hikeDao(): HikeDao
    abstract fun observationDao(): ObservationDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        /**
         * Migration from version 2 to 3: Added GPS coordinates
         * Adds latitude and longitude columns to hikes table.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE hikes ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE hikes ADD COLUMN longitude REAL")
            }
        }

        /**
         * Migration from version 3 to 4: Added calendar tracking
         * Adds addedToCalendar column to track if hike was added to calendar.
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE hikes ADD COLUMN addedToCalendar INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * Singleton pattern: Returns single database instance for entire app.
         * Thread-safe using synchronized block.
         *
         * - addMigrations: Apply schema changes without losing data
         * - fallbackToDestructiveMigration: If migration fails, recreate database
         */
        fun getInstance(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "mhike.db"
            )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
        }
    }
}
