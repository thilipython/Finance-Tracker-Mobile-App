package com.example.financetracker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


@Dao
public interface TransactionDao {
    @Insert
    long insert(Transaction transaction);

    @Query("SELECT * FROM transactions")
    List<Transaction> getAll();


    @Query("SELECT COUNT(*) FROM transactions")
    int getCount();

    @Delete
    void delete(Transaction transaction);

    @Query("SELECT * FROM transactions WHERE id = :id")
    Transaction getById(int id);

    @Update
    void update(Transaction transaction);

    @Query("SELECT * FROM transactions WHERE goal_id = :goalId")
    List<Transaction> getTransactionsForGoal(int goalId);
}