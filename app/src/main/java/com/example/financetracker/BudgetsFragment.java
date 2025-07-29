package com.example.financetracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BudgetsFragment extends Fragment {

    private static final int REQUEST_ADD_BUDGET = 1;

    private RecyclerView recyclerView;
    private BudgetAdapter adapter;
    private AppDatabase db;
    private ExecutorService executor;
    private TextView tvEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budgets, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewBudgets);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        FloatingActionButton fabAddBudget = view.findViewById(R.id.fabAddBudget);

        db = AppDatabase.getInstance(requireContext());
        executor = Executors.newSingleThreadExecutor();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BudgetAdapter(new ArrayList<>(), requireContext());
        recyclerView.setAdapter(adapter);

        fabAddBudget.setOnClickListener(v -> launchAddBudgetActivity());

        loadBudgets();

        return view;
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
                Budget budget = adapter.getBudgetAtPosition(position);
                deleteBudget(budget, position);
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

    private void launchAddBudgetActivity() {
        Intent intent = new Intent(getActivity(), AddBudgetActivity.class);
        startActivityForResult(intent, REQUEST_ADD_BUDGET);
    }

    private void loadBudgets() {
        executor.execute(() -> {
            try {
                List<Budget> budgets = db.budgetDao().getAllBudgets();
                requireActivity().runOnUiThread(() -> {
                    adapter.setBudgets(budgets);
                    updateEmptyState(budgets);
                });
            } catch (Exception e) {
                Log.e("BudgetsFragment", "Error loading budgets", e);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to load budgets", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void deleteBudget(Budget budget, int position) {
        executor.execute(() -> {
            try {
                db.budgetDao().delete(budget);
                requireActivity().runOnUiThread(() -> {
                    adapter.removeBudgetAtPosition(position);
                    Toast.makeText(requireContext(), "Budget deleted", Toast.LENGTH_SHORT).show();
                    updateEmptyState(adapter.getBudgets());
                });
            } catch (Exception e) {
                Log.e("BudgetsFragment", "Error deleting budget", e);
                requireActivity().runOnUiThread(() -> {
                    adapter.notifyItemChanged(position);
                    Toast.makeText(requireContext(), "Failed to delete budget", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateEmptyState(List<Budget> budgets) {
        if (budgets.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_BUDGET && resultCode == Activity.RESULT_OK) {
            loadBudgets();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}