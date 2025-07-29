package com.example.financetracker;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    public String category;
    public double amount;

    @ColumnInfo(name = "is_expense")
    public boolean isExpense;

    @ColumnInfo(name = "formatted_date")
    public String formattedDate;

    @ColumnInfo(name = "created_at")
    public long createdAt = System.currentTimeMillis();
}