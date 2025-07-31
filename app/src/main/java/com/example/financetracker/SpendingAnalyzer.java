package com.example.financetracker;

import android.content.Context;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public class SpendingAnalyzer {
    private final AppDatabase db;
    private SpendingPredictor predictor;

    public SpendingAnalyzer(Context context) {
        db = AppDatabase.getInstance(context);
        try {
            predictor = new SpendingPredictor(context);
        } catch (IOException e) {
            predictor = null;
        }
    }

    public String analyze() {
        List<Transaction> transactions = db.transactionDao().getAll();

        // 1. Calculate totals
        double totalSpent = 0;
        double totalIncome = 0;
        HashMap<String, Double> categorySpending = new HashMap<>();

        for (Transaction t : transactions) {
            if (t.amount < 0) {
                double amount = Math.abs(t.amount);
                totalSpent += amount;
                categorySpending.put(t.category,
                        categorySpending.getOrDefault(t.category, 0.0) + amount);
            } else {
                totalIncome += t.amount;
            }
        }

        // 2. Generate insights
        StringBuilder insights = new StringBuilder();
        insights.append("💸 Total Spent: £").append(String.format("%.2f", totalSpent))
                .append("\n💰 Total Income: £").append(String.format("%.2f", totalIncome))
                .append("\n\n");

        // 3. Top categories analysis
        if (!categorySpending.isEmpty()) {
            insights.append("📊 Spending Breakdown:\n");
            List<Map.Entry<String, Double>> sortedCategories =
                    new ArrayList<>(categorySpending.entrySet());
            sortedCategories.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            int count = Math.min(3, sortedCategories.size());
            for (int i = 0; i < count; i++) {
                Map.Entry<String, Double> entry = sortedCategories.get(i);
                String category = entry.getKey();
                double amount = entry.getValue();
                double percentage = (amount / totalSpent) * 100;

                insights.append("• ").append(category).append(": £")
                        .append(String.format("%.2f", amount))
                        .append(" (").append(String.format("%.1f", percentage)).append("%)\n");
            }
        }

        // 4. AI Prediction
        if (predictor != null && transactions.size() >= 3) {
            try {
                float prediction = predictor.predictNextMonthSpending(
                        getLastNTransactions(transactions, 3));
                insights.append("\n Next month expenditure prediction ≈ £")
                        .append(String.format("%.2f", prediction));
            } catch (Exception e) {
                insights.append("\n⚠️ Prediction unavailable");
            }
        }

        // 5. Savings advice
        if (totalSpent > 0 && totalIncome > 0) {
            double savingsRate = ((totalIncome - totalSpent) / totalIncome) * 100;
            insights.append("\n\n💡 Advice:\n");

            if (savingsRate > 20) {
                insights.append("Great job! You're saving ")
                        .append(String.format("%.1f", savingsRate))
                        .append("% of your income.");
            } else if (savingsRate > 0) {
                insights.append("Try saving more! Current rate: ")
                        .append(String.format("%.1f", savingsRate))
                        .append("%");
            } else {
                insights.append("Warning: You're spending more than you earn!");
            }
        }

        return insights.toString();
    }

    private List<Transaction> getLastNTransactions(List<Transaction> transactions, int n) {
        int startIndex = Math.max(0, transactions.size() - n);
        return transactions.subList(startIndex, transactions.size());
    }

    public void close() {
        if (predictor != null) {
            predictor.close();
        }
    }
}