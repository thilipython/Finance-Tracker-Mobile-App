package com.example.financetracker;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "savings_goals")
public class SavingsGoal {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String description;

    @ColumnInfo(name = "target_amount")
    public double targetAmount;

    @ColumnInfo(name = "current_amount")
    public double currentAmount = 0;

    @ColumnInfo(name = "target_date")
    public long targetDate;

    @ColumnInfo(name = "created_at")
    public long createdAt = System.currentTimeMillis();

    @Override
    public String toString() {
        return name;
    }
}