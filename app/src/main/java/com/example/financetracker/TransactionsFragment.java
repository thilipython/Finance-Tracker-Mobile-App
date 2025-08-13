package com.example.financetracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionsFragment extends Fragment implements TransactionAdapter.OnTransactionClickListener {

    private static final int ADD_TRANSACTION_REQUEST = 1;
    private static final int EDIT_TRANSACTION_REQUEST = 2;

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private AppDatabase db;
    private TextView tvBalance;
    private FloatingActionButton fabAddTransaction;
    private Button btnInsights;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewTransactions);
        tvBalance = view.findViewById(R.id.tvBalance);
        fabAddTransaction = view.findViewById(R.id.fabAddTransaction);
        btnInsights = view.findViewById(R.id.btnShowInsights);
        db = AppDatabase.getInstance(requireContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        fabAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddTransactionActivity.class);
            startActivityForResult(intent, ADD_TRANSACTION_REQUEST);
        });

        btnInsights.setOnClickListener(v -> {
            AlertDialog loadingDialog = new AlertDialog.Builder(requireContext())
                    .setTitle("Analyzing...")
                    .setMessage("Crunching your numbers...")
                    .setCancelable(false)
                    .show();

            new Thread(() -> {
                SpendingAnalyzer analyzer = new SpendingAnalyzer(requireContext());
                String analysis = analyzer.analyze();

                requireActivity().runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Your Spending Insights")
                            .setMessage(analysis)
                            .setPositiveButton("OK", null)
                            .show();
                });
            }).start();
        });

        loadTransactions();
        return view;
    }

    @Override
    public void onTransactionClick(Transaction transaction) {
        // Handle click (edit)
        Intent intent = new Intent(getActivity(), AddTransactionActivity.class);
        intent.putExtra("EDIT_MODE", true);
        intent.putExtra("TRANSACTION_ID", transaction.id);
        startActivityForResult(intent, EDIT_TRANSACTION_REQUEST);
    }

    @Override
    public void onTransactionLongClick(Transaction transaction) {
        // Handle long click (delete confirmation)
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int position = adapter.getTransactions().indexOf(transaction);
                    if (position != -1) {
                        deleteTransaction(transaction, position);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupSwipeToDelete();
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Transaction transaction = adapter.getTransactions().get(position);
                deleteTransaction(transaction, position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                Paint paint = new Paint();
                paint.setColor(ContextCompat.getColor(requireContext(), R.color.delete_red));

                if (dX > 0) {
                    c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                            (float) itemView.getBottom(), paint);
                } else {
                    c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                            (float) itemView.getRight(), (float) itemView.getBottom(), paint);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void loadTransactions() {
        if (isAdded() && getContext() != null) {
            new LoadTransactionsTask(getContext()).execute();
        }
    }

    private void updateBalance() {
        if (isAdded() && getContext() != null) {
            new CalculateBalanceTask(getContext()).execute();
        }
    }

    private void deleteTransaction(Transaction transaction, int position) {
        if (isAdded() && getContext() != null) {
            new DeleteTransactionTask(getContext(), transaction, position).execute();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            loadTransactions();
        }
    }

    private class LoadTransactionsTask extends AsyncTask<Void, Void, List<Transaction>> {
        private WeakReference<Context> contextRef;

        LoadTransactionsTask(Context context) {
            this.contextRef = new WeakReference<>(context);
        }

        @Override
        protected List<Transaction> doInBackground(Void... voids) {
            try {
                return db.transactionDao().getAll();
            } catch (Exception e) {
                Log.e("LoadTransactions", "Error loading transactions", e);
                return new ArrayList<>();
            }
        }

        @Override
        protected void onPostExecute(List<Transaction> transactions) {
            Context context = contextRef.get();
            if (context != null && isAdded()) {
                adapter.setTransactions(transactions);
                updateBalance();
            }
        }
    }

    private class CalculateBalanceTask extends AsyncTask<Void, Void, Double> {
        private WeakReference<Context> contextRef;

        CalculateBalanceTask(Context context) {
            this.contextRef = new WeakReference<>(context);
        }

        @Override
        protected Double doInBackground(Void... voids) {
            double total = 0;
            try {
                List<Transaction> transactions = db.transactionDao().getAll();
                for (Transaction t : transactions) {
                    total += t.amount;
                }
            } catch (Exception e) {
                Log.e("CalculateBalance", "Error calculating balance", e);
            }
            return total;
        }

        @Override
        protected void onPostExecute(Double total) {
            Context context = contextRef.get();
            if (context != null && isAdded()) {
                NumberFormat format = NumberFormat.getCurrencyInstance(Locale.UK);
                tvBalance.setText(getString(R.string.total_balance, format.format(total)));
            }
        }
    }

    private class DeleteTransactionTask extends AsyncTask<Void, Void, Boolean> {
        private final Transaction transaction;
        private final int position;
        private WeakReference<Context> contextRef;

        DeleteTransactionTask(Context context, Transaction transaction, int position) {
            this.contextRef = new WeakReference<>(context);
            this.transaction = transaction;
            this.position = position;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                db.transactionDao().delete(transaction);
                return true;
            } catch (Exception e) {
                Log.e("DeleteTask", "Error deleting transaction", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            Context context = contextRef.get();
            if (context != null && isAdded()) {
                if (success) {
                    adapter.getTransactions().remove(position);
                    adapter.notifyItemRemoved(position);
                    updateBalance();
                    Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show();
                } else {
                    adapter.notifyItemChanged(position);
                    Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}