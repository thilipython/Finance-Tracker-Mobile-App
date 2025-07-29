package com.example.financetracker;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private EditText etTitle, etAmount;
    private AutoCompleteTextView actvCategory;
    private RadioGroup radioGroupType;
    private AppDatabase db;
    private boolean isEditMode = false;
    private int transactionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Initialize views
        etTitle = findViewById(R.id.etTitle);
        etAmount = findViewById(R.id.etAmount);
        actvCategory = findViewById(R.id.actvCategory);
        radioGroupType = findViewById(R.id.radioGroupType);
        Button btnSave = findViewById(R.id.btnSave);

        // Initialize database
        db = AppDatabase.getInstance(getApplicationContext());

        // Check if in edit mode
        if (getIntent().hasExtra("EDIT_MODE")) {
            isEditMode = true;
            transactionId = getIntent().getIntExtra("TRANSACTION_ID", -1);
            new LoadTransactionTask().execute();
        }

        // Setup category dropdown
        setupCategoryDropdown();

        // Set save button click listener
        btnSave.setOnClickListener(v -> saveTransaction());
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

    private class LoadTransactionTask extends AsyncTask<Void, Void, Transaction> {
        @Override
        protected Transaction doInBackground(Void... voids) {
            return db.transactionDao().getById(transactionId);
        }

        @Override
        protected void onPostExecute(Transaction transaction) {
            if (transaction != null) {
                etTitle.setText(transaction.title);
                etAmount.setText(String.valueOf(Math.abs(transaction.amount)));
                actvCategory.setText(transaction.category);

                RadioButton radioToCheck = transaction.isExpense ?
                        findViewById(R.id.radioExpense) :
                        findViewById(R.id.radioIncome);
                radioToCheck.setChecked(true);
            }
        }
    }

    private void saveTransaction() {
        String title = etTitle.getText().toString().trim();
        String amountText = etAmount.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();

        // Validate inputs
        if (title.isEmpty()) {
            etTitle.setError("Please enter a title");
            return;
        }

        if (category.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amountText.isEmpty()) {
            etAmount.setError("Please enter an amount");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            int selectedId = radioGroupType.getCheckedRadioButtonId();

            if (selectedId == -1) {
                Toast.makeText(this, "Please select transaction type", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isExpense = selectedId == R.id.radioExpense;
            double transactionAmount = isExpense ? -Math.abs(amount) : Math.abs(amount);

            Transaction transaction = new Transaction();
            transaction.title = title;
            transaction.amount = transactionAmount;
            transaction.category = category;
            transaction.isExpense = isExpense;
            transaction.formattedDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(new Date());

            if (isEditMode) {
                transaction.id = transactionId;
                new UpdateTransactionTask(transaction).execute();
            } else {
                new SaveTransactionTask(transaction, isExpense ? amount : 0).execute();
            }

        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount format");
            Log.e("AddTransaction", "Invalid amount input", e);
        }
    }

    private class SaveTransactionTask extends AsyncTask<Void, Void, Boolean> {
        private final Transaction transaction;
        private final double expenseAmount;
        private Exception exception;

        SaveTransactionTask(Transaction transaction, double expenseAmount) {
            this.transaction = transaction;
            this.expenseAmount = expenseAmount;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                db.transactionDao().insert(transaction);
                if (expenseAmount > 0) {
                    db.budgetDao().addToSpending(transaction.category, expenseAmount);
                }
                return true;
            } catch (Exception e) {
                this.exception = e;
                Log.e("SaveTransaction", "Error saving transaction", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(AddTransactionActivity.this,
                        "Transaction saved successfully",
                        Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(AddTransactionActivity.this,
                        "Failed to save transaction: " + (exception != null ? exception.getMessage() : ""),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private class UpdateTransactionTask extends AsyncTask<Void, Void, Boolean> {
        private final Transaction transaction;
        private Exception exception;

        UpdateTransactionTask(Transaction transaction) {
            this.transaction = transaction;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                db.transactionDao().update(transaction);
                return true;
            } catch (Exception e) {
                this.exception = e;
                Log.e("UpdateTransaction", "Error updating transaction", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(AddTransactionActivity.this,
                        "Transaction updated successfully",
                        Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(AddTransactionActivity.this,
                        "Failed to update transaction: " + (exception != null ? exception.getMessage() : ""),
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}