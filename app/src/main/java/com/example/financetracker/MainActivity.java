package com.example.financetracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {
    private Button btnTransactions, btnBudgets, btnSavings, btnLogout;
    private TextView tvWelcome;
    private AppDatabase db;
    private AuthManager authManager;
    private AuthHelper authHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initializeComponents();
        setupUI();
        setupListeners();
        loadInitialFragment();
    }

    private void initializeComponents() {
        authHelper = new AuthHelper(this);
        authManager = AuthManager.getInstance(this);
        db = AppDatabase.getInstance(this);
    }

    private void setupUI() {
        btnTransactions = findViewById(R.id.btnTransactions);
        btnBudgets = findViewById(R.id.btnBudgets);
        btnSavings = findViewById(R.id.btnSavings);
        btnLogout = findViewById(R.id.btnLogout);
        tvWelcome = findViewById(R.id.tvWelcome);


        String userEmail = authManager.getCurrentUserEmail();
        tvWelcome.setText(userEmail != null ? "Welcome, " + userEmail : "Welcome");
    }

    private void setupListeners() {
        btnTransactions.setOnClickListener(v ->
                loadFragment(new TransactionsFragment(), true)
        );

        btnBudgets.setOnClickListener(v ->
                loadFragment(new BudgetsFragment(), false)
        );

        btnSavings.setOnClickListener(v ->
                loadFragment(new SavingsGoalsFragment(), false)
        );

        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void loadInitialFragment() {
        loadFragment(new TransactionsFragment(), true);
    }

    private void loadFragment(Fragment fragment, boolean isTransaction) {
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();

            updateTabButtons(fragment, isTransaction);
        } catch (Exception e) {
            Toast.makeText(this, "Error loading fragment", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTabButtons(Fragment fragment, boolean isTransaction) {
        btnTransactions.setBackgroundResource(
                isTransaction ? R.drawable.tab_selected_bg : R.drawable.tab_unselected_bg);
        btnBudgets.setBackgroundResource(
                fragment instanceof BudgetsFragment ? R.drawable.tab_selected_bg : R.drawable.tab_unselected_bg);
        btnSavings.setBackgroundResource(
                fragment instanceof SavingsGoalsFragment ? R.drawable.tab_selected_bg : R.drawable.tab_unselected_bg);
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> performLogout())
                .setNegativeButton("No", null)
                .show();
    }

    private void performLogout() {
        authManager.logout();


        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Logging out...");
        progress.setCancelable(false);
        progress.show();

        authHelper.logout(new AuthHelper.LogoutCallback() {
            @Override
            public void onLogoutComplete() {
                runOnUiThread(() -> {
                    progress.dismiss();
                    navigateToLogin();
                });
            }

            @Override
            public void onLogoutError(String message) {
                runOnUiThread(() -> {
                    progress.dismiss();
                    showLogoutError(message);
                });
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showLogoutError(String message) {
        Toast.makeText(this, "Logout failed: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }
}