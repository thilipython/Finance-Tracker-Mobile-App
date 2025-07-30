package com.example.financetracker;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private Button btnTransactions, btnBudgets, btnSavings;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);

        // Initialize buttons
        btnTransactions = findViewById(R.id.btnTransactions);
        btnBudgets = findViewById(R.id.btnBudgets);
        btnSavings = findViewById(R.id.btnSavings);

        // Load initial fragment
        loadFragment(new TransactionsFragment(), true);

        // Set click listeners
        btnTransactions.setOnClickListener(v ->
                loadFragment(new TransactionsFragment(), true)
        );

        btnBudgets.setOnClickListener(v ->
                loadFragment(new BudgetsFragment(), false)
        );

        btnSavings.setOnClickListener(v ->
                loadFragment(new SavingsGoalsFragment(), false)
        );
    }

    private void loadFragment(Fragment fragment, boolean isTransaction) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();

        // Update button backgrounds to show active tab
        btnTransactions.setBackgroundResource(
                isTransaction ? R.drawable.tab_selected_bg : R.drawable.tab_unselected_bg);
        btnBudgets.setBackgroundResource(
                fragment instanceof BudgetsFragment ? R.drawable.tab_selected_bg : R.drawable.tab_unselected_bg);
        btnSavings.setBackgroundResource(
                fragment instanceof SavingsGoalsFragment ? R.drawable.tab_selected_bg : R.drawable.tab_unselected_bg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }
}