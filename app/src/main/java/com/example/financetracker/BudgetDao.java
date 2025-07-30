package com.example.financetracker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BudgetDao {
    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Budget budget);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertWithConflict(Budget budget);

    // Read operations
    @Transaction
    @Query("SELECT * FROM budgets WHERE category = :category LIMIT 1")
    Budget getBudgetByCategory(String category);

    @Query("SELECT * FROM budgets")
    List<Budget> getAllBudgets();

    @Query("SELECT * FROM budgets WHERE id = :id")
    Budget getBudgetById(int id);

    // Update operations
    @Update(onConflict = OnConflictStrategy.REPLACE)
    int update(Budget budget);

    @Query("UPDATE budgets SET current_spending = current_spending + :amount WHERE category = :category")
    int addToSpending(String category, double amount);

    // Delete operations
    @Delete
    int delete(Budget budget);

    @Query("DELETE FROM budgets WHERE category = :category")
    int deleteByCategory(String category);

    // Debugging and utility operations
    @Query("SELECT COUNT(*) FROM budgets")
    int getCount();

    @Query("SELECT changes() AS affected_rows")
    int getLastOperationAffectedRows();

    @Query("DELETE FROM budgets")
    void deleteAll();

    @Query("SELECT EXISTS(SELECT 1 FROM budgets WHERE category = :category LIMIT 1)")
    boolean exists(String category);
}