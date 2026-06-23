package com.example.eventpass.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The Room database for EventPass. Holds the single [Attendee] table.
 *
 * We seed a handful of sample attendees the very first time the database is
 * created so the app is usable on first launch (dashboard counts, list, and
 * scannable QR ids all work out of the box).
 */
@Database(
    entities = [Attendee::class],
    version = 1,
    exportSchema = false
)
abstract class EventPassDatabase : RoomDatabase() {

    abstract fun attendeeDao(): AttendeeDao

    companion object {
        @Volatile
        private var INSTANCE: EventPassDatabase? = null

        /**
         * Returns the singleton database instance, creating it if needed.
         *
         * @param context     application context
         * @param scope       application-scoped coroutine scope used to seed data
         */
        fun getInstance(context: Context, scope: CoroutineScope): EventPassDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EventPassDatabase::class.java,
                    "eventpass.db"
                )
                    .addCallback(SeedCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /** Sample data inserted once, right after the DB file is first created. */
        val SEED_ATTENDEES = listOf(
            Attendee("101", "Aarav Sharma", "aarav.sharma@example.com", "VIP"),
            Attendee("102", "Diya Patel", "diya.patel@example.com", "General"),
            Attendee("103", "Vivaan Reddy", "vivaan.reddy@example.com", "General"),
            Attendee("104", "Ananya Iyer", "ananya.iyer@example.com", "Speaker"),
            Attendee("105", "Kabir Singh", "kabir.singh@example.com", "VIP"),
            Attendee("106", "Saanvi Gupta", "saanvi.gupta@example.com", "General"),
            Attendee("107", "Arjun Nair", "arjun.nair@example.com", "General"),
            Attendee("108", "Myra Joshi", "myra.joshi@example.com", "Student"),
            Attendee("109", "Reyansh Das", "reyansh.das@example.com", "Student"),
            Attendee("110", "Ishaan Mehta", "ishaan.mehta@example.com", "General")
        )
    }

    /**
     * Room callback that runs exactly once when the database is first created.
     * It populates the table with [SEED_ATTENDEES] on a background coroutine.
     */
    private class SeedCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    database.attendeeDao().upsertAll(SEED_ATTENDEES)
                }
            }
        }
    }
}
