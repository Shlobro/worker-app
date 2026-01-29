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
        Payment::class,
        Employer::class
    ],
    version = 24,
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
    abstract fun employerDao(): EmployerDao

    companion object {
        @Volatile
        private var INSTANCE: WorkerTrackingDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Remove the referencePercentage column and rename contactInfo to phoneNumber
                db.execSQL("CREATE TABLE workers_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, phoneNumber TEXT NOT NULL, referenceId INTEGER, FOREIGN KEY(referenceId) REFERENCES workers(id) ON DELETE SET NULL)")
                db.execSQL("INSERT INTO workers_new (id, name, phoneNumber, referenceId) SELECT id, name, contactInfo, referenceId FROM workers")
                db.execSQL("DROP TABLE workers")
                db.execSQL("ALTER TABLE workers_new RENAME TO workers")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add globalPayRate and hourlyPayRate columns to workers table
                db.execSQL("ALTER TABLE workers ADD COLUMN globalPayRate REAL")
                db.execSQL("ALTER TABLE workers ADD COLUMN hourlyPayRate REAL")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create project_workers table for many-to-many relationship with pay rates
                db.execSQL("""
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
            override fun migrate(db: SupportSQLiteDatabase) {
                // Remove project_workers table and worker payment rate columns
                db.execSQL("DROP TABLE IF EXISTS project_workers")
                
                // Remove payment rate columns from workers table
                db.execSQL("CREATE TABLE workers_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, phoneNumber TEXT NOT NULL, referenceId INTEGER, FOREIGN KEY(referenceId) REFERENCES workers(id) ON DELETE SET NULL)")
                db.execSQL("INSERT INTO workers_new (id, name, phoneNumber, referenceId) SELECT id, name, phoneNumber, referenceId FROM workers")
                db.execSQL("DROP TABLE workers")
                db.execSQL("ALTER TABLE workers_new RENAME TO workers")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create shift_workers table
                db.execSQL("""
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
                db.execSQL("""
                    INSERT INTO shift_workers (shiftId, workerId, isHourlyRate, payRate)
                    SELECT id, workerId, 1, payRate FROM shifts WHERE workerId IS NOT NULL
                """)
                
                // Update shifts table to remove workerId and payRate columns
                db.execSQL("""
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
                db.execSQL("INSERT INTO shifts_new (id, projectId, date, startTime, endTime, hours) SELECT id, projectId, date, startTime, endTime, hours FROM shifts")
                db.execSQL("DROP TABLE shifts")
                db.execSQL("ALTER TABLE shifts_new RENAME TO shifts")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create project_income table
                db.execSQL("""
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
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add name column to shifts table
                db.execSQL("ALTER TABLE shifts ADD COLUMN name TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add status and endDate columns to projects table
                db.execSQL("ALTER TABLE projects ADD COLUMN status TEXT NOT NULL DEFAULT 'ACTIVE'")
                db.execSQL("ALTER TABLE projects ADD COLUMN endDate INTEGER")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Remove incomeType and incomeAmount columns from projects table
                db.execSQL("""
                    CREATE TABLE projects_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        location TEXT NOT NULL,
                        startDate INTEGER NOT NULL,
                        status TEXT NOT NULL DEFAULT 'ACTIVE',
                        endDate INTEGER
                    )
                """)
                db.execSQL("INSERT INTO projects_new (id, name, location, startDate, status, endDate) SELECT id, name, location, startDate, status, endDate FROM projects")
                db.execSQL("DROP TABLE projects")
                db.execSQL("ALTER TABLE projects_new RENAME TO projects")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Update events table to replace time with startTime, endTime, and hours
                db.execSQL("""
                    CREATE TABLE events_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        date INTEGER NOT NULL,
                        startTime TEXT NOT NULL DEFAULT '',
                        endTime TEXT NOT NULL DEFAULT '',
                        hours TEXT NOT NULL DEFAULT ''
                    )
                """)
                db.execSQL("INSERT INTO events_new (id, name, date, startTime) SELECT id, name, date, time FROM events")
                db.execSQL("DROP TABLE events")
                db.execSQL("ALTER TABLE events_new RENAME TO events")
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add referencePayRate column to shift_workers table
                db.execSQL("ALTER TABLE shift_workers ADD COLUMN referencePayRate REAL")
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add income column to events table
                db.execSQL("ALTER TABLE events ADD COLUMN income REAL NOT NULL DEFAULT 0.0")
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add photoUris column to workers table
                db.execSQL("ALTER TABLE workers ADD COLUMN photoUris TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add isPaid column to shift_workers and event_workers tables
                db.execSQL("ALTER TABLE shift_workers ADD COLUMN isPaid INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE event_workers ADD COLUMN isPaid INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add referencePayRate column to event_workers table
                db.execSQL("ALTER TABLE event_workers ADD COLUMN referencePayRate REAL")
            }
        }

        private val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add isHourlyRate column to event_workers table (default to true to maintain existing behavior)
                db.execSQL("ALTER TABLE event_workers ADD COLUMN isHourlyRate INTEGER NOT NULL DEFAULT 1")
            }
        }

        private val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create employers table
                db.execSQL("""
                    CREATE TABLE employers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        phoneNumber TEXT NOT NULL
                    )
                """)
                
                // Add employerId column to projects table
                db.execSQL("ALTER TABLE projects ADD COLUMN employerId INTEGER REFERENCES employers(id) ON DELETE SET NULL")
                
                // Add employerId column to events table
                db.execSQL("ALTER TABLE events ADD COLUMN employerId INTEGER REFERENCES employers(id) ON DELETE SET NULL")
            }
        }

        private val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add amountPaid and tipAmount columns to event_workers table
                db.execSQL("ALTER TABLE event_workers ADD COLUMN amountPaid REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE event_workers ADD COLUMN tipAmount REAL NOT NULL DEFAULT 0.0")
            }
        }

        private val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add isReferencePaid, referenceAmountPaid, referenceTipAmount to event_workers table
                db.execSQL("ALTER TABLE event_workers ADD COLUMN isReferencePaid INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE event_workers ADD COLUMN referenceAmountPaid REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE event_workers ADD COLUMN referenceTipAmount REAL NOT NULL DEFAULT 0.0")
            }
        }

        private val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add unique constraint to prevent duplicate worker assignments in event_workers
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_event_workers_eventId_workerId ON event_workers(eventId, workerId)")

                // Add unique constraint to prevent duplicate worker assignments in shift_workers
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_shift_workers_shiftId_workerId ON shift_workers(shiftId, workerId)")
            }
        }

        private val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add indices on foreign key columns to improve query performance
                db.execSQL("CREATE INDEX IF NOT EXISTS index_events_employerId ON events(employerId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_workerId ON payments(workerId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_projects_employerId ON projects(employerId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_project_income_projectId ON project_income(projectId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_shifts_projectId ON shifts(projectId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_workers_referenceId ON workers(referenceId)")
            }
        }

        private val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add granular payment fields to shift_workers table to match event_workers
                db.execSQL("ALTER TABLE shift_workers ADD COLUMN amountPaid REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE shift_workers ADD COLUMN tipAmount REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE shift_workers ADD COLUMN isReferencePaid INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE shift_workers ADD COLUMN referenceAmountPaid REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE shift_workers ADD COLUMN referenceTipAmount REAL NOT NULL DEFAULT 0.0")
            }
        }

        private val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Preserve historical records when worker is deleted by changing CASCADE to SET NULL
                // This requires recreating tables as SQLite doesn't support ALTER CONSTRAINT

                // Recreate shift_workers table with SET NULL on worker delete
                db.execSQL("""
                    CREATE TABLE shift_workers_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        shiftId INTEGER NOT NULL,
                        workerId INTEGER,
                        isHourlyRate INTEGER NOT NULL,
                        payRate REAL NOT NULL,
                        referencePayRate REAL,
                        isPaid INTEGER NOT NULL DEFAULT 0,
                        amountPaid REAL NOT NULL DEFAULT 0.0,
                        tipAmount REAL NOT NULL DEFAULT 0.0,
                        isReferencePaid INTEGER NOT NULL DEFAULT 0,
                        referenceAmountPaid REAL NOT NULL DEFAULT 0.0,
                        referenceTipAmount REAL NOT NULL DEFAULT 0.0,
                        FOREIGN KEY(shiftId) REFERENCES shifts(id) ON DELETE CASCADE,
                        FOREIGN KEY(workerId) REFERENCES workers(id) ON DELETE SET NULL
                    )
                """)
                db.execSQL("""
                    INSERT INTO shift_workers_new
                    SELECT id, shiftId, workerId, isHourlyRate, payRate, referencePayRate,
                           isPaid, amountPaid, tipAmount, isReferencePaid, referenceAmountPaid, referenceTipAmount
                    FROM shift_workers
                """)
                db.execSQL("DROP TABLE shift_workers")
                db.execSQL("ALTER TABLE shift_workers_new RENAME TO shift_workers")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_shift_workers_shiftId_workerId ON shift_workers(shiftId, workerId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_shift_workers_workerId ON shift_workers(workerId)")

                // Recreate event_workers table with SET NULL on worker delete
                db.execSQL("""
                    CREATE TABLE event_workers_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        eventId INTEGER NOT NULL,
                        workerId INTEGER,
                        hours REAL NOT NULL,
                        isHourlyRate INTEGER NOT NULL,
                        payRate REAL NOT NULL,
                        referencePayRate REAL,
                        isPaid INTEGER NOT NULL DEFAULT 0,
                        amountPaid REAL NOT NULL DEFAULT 0.0,
                        tipAmount REAL NOT NULL DEFAULT 0.0,
                        isReferencePaid INTEGER NOT NULL DEFAULT 0,
                        referenceAmountPaid REAL NOT NULL DEFAULT 0.0,
                        referenceTipAmount REAL NOT NULL DEFAULT 0.0,
                        FOREIGN KEY(eventId) REFERENCES events(id) ON DELETE CASCADE,
                        FOREIGN KEY(workerId) REFERENCES workers(id) ON DELETE SET NULL
                    )
                """)
                db.execSQL("""
                    INSERT INTO event_workers_new
                    SELECT id, eventId, workerId, hours, isHourlyRate, payRate, referencePayRate,
                           isPaid, amountPaid, tipAmount, isReferencePaid, referenceAmountPaid, referenceTipAmount
                    FROM event_workers
                """)
                db.execSQL("DROP TABLE event_workers")
                db.execSQL("ALTER TABLE event_workers_new RENAME TO event_workers")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_event_workers_eventId_workerId ON event_workers(eventId, workerId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_event_workers_workerId ON event_workers(workerId)")

                // Recreate payments table with SET NULL on worker delete
                db.execSQL("""
                    CREATE TABLE payments_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        workerId INTEGER,
                        amount REAL NOT NULL,
                        datePaid INTEGER NOT NULL,
                        sourceType TEXT NOT NULL,
                        sourceId INTEGER,
                        FOREIGN KEY(workerId) REFERENCES workers(id) ON DELETE SET NULL
                    )
                """)
                db.execSQL("INSERT INTO payments_new SELECT * FROM payments")
                db.execSQL("DROP TABLE payments")
                db.execSQL("ALTER TABLE payments_new RENAME TO payments")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_workerId ON payments(workerId)")
            }
        }

        fun getDatabase(context: Context): WorkerTrackingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkerTrackingDatabase::class.java,
                    "worker_tracking_database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24).build()
                INSTANCE = instance
                instance
            }
        }
    }
}