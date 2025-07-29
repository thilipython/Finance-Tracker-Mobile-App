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

        // Setup category dropdown
        setupCategoryDropdown();

        // Set save button click listener
        btnSave.setOnClickListener(v -> saveBudget());
    }

    private void setupCategoryDropdown() {
        // Get categories from resources
        String[] categories = getResources().getStringArray(R.array.categories_array);

        // Create and set adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        actvCategory.setAdapter(adapter);
    }

    private void saveBudget() {
        String title = etTitle.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();
        String limitText = etLimit.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Please enter a title");
            return;
        }

        if (category.isEmpty()) {
            Toast.makeText(this, "Please select a category type", Toast.LENGTH_SHORT).show();
            return;
        }

        if (limitText.isEmpty()) {
            etLimit.setError("Please enter a limit");
            return;
        }

        try {
            double limit = Double.parseDouble(limitText);
            Budget budget = new Budget();
            budget.title = title;  // Add this field to your Budget entity
            budget.category = category;
            budget.limit = limit;
            budget.currentSpending = 0;

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

        } catch (NumberFormatException e) {
            etLimit.setError("Invalid amount format");
        }
    }
}