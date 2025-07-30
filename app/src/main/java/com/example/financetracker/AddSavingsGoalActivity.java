package com.example.financetracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddSavingsGoalActivity extends AppCompatActivity {

    private EditText etName, etDescription, etTargetAmount;
    private Button btnDatePicker, btnSave;
    private AppDatabase db;
    private Executor executor = Executors.newSingleThreadExecutor();
    private long selectedDate = System.currentTimeMillis();
    private boolean isEditMode = false;
    private int goalId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_savings_goal);

        initializeViews();
        db = AppDatabase.getInstance(getApplicationContext());

        checkEditMode();
        setupButtonListeners();
    }

    private void initializeViews() {
        etName = findViewById(R.id.etGoalName);
        etDescription = findViewById(R.id.etGoalDescription);
        etTargetAmount = findViewById(R.id.etTargetAmount);
        btnDatePicker = findViewById(R.id.btnDatePicker);
        btnSave = findViewById(R.id.btnSave);

        // Set initial date text
        updateDateButtonText();
    }

    private void checkEditMode() {
        if (getIntent().hasExtra("EDIT_MODE")) {
            isEditMode = true;
            goalId = getIntent().getIntExtra("GOAL_ID", -1);
            loadGoalData();
        }
    }

    private void setupButtonListeners() {
        btnDatePicker.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveGoal());
    }

    private void loadGoalData() {
        executor.execute(() -> {
            SavingsGoal goal = db.savingsGoalDao().getGoalById(goalId);
            runOnUiThread(() -> {
                if (goal != null) {
                    populateGoalFields(goal);
                }
            });
        });
    }

    private void populateGoalFields(SavingsGoal goal) {
        etName.setText(goal.name);
        etDescription.setText(goal.description);
        etTargetAmount.setText(String.valueOf(goal.targetAmount));

        selectedDate = goal.targetDate;
        updateDateButtonText();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDate);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDay);
                    selectedDate = selectedCalendar.getTimeInMillis();
                    updateDateButtonText();
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void updateDateButtonText() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDate);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        btnDatePicker.setText(String.format("Target Date: %d/%d/%d", month, day, year));
    }

    private void saveGoal() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String targetAmountText = etTargetAmount.getText().toString().trim();

        if (!validateInputs(name, targetAmountText)) {
            return;
        }

        try {
            double targetAmount = Double.parseDouble(targetAmountText);
            SavingsGoal goal = createSavingsGoal(name, description, targetAmount);
            saveGoalToDatabase(goal);
        } catch (NumberFormatException e) {
            etTargetAmount.setError("Invalid amount");
        }
    }

    private boolean validateInputs(String name, String targetAmountText) {
        if (name.isEmpty()) {
            etName.setError("Please enter a name");
            return false;
        }

        if (targetAmountText.isEmpty()) {
            etTargetAmount.setError("Please enter target amount");
            return false;
        }

        return true;
    }

    private SavingsGoal createSavingsGoal(String name, String description, double targetAmount) {
        SavingsGoal goal = new SavingsGoal();
        goal.name = name;
        goal.description = description;
        goal.targetAmount = targetAmount;
        goal.targetDate = selectedDate;

        if (isEditMode) {
            goal.id = goalId;
        }

        return goal;
    }

    private void saveGoalToDatabase(SavingsGoal goal) {
        executor.execute(() -> {
            try {
                if (isEditMode) {
                    db.savingsGoalDao().update(goal);
                } else {
                    db.savingsGoalDao().insert(goal);
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "Goal saved successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error saving goal", Toast.LENGTH_SHORT).show());
            }
        });
    }
}