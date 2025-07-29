package com.example.financetracker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface BudgetDao {
    @Insert
    void insert(Budget budget);



    @Query("SELECT * FROM budgets WHERE category = :category LIMIT 1")
    Budget getBudgetByCategory(String category);

    @Query("UPDATE budgets SET current_spending = current_spending + :amount WHERE category = :category")
    int addToSpending(String category, double amount);

    @Delete
    void delete(Budget budget);

    @Query("DELETE FROM budgets WHERE category = :category")
    void deleteByCategory(String category);

    @Query("SELECT * FROM budgets")
    List<Budget> getAllBudgets();
}
