package com.example.financetracker;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddTransactionActivity extends AppCompatActivity {

    private EditText etTitle, etAmount, etAllocationAmount;
    private MaterialAutoCompleteTextView actvCategory, spinnerGoals;
    private RadioGroup radioGroupType;
    private LinearLayout goalAllocationSection;
    private TextInputLayout goalDropdownLayout;
    private AppDatabase db;
    private boolean isEditMode = false;
    private int transactionId = -1;
    private List<SavingsGoal> goalsList;
    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        initializeViews();
        db = AppDatabase.getInstance(getApplicationContext());

        checkEditMode();
        setupCategoryDropdown();
        setupRadioGroupListener();
        setupSaveButton();
        setupAllocationAmountListener();
    }

    private void initializeViews() {
        etTitle = findViewById(R.id.etTitle);
        etAmount = findViewById(R.id.etAmount);
        actvCategory = findViewById(R.id.actvCategory);
        radioGroupType = findViewById(R.id.radioGroupType);
        goalAllocationSection = findViewById(R.id.goalAllocationSection);
        goalDropdownLayout = findViewById(R.id.goalDropdownLayout);
        spinnerGoals = findViewById(R.id.spinnerGoals);
        etAllocationAmount = findViewById(R.id.etAllocationAmount);
    }

    private void checkEditMode() {
        if (getIntent().hasExtra("EDIT_MODE")) {
            isEditMode = true;
            transactionId = getIntent().getIntExtra("TRANSACTION_ID", -1);
            new LoadTransactionTask().execute();
        }
    }

    private void setupCategoryDropdown() {
        String[] categories = getResources().getStringArray(R.array.categories_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        actvCategory.setAdapter(adapter);
        actvCategory.setThreshold(1);
    }

    private void setupRadioGroupListener() {
        radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioIncome) {
                goalAllocationSection.setVisibility(View.VISIBLE);
                loadGoals();
            } else {
                goalAllocationSection.setVisibility(View.GONE);
            }
        });
    }

    private void setupSaveButton() {
        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void setupAllocationAmountListener() {
        etAllocationAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateAllocationAmount();
            }
        });
    }

    private void validateAllocationAmount() {
        try {
            if (etAmount.getText().toString().isEmpty() || etAllocationAmount.getText().toString().isEmpty()) {
                return;
            }

            double amount = Double.parseDouble(etAmount.getText().toString());
            double allocation = Double.parseDouble(etAllocationAmount.getText().toString());

            if (allocation > amount) {
                etAllocationAmount.setError("Allocation cannot exceed transaction amount");
            } else {
                etAllocationAmount.setError(null);
            }
        } catch (NumberFormatException e) {
            // Ignore invalid inputs
        }
    }

    private void loadGoals() {
        executor.execute(() -> {
            try {
                goalsList = db.savingsGoalDao().getAllGoals();
                runOnUiThread(() -> {
                    if (goalsList == null || goalsList.isEmpty()) {
                        goalDropdownLayout.setVisibility(View.GONE);
                        Toast.makeText(this, "No savings goals available", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    goalDropdownLayout.setVisibility(View.VISIBLE);
                    setupGoalsAdapter();
                });
            } catch (Exception e) {
                Log.e("LoadGoals", "Error loading goals", e);
                runOnUiThread(() -> {
                    goalDropdownLayout.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading goals", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setupGoalsAdapter() {
        ArrayAdapter<SavingsGoal> adapter = new ArrayAdapter<SavingsGoal>(
                this,
                R.layout.dropdown_item_goal,
                R.id.tvGoalName,
                goalsList
        ) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textName = view.findViewById(R.id.tvGoalName);
                TextView textProgress = view.findViewById(R.id.tvGoalProgress);

                SavingsGoal goal = getItem(position);
                if (goal != null) {
                    textName.setText(goal.name);
                    double progress = (goal.currentAmount / goal.targetAmount) * 100;
                    textProgress.setText(String.format(Locale.getDefault(), "%.1f%%", progress));
                }
                return view;
            }
        };

        spinnerGoals.setAdapter(adapter);
        spinnerGoals.setThreshold(1);
        spinnerGoals.setOnItemClickListener((parent, view, position, id) -> {
            SavingsGoal selected = adapter.getItem(position);
            if (selected != null) {
                updateAllocationField(selected);
            }
        });
    }

    private void updateAllocationField(SavingsGoal goal) {
        double remaining = goal.targetAmount - goal.currentAmount;
        try {
            double amount = Double.parseDouble(etAmount.getText().toString());
            if (remaining > 0) {
                double suggestedAllocation = Math.min(remaining, amount);
                etAllocationAmount.setText(String.format(Locale.getDefault(), "%.2f", suggestedAllocation));
            }
        } catch (NumberFormatException e) {
            // Amount not entered yet
        }
    }

    private class LoadTransactionTask extends AsyncTask<Void, Void, Transaction> {
        @Override
        protected Transaction doInBackground(Void... voids) {
            return db.transactionDao().getById(transactionId);
        }

        @Override
        protected void onPostExecute(Transaction transaction) {
            if (transaction != null) {
                populateTransactionFields(transaction);
            }
        }
    }

    private void populateTransactionFields(Transaction transaction) {
        etTitle.setText(transaction.title);
        etAmount.setText(String.valueOf(Math.abs(transaction.amount)));
        actvCategory.setText(transaction.category);

        RadioButton radioToCheck = transaction.isExpense ?
                findViewById(R.id.radioExpense) :
                findViewById(R.id.radioIncome);
        radioToCheck.setChecked(true);

        if (transaction.isGoalDeposit) {
            goalAllocationSection.setVisibility(View.VISIBLE);
            loadGoalsAndSelect(transaction.goalId, transaction.amount);
        }
    }

    private void loadGoalsAndSelect(int goalId, double amount) {
        executor.execute(() -> {
            List<SavingsGoal> goals = db.savingsGoalDao().getAllGoals();
            runOnUiThread(() -> {
                if (goals != null && !goals.isEmpty()) {
                    for (SavingsGoal goal : goals) {
                        if (goal.id == goalId) {
                            spinnerGoals.setText(goal.name, false);
                            break;
                        }
                    }
                    etAllocationAmount.setText(String.valueOf(Math.abs(amount)));
                }
            });
        });
    }

    private void saveTransaction() {
        if (!validateInputs()) {
            return;
        }

        try {
            Transaction transaction = createTransactionFromInputs();
            if (isEditMode) {
                transaction.id = transactionId;
                new UpdateTransactionTask(transaction).execute();
            } else {
                new SaveTransactionTask(transaction).execute();
            }
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
            Log.e("SaveTransaction", "Invalid amount input", e);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs() {
        String title = etTitle.getText().toString().trim();
        String amountText = etAmount.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            return false;
        }

        if (category.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (amountText.isEmpty()) {
            etAmount.setError("Amount is required");
            return false;
        }

        return true;
    }

    private Transaction createTransactionFromInputs() {
        String title = etTitle.getText().toString().trim();
        double amount = Double.parseDouble(etAmount.getText().toString());
        String category = actvCategory.getText().toString().trim();
        boolean isExpense = radioGroupType.getCheckedRadioButtonId() == R.id.radioExpense;
        double transactionAmount = isExpense ? -Math.abs(amount) : Math.abs(amount);

        Transaction transaction = new Transaction();
        transaction.title = title;
        transaction.amount = transactionAmount;
        transaction.category = category;
        transaction.isExpense = isExpense;
        transaction.formattedDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(new Date());

        if (!isExpense && goalAllocationSection.getVisibility() == View.VISIBLE) {
            setupGoalAllocation(transaction);
        }

        return transaction;
    }

    private void setupGoalAllocation(Transaction transaction) {
        String allocationText = etAllocationAmount.getText().toString().trim();
        if (allocationText.isEmpty()) {
            transaction.isGoalDeposit = false;
            return;
        }

        double allocationAmount = Double.parseDouble(allocationText);
        if (allocationAmount <= 0) {
            transaction.isGoalDeposit = false;
            return;
        }

        String selectedGoalName = spinnerGoals.getText().toString();
        if (selectedGoalName.isEmpty() || goalsList == null) {
            throw new IllegalArgumentException("Please select a savings goal");
        }

        for (SavingsGoal goal : goalsList) {
            if (goal.name.equals(selectedGoalName)) {
                transaction.isGoalDeposit = true;
                transaction.goalId = goal.id;


                if ((goal.currentAmount + allocationAmount) > goal.targetAmount) {
                    throw new IllegalArgumentException("Allocation would exceed goal target");
                }


                transaction.amount = allocationAmount;
                break;
            }
        }
    }

    private class SaveTransactionTask extends AsyncTask<Void, Void, Boolean> {
        private final Transaction transaction;
        private Exception exception;

        SaveTransactionTask(Transaction transaction) {
            this.transaction = transaction;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                db.runInTransaction(() -> {
                    long transactionId = db.transactionDao().insert(transaction);

                    if (transaction.isExpense) {
                        db.budgetDao().addToSpending(transaction.category, Math.abs(transaction.amount));
                    } else if (transaction.isGoalDeposit) {
                        updateGoalAllocation(transaction);
                    }
                });
                return true;
            } catch (Exception e) {
                this.exception = e;
                Log.e("SaveTransaction", "Error saving transaction", e);
                return false;
            }
        }

        private void updateGoalAllocation(Transaction transaction) {
            SavingsGoal goal = db.savingsGoalDao().getGoalById(transaction.goalId);
            if (goal != null) {
                goal.currentAmount += Math.abs(transaction.amount); // Use the allocated amount
                db.savingsGoalDao().update(goal);
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
                        "Failed to save transaction" +
                                (exception != null ? ": " + exception.getMessage() : ""),
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
                db.runInTransaction(() -> {
                    db.transactionDao().update(transaction);

                    if (transaction.isGoalDeposit) {
                        updateGoalAllocation(transaction);
                    }
                });
                return true;
            } catch (Exception e) {
                this.exception = e;
                Log.e("UpdateTransaction", "Error updating transaction", e);
                return false;
            }
        }

        private void updateGoalAllocation(Transaction transaction) {
            SavingsGoal goal = db.savingsGoalDao().getGoalById(transaction.goalId);
            if (goal != null) {
                goal.currentAmount += Math.abs(transaction.amount);
                db.savingsGoalDao().update(goal);
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
                        "Failed to update transaction" +
                                (exception != null ? ": " + exception.getMessage() : ""),
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}