package com.example.workertracking.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.example.workertracking.data.dao.*
import com.example.workertracking.data.entity.*

@Database(
    entities = [
        Project::class,
        Worker::class,
        Shift::class,
        Event::class,
        EventWorker::class,
        Payment::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WorkerTrackingDatabase : RoomDatabase() {
    
    abstract fun projectDao(): ProjectDao
    abstract fun workerDao(): WorkerDao
    abstract fun shiftDao(): ShiftDao
    abstract fun eventDao(): EventDao
    abstract fun eventWorkerDao(): EventWorkerDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: WorkerTrackingDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Remove the referencePercentage column and rename contactInfo to phoneNumber
                database.execSQL("CREATE TABLE workers_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, phoneNumber TEXT NOT NULL, referenceId INTEGER, FOREIGN KEY(referenceId) REFERENCES workers(id) ON DELETE SET NULL)")
                database.execSQL("INSERT INTO workers_new (id, name, phoneNumber, referenceId) SELECT id, name, contactInfo, referenceId FROM workers")
                database.execSQL("DROP TABLE workers")
                database.execSQL("ALTER TABLE workers_new RENAME TO workers")
            }
        }

        fun getDatabase(context: Context): WorkerTrackingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkerTrackingDatabase::class.java,
                    "worker_tracking_database"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}