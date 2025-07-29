package com.example.financetracker;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private Button btnTransactions, btnBudgets;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);
        btnTransactions = findViewById(R.id.btnTransactions);
        btnBudgets = findViewById(R.id.btnBudgets);

        // Load initial fragment
        loadFragment(new TransactionsFragment(), true);

        btnTransactions.setOnClickListener(v ->
                loadFragment(new TransactionsFragment(), true)
        );

        btnBudgets.setOnClickListener(v ->
                loadFragment(new BudgetsFragment(), false)
        );
    }

    private void loadFragment(Fragment fragment, boolean isTransaction) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();

        btnTransactions.setBackgroundResource(
                isTransaction ? R.drawable.tab_selected_bg : R.drawable.tab_unselected_bg);
        btnBudgets.setBackgroundResource(
                isTransaction ? R.drawable.tab_unselected_bg : R.drawable.tab_selected_bg);
    }
}