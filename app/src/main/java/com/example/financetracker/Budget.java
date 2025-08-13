package com.example.financetracker;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "budgets")
public class Budget {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String category;
    public double limit;

    @ColumnInfo(name = "current_spending")
    public double currentSpending = 0;

    @ColumnInfo(name = "created_at")
    public long createdAt = System.currentTimeMillis();
}