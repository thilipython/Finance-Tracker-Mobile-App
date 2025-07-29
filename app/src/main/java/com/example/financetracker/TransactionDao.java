package com.example.financetracker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;


@Dao
public interface TransactionDao {
    @Insert
    void insert(Transaction transaction);

    @Query("SELECT * FROM transactions")
    List<Transaction> getAll();

    // Add this for debugging
    @Query("SELECT COUNT(*) FROM transactions")
    int getCount();

    @Delete
    void delete(Transaction transaction);
}