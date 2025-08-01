package com.example.financetracker;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;


    @Dao
    public interface UserDao {
        @Insert
        long insert(User user);

        @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
        User getUserByEmail(String email);

        @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
        User authenticate(String email, String password);


    }

