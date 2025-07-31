package com.example.financetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SavingsGoalAdapter extends RecyclerView.Adapter<SavingsGoalAdapter.SavingsGoalViewHolder> {

    private List<SavingsGoal> savingsGoals;
    private OnSavingsGoalClickListener listener;

    public interface OnSavingsGoalClickListener {
        void onSavingsGoalClick(SavingsGoal goal);
    }

    public SavingsGoalAdapter(List<SavingsGoal> savingsGoals, OnSavingsGoalClickListener listener) {
        this.savingsGoals = savingsGoals;
        this.listener = listener;
    }

    public SavingsGoal getGoalAtPosition(int position) {
        return savingsGoals.get(position);
    }

    public void removeGoalAtPosition(int position) {
        savingsGoals.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public SavingsGoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_savings_goal, parent, false);
        return new SavingsGoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavingsGoalViewHolder holder, int position) {
        SavingsGoal goal = savingsGoals.get(position);
        holder.bind(goal);
        holder.itemView.setOnClickListener(v -> listener.onSavingsGoalClick(goal));
    }

    @Override
    public int getItemCount() {
        return savingsGoals.size();
    }

    public void setSavingsGoals(List<SavingsGoal> goals) {
        this.savingsGoals = goals;
        notifyDataSetChanged();
    }

    static class SavingsGoalViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvTargetAmount;
        private TextView tvCurrentAmount;
        private TextView tvTargetDate;
        private ProgressBar progressBar;

        public SavingsGoalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvGoalName);
            tvTargetAmount = itemView.findViewById(R.id.tvTargetAmount);
            tvCurrentAmount = itemView.findViewById(R.id.tvCurrentAmount);
            tvTargetDate = itemView.findViewById(R.id.tvTargetDate);
            progressBar = itemView.findViewById(R.id.progressBar);
        }

        public void bind(SavingsGoal goal) {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.UK);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

            tvName.setText(goal.name);
            tvTargetAmount.setText(currencyFormat.format(goal.targetAmount));
            tvCurrentAmount.setText(currencyFormat.format(goal.currentAmount));
            tvTargetDate.setText("Target: " + dateFormat.format(new Date(goal.targetDate)));

            int progress = (int) ((goal.currentAmount / goal.targetAmount) * 100);
            progressBar.setProgress(progress);
        }
    }
}