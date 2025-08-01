package com.example.financetracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {
    private EditText etEmail, etPassword, etConfirmPassword;
    private AuthHelper authHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        authHelper = new AuthHelper(this);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnSignup = findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(v -> attemptRegistration());
    }

    private void attemptRegistration() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords don't match");
            return;
        }

        // Show loading indicator
        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Registering...");
        progress.setCancelable(false);
        progress.show();

        // Create new user
        User newUser = new User();
        newUser.email = email;
        newUser.password = password; // Will be hashed in AuthHelper

        // Use RegistrationCallback instead of AuthCallback
        authHelper.register(newUser, new AuthHelper.RegistrationCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    progress.dismiss();
                    Toast.makeText(SignupActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    finish(); // Return to login activity
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progress.dismiss();
                    Toast.makeText(SignupActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}