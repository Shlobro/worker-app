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
        ProjectIncome::class,
        Worker::class,
        Shift::class,
        ShiftWorker::class,
        Event::class,
        EventWorker::class,
        Payment::class
    ],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WorkerTrackingDatabase : RoomDatabase() {
    
    abstract fun projectDao(): ProjectDao
    abstract fun projectIncomeDao(): ProjectIncomeDao
    abstract fun workerDao(): WorkerDao
    abstract fun shiftDao(): ShiftDao
    abstract fun shiftWorkerDao(): ShiftWorkerDao
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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add globalPayRate and hourlyPayRate columns to workers table
                database.execSQL("ALTER TABLE workers ADD COLUMN globalPayRate REAL")
                database.execSQL("ALTER TABLE workers ADD COLUMN hourlyPayRate REAL")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create project_workers table for many-to-many relationship with pay rates
                database.execSQL("""
                    CREATE TABLE project_workers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        projectId INTEGER NOT NULL,
                        workerId INTEGER NOT NULL,
                        payRate REAL NOT NULL,
                        FOREIGN KEY(projectId) REFERENCES projects(id) ON DELETE CASCADE,
                        FOREIGN KEY(workerId) REFERENCES workers(id) ON DELETE CASCADE
                    )
                """)
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Remove project_workers table and worker payment rate columns
                database.execSQL("DROP TABLE IF EXISTS project_workers")
                
                // Remove payment rate columns from workers table
                database.execSQL("CREATE TABLE workers_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, phoneNumber TEXT NOT NULL, referenceId INTEGER, FOREIGN KEY(referenceId) REFERENCES workers(id) ON DELETE SET NULL)")
                database.execSQL("INSERT INTO workers_new (id, name, phoneNumber, referenceId) SELECT id, name, phoneNumber, referenceId FROM workers")
                database.execSQL("DROP TABLE workers")
                database.execSQL("ALTER TABLE workers_new RENAME TO workers")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create shift_workers table
                database.execSQL("""
                    CREATE TABLE shift_workers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        shiftId INTEGER NOT NULL,
                        workerId INTEGER NOT NULL,
                        isHourlyRate INTEGER NOT NULL,
                        payRate REAL NOT NULL,
                        FOREIGN KEY(shiftId) REFERENCES shifts(id) ON DELETE CASCADE,
                        FOREIGN KEY(workerId) REFERENCES workers(id) ON DELETE CASCADE
                    )
                """)
                
                // Migrate existing shift data to new structure
                database.execSQL("""
                    INSERT INTO shift_workers (shiftId, workerId, isHourlyRate, payRate)
                    SELECT id, workerId, 1, payRate FROM shifts WHERE workerId IS NOT NULL
                """)
                
                // Update shifts table to remove workerId and payRate columns
                database.execSQL("""
                    CREATE TABLE shifts_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        projectId INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        startTime TEXT NOT NULL,
                        endTime TEXT NOT NULL,
                        hours REAL NOT NULL,
                        FOREIGN KEY(projectId) REFERENCES projects(id) ON DELETE CASCADE
                    )
                """)
                database.execSQL("INSERT INTO shifts_new (id, projectId, date, startTime, endTime, hours) SELECT id, projectId, date, startTime, endTime, hours FROM shifts")
                database.execSQL("DROP TABLE shifts")
                database.execSQL("ALTER TABLE shifts_new RENAME TO shifts")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create project_income table
                database.execSQL("""
                    CREATE TABLE project_income (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        projectId INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        description TEXT NOT NULL,
                        amount REAL NOT NULL,
                        units REAL NOT NULL DEFAULT 1.0,
                        FOREIGN KEY(projectId) REFERENCES projects(id) ON DELETE CASCADE
                    )
                """)
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add name column to shifts table
                database.execSQL("ALTER TABLE shifts ADD COLUMN name TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add status and endDate columns to projects table
                database.execSQL("ALTER TABLE projects ADD COLUMN status TEXT NOT NULL DEFAULT 'ACTIVE'")
                database.execSQL("ALTER TABLE projects ADD COLUMN endDate INTEGER")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Remove incomeType and incomeAmount columns from projects table
                database.execSQL("""
                    CREATE TABLE projects_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        location TEXT NOT NULL,
                        startDate INTEGER NOT NULL,
                        status TEXT NOT NULL DEFAULT 'ACTIVE',
                        endDate INTEGER
                    )
                """)
                database.execSQL("INSERT INTO projects_new (id, name, location, startDate, status, endDate) SELECT id, name, location, startDate, status, endDate FROM projects")
                database.execSQL("DROP TABLE projects")
                database.execSQL("ALTER TABLE projects_new RENAME TO projects")
            }
        }

        fun getDatabase(context: Context): WorkerTrackingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkerTrackingDatabase::class.java,
                    "worker_tracking_database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10).build()
                INSTANCE = instance
                instance
            }
        }
    }
}