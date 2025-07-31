package com.example.financetracker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {
    private List<Budget> budgets;
    private final Context context;
    private final OnBudgetClickListener listener;

    public interface OnBudgetClickListener {
        void onBudgetClick(Budget budget);
    }

    public BudgetAdapter(List<Budget> budgets, Context context, OnBudgetClickListener listener) {
        this.budgets = budgets;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Budget budget = budgets.get(position);
        holder.tvCategory.setText(budget.category);
        holder.tvLimit.setText(String.format("Limit: £%.2f", budget.limit));

        double percentage = (budget.currentSpending / budget.limit) * 100;
        percentage = Math.min(percentage, 100);

        holder.progressBar.setProgress((int) percentage);
        holder.tvSpent.setText(String.format("Spent: £%.2f (%.0f%%)",
                budget.currentSpending, percentage));

        int color;
        if (percentage < 75) {
            color = ContextCompat.getColor(context, R.color.progress_green);
        } else if (percentage < 100) {
            color = ContextCompat.getColor(context, R.color.progress_yellow);
        } else {
            color = ContextCompat.getColor(context, R.color.progress_red);
        }

        holder.progressBar.setProgressTintList(ColorStateList.valueOf(color));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBudgetClick(budget);
            }
        });
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    public void setBudgets(List<Budget> newBudgets) {
        this.budgets = new ArrayList<>(newBudgets); // Create new list to force refresh
        notifyDataSetChanged(); // Important: trigger UI update
    }

    public Budget getBudgetAtPosition(int position) {
        return budgets.get(position);
    }

    public void removeBudgetAtPosition(int position) {
        budgets.remove(position);
        notifyItemRemoved(position);
    }

    public List<Budget> getBudgets() {
        return budgets;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvLimit, tvSpent;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvLimit = itemView.findViewById(R.id.tvLimit);
            tvSpent = itemView.findViewById(R.id.tvSpent);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}