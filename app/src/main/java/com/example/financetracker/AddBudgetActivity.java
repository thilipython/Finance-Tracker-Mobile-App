package com.example.financetracker;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddBudgetActivity extends AppCompatActivity {

    private EditText etTitle, etLimit;
    private AutoCompleteTextView actvCategory;
    private AppDatabase db;
    private Executor executor = Executors.newSingleThreadExecutor();
    private boolean isEditMode = false;
    private int budgetId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_budget);

        // Initialize views
        etTitle = findViewById(R.id.etTitle);
        etLimit = findViewById(R.id.etLimit);
        actvCategory = findViewById(R.id.actvCategory);
        Button btnSave = findViewById(R.id.btnSave);

        // Initialize database
        db = AppDatabase.getInstance(getApplicationContext());

        // Check if in edit mode
        if (getIntent().hasExtra("EDIT_MODE")) {
            isEditMode = true;
            budgetId = getIntent().getIntExtra("BUDGET_ID", -1);
            loadBudgetData();
        }

        // Setup category dropdown
        setupCategoryDropdown();

        // Set save button click listener
        btnSave.setOnClickListener(v -> saveBudget());
    }

    private void setupCategoryDropdown() {
        String[] categories = getResources().getStringArray(R.array.categories_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        actvCategory.setAdapter(adapter);
    }

    private void loadBudgetData() {
        executor.execute(() -> {
            Budget budget = db.budgetDao().getBudgetById(budgetId);
            runOnUiThread(() -> {
                if (budget != null) {
                    etTitle.setText(budget.title);
                    etLimit.setText(String.valueOf(budget.limit));
                    actvCategory.setText(budget.category);
                }
            });
        });
    }

    private void saveBudget() {
        String title = etTitle.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();
        String limitText = etLimit.getText().toString().trim();

        // Validation (unchanged)
        if (title.isEmpty() || category.isEmpty() || limitText.isEmpty()) {
            return;
        }

        try {
            double limit = Double.parseDouble(limitText);
            Budget budget = new Budget();
            budget.title = title;
            budget.category = category;
            budget.limit = limit;
            budget.currentSpending = 0; // Initialize spending

            // Save to database
            executor.execute(() -> {
                try {
                    db.budgetDao().insert(budget);

                    // Ensure UI updates on main thread
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Budget saved successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish(); // Close activity after save
                    });
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error saving budget", Toast.LENGTH_SHORT).show());
                }
            });
        } catch (NumberFormatException e) {
            etLimit.setError("Invalid amount");
        }
    }


    private void insertBudget(Budget budget) {
        executor.execute(() -> {
            try {
                db.budgetDao().insert(budget);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Budget saved successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to save budget: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void updateBudget(Budget budget) {
        executor.execute(() -> {
            try {
                db.budgetDao().update(budget);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Budget updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to update budget: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}