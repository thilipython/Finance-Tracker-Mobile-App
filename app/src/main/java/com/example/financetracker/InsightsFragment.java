package com.example.financetracker;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsightsFragment extends Fragment {

    private TextView tvInsightsResult;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insights, container, false);

        tvInsightsResult = view.findViewById(R.id.tvInsightsResult);
        Button btnAnalyze = view.findViewById(R.id.btnAnalyze);
        db = AppDatabase.getInstance(requireContext());

        Button btnInsights = view.findViewById(R.id.btnShowInsights);
        btnInsights.setOnClickListener(v -> {

            new AlertDialog.Builder(requireContext())
                    .setTitle("Spending Insights")
                    .setMessage("Insights will appear here after Step 2")
                    .setPositiveButton("OK", null)
                    .show();
        });

        btnAnalyze.setOnClickListener(v -> analyzeSpending());

        return view;
    }

    private void analyzeSpending() {
        tvInsightsResult.setText("Analyzing your spending...");
        new AnalyzeTask().execute();
    }

    private class AnalyzeTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {

            return "Analysis coming in Step 2!";
        }

        @Override
        protected void onPostExecute(String result) {
            tvInsightsResult.setText(result);
        }
    }
}