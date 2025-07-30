package com.example.financetracker;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {Transaction.class, Budget.class, SavingsGoal.class},
        version = 7,  // Updated to version 7
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static final String TAG = "AppDatabase";
    private static volatile AppDatabase INSTANCE;

    public abstract TransactionDao transactionDao();
    public abstract BudgetDao budgetDao();
    public abstract SavingsGoalDao savingsGoalDao();

    // Migration from version 1 to 2
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // Empty migration - no schema changes
        }
    };

    // Migration from version 2 to 3 (create budgets table)
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

    // Migration from version 3 to 4 (add title to budgets)
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE budgets ADD COLUMN title TEXT");
        }
    };

    // Migration from version 4 to 5 (add title to transactions)
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE transactions ADD COLUMN title TEXT");
        }
    };

    // Migration from version 5 to 6 (create savings_goals table)
    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS savings_goals (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT, description TEXT, " +
                    "target_amount REAL NOT NULL, " +
                    "current_amount REAL NOT NULL DEFAULT 0, " +
                    "target_date INTEGER NOT NULL, " +
                    "created_at INTEGER NOT NULL)");
        }
    };

    // Migration from version 6 to 7 (add goal fields to transactions)
    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE transactions ADD COLUMN is_goal_deposit INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE transactions ADD COLUMN goal_id INTEGER NOT NULL DEFAULT -1");
        }
    };

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "finance_database.db")
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    Log.d(TAG, "Database created");
                                    createAllTables(db);
                                }
                            })
                            .addMigrations(
                                    MIGRATION_1_2,
                                    MIGRATION_2_3,
                                    MIGRATION_3_4,
                                    MIGRATION_4_5,
                                    MIGRATION_5_6,
                                    MIGRATION_6_7
                            )
                            .fallbackToDestructiveMigration() // Remove for production
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

        Log.i(TAG, "All tables created successfully");
    }

    private static void logStorageState(Context context) {
        try {
            File dbFile = context.getDatabasePath("finance_database.db");
            File dir = dbFile.getParentFile();

            Log.d(TAG, "Database path: " + dbFile.getAbsolutePath());
            Log.d(TAG, "Directory exists: " + dir.exists());
            Log.d(TAG, "Directory writable: " + dir.canWrite());
            Log.d(TAG, "DB file exists: " + dbFile.exists());
        } catch (Exception e) {
            Log.e(TAG, "Storage check failed", e);
        }
    }

    private static void verifyDatabase(AppDatabase db) {
        db.getQueryExecutor().execute(() -> {
            try {
                // Verify schema
                Cursor cursor = db.query("SELECT name FROM sqlite_master WHERE type='table'", null);
                Log.d(TAG, "--- Database Schema ---");
                while (cursor.moveToNext()) {
                    Log.d(TAG, "Table: " + cursor.getString(0));
                }
                cursor.close();

                // Test budget insert
                Budget testBudget = new Budget();
                testBudget.title = "TEST";
                testBudget.category = "DEBUG";
                testBudget.limit = 100;
                db.budgetDao().insert(testBudget);
                Log.i(TAG, "Test budget inserted successfully");

                // Verify counts
                int budgetCount = db.budgetDao().getAllBudgets().size();
                Log.i(TAG, "Budgets in database: " + budgetCount);

                // Test savings goal insert
                SavingsGoal testGoal = new SavingsGoal();
                testGoal.name = "TEST GOAL";
                testGoal.targetAmount = 1000;
                testGoal.targetDate = System.currentTimeMillis();
                db.savingsGoalDao().insert(testGoal);
                Log.i(TAG, "Test savings goal inserted successfully");

                int goalCount = db.savingsGoalDao().getAllGoals().size();
                Log.i(TAG, "Goals in database: " + goalCount);

            } catch (Exception e) {
                Log.e(TAG, "Database verification failed", e);
            }
        });
    }
}