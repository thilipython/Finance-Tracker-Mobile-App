package com.example.financetracker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SavingsGoalDao {
    @Insert
    void insert(SavingsGoal goal);

    @Update
    void update(SavingsGoal goal);

    @Query("DELETE FROM savings_goals WHERE id = :id")
    void delete(int id);

    @Query("SELECT * FROM savings_goals ORDER BY target_date ASC")
    List<SavingsGoal> getAllGoals();

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    SavingsGoal getGoalById(int id);

    @Delete
    void delete(SavingsGoal goal);
}