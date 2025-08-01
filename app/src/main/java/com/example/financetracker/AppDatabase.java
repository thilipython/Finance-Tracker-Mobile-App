package com.example.financetracker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
        entities = {
                Transaction.class,
                Budget.class,
                SavingsGoal.class,
                User.class
        },
        version = 9,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract TransactionDao transactionDao();
    public abstract BudgetDao budgetDao();
    public abstract SavingsGoalDao savingsGoalDao();
    public abstract UserDao userDao();

    // Migration from version 1 to 2
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // Initial tables were created in version 1
        }
    };

    // Migration from version 2 to 3 (added budgets table)
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS budgets (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "title TEXT, category TEXT, `limit` REAL NOT NULL, " +
                    "current_spending REAL NOT NULL DEFAULT 0, " +
                    "created_at INTEGER NOT NULL DEFAULT 0)");
        }
    };

    // Migration from version 3 to 4
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // No schema changes in this version
        }
    };

    // Migration from version 4 to 5
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // No schema changes in this version
        }
    };

    // Migration from version 5 to 6
    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // No schema changes in this version
        }
    };

    // Migration from version 6 to 7
    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // No schema changes in this version
        }
    };

    // Migration from version 7 to 8
    static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // No schema changes in this version
        }
    };

    // Migration from version 8 to 9 (fixes users table)
    static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // Recreate users table with correct schema
            db.execSQL("DROP TABLE IF EXISTS users");
            db.execSQL("CREATE TABLE users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "email TEXT NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "name TEXT, " +
                    "created_at INTEGER NOT NULL)");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_users_email ON users(email)");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "finance_database.db")
                            .addMigrations(
                                    MIGRATION_1_2,
                                    MIGRATION_2_3,
                                    MIGRATION_3_4,
                                    MIGRATION_4_5,
                                    MIGRATION_5_6,
                                    MIGRATION_6_7,
                                    MIGRATION_7_8,
                                    MIGRATION_8_9
                            )
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    createAllTables(db);
                                    insertDefaultAdminUser(db);
                                }
                            })
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void createAllTables(SupportSQLiteDatabase db) {
        // Create transactions table
        db.execSQL("CREATE TABLE IF NOT EXISTS transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "title TEXT, amount REAL NOT NULL, " +
                "category TEXT, isExpense INTEGER NOT NULL, " +
                "formattedDate TEXT, " +
                "is_goal_deposit INTEGER NOT NULL DEFAULT 0, " +
                "goal_id INTEGER NOT NULL DEFAULT -1)");

        // Create budgets table
        db.execSQL("CREATE TABLE IF NOT EXISTS budgets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "title TEXT, category TEXT, " +
                "`limit` REAL NOT NULL, " +
                "current_spending REAL NOT NULL DEFAULT 0, " +
                "created_at INTEGER NOT NULL DEFAULT 0)");

        // Create savings_goals table
        db.execSQL("CREATE TABLE IF NOT EXISTS savings_goals (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "name TEXT, description TEXT, " +
                "target_amount REAL NOT NULL, " +
                "current_amount REAL NOT NULL DEFAULT 0, " +
                "target_date INTEGER NOT NULL, " +
                "created_at INTEGER NOT NULL)");

        // Create users table
        db.execSQL("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "email TEXT NOT NULL, " +
                "password TEXT NOT NULL, " +
                "name TEXT, " +
                "created_at INTEGER NOT NULL)");

        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_users_email ON users(email)");
    }

    private static void insertDefaultAdminUser(SupportSQLiteDatabase db) {
        db.execSQL("INSERT OR IGNORE INTO users (email, password, name, created_at) VALUES " +
                        "(?, ?, ?, ?)",
                new Object[]{
                        "admin@example.com",
                        SecurityUtils.hashPassword("admin123"),
                        "Admin User",
                        System.currentTimeMillis() / 1000
                });
    }
}