package com.example.financetracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        // Initialize AuthHelper
        authHelper = new AuthHelper(this);

        // Set click listeners
        findViewById(R.id.btnSignup).setOnClickListener(v -> attemptRegistration());
        findViewById(R.id.btnLoginRedirect).setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
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

        // Use AuthHelper.RegistrationCallback
        authHelper.register(newUser, new AuthHelper.RegistrationCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    progress.dismiss();
                    Toast.makeText(SignupActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                    // Pass credentials to LoginActivity
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("password", password);
                    startActivity(intent);
                    finish();
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
