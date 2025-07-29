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
import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {
    private List<Budget> budgets;
    private final Context context;

    public BudgetAdapter(List<Budget> budgets, Context context) {
        this.budgets = budgets;
        this.context = context;
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
        holder.tvLimit.setText(String.format("Limit: $%.2f", budget.limit));

        // Calculate percentage spent
        double percentage = (budget.currentSpending / budget.limit) * 100;
        percentage = Math.min(percentage, 100); // Cap at 100%

        holder.progressBar.setProgress((int) percentage);

        // Add this line to properly display spent amount:
        holder.tvSpent.setText(String.format("Spent: $%.2f of $%.2f (%.0f%%)",
                budget.currentSpending,
                budget.limit,
                percentage));

        // Set progress bar color based on percentage
        int color;
        if (percentage < 75) {
            color = ContextCompat.getColor(context, R.color.progress_green);
        } else if (percentage < 100) {
            color = ContextCompat.getColor(context, R.color.progress_yellow);
        } else {
            color = ContextCompat.getColor(context, R.color.progress_red);
        }
        holder.progressBar.setProgressTintList(ColorStateList.valueOf(color));
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    public void setBudgets(List<Budget> newBudgets) {
        this.budgets = newBudgets;
        notifyDataSetChanged();
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