package com.example.financetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactions;

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = new ArrayList<>(transactions);
    }

    public void setTransactions(List<Transaction> newTransactions) {
        this.transactions = new ArrayList<>(newTransactions);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        // Set all fields
        holder.tvTitle.setText(transaction.title);
        holder.tvCategory.setText(transaction.category);

        String amountText = String.format(Locale.US, "%s%.2f",
                transaction.amount >= 0 ? "+" : "",
                Math.abs(transaction.amount));
        holder.tvAmount.setText(amountText);

        int color = ContextCompat.getColor(holder.itemView.getContext(),
                transaction.amount >= 0 ? R.color.income_green : R.color.expense_red);
        holder.tvAmount.setTextColor(color);

        if (transaction.formattedDate != null) {
            holder.tvDate.setText(transaction.formattedDate);
            holder.tvDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvDate.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvAmount, tvDate;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}