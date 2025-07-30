package com.example.financetracker;

import android.app.Activity;
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

public class SavingsGoalsFragment extends Fragment implements SavingsGoalAdapter.OnSavingsGoalClickListener {

    private static final int REQUEST_ADD_GOAL = 1;
    private static final int REQUEST_EDIT_GOAL = 2;

    private RecyclerView recyclerView;
    private SavingsGoalAdapter adapter;
    private AppDatabase db;
    private ExecutorService executor;
    private TextView tvEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_savings_goals, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewSavingsGoals);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        FloatingActionButton fabAddGoal = view.findViewById(R.id.fabAddGoal);

        db = AppDatabase.getInstance(requireContext());
        executor = Executors.newSingleThreadExecutor();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SavingsGoalAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        fabAddGoal.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddSavingsGoalActivity.class);
            startActivityForResult(intent, REQUEST_ADD_GOAL);
        });

        loadGoals();
        setupSwipeToDelete();

        return view;
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
                SavingsGoal goal = adapter.getGoalAtPosition(position);
                deleteGoal(goal, position);
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

    private void deleteGoal(SavingsGoal goal, int position) {
        executor.execute(() -> {
            try {
                db.savingsGoalDao().delete(goal);
                requireActivity().runOnUiThread(() -> {
                    adapter.removeGoalAtPosition(position);
                    Toast.makeText(requireContext(), "Goal deleted", Toast.LENGTH_SHORT).show();
                    updateEmptyState(adapter.getItemCount() == 0);
                });
            } catch (Exception e) {
                Log.e("SavingsGoalsFragment", "Error deleting goal", e);
                requireActivity().runOnUiThread(() -> {
                    adapter.notifyItemChanged(position);
                    Toast.makeText(requireContext(), "Failed to delete goal", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onSavingsGoalClick(SavingsGoal goal) {
        Intent intent = new Intent(getActivity(), AddSavingsGoalActivity.class);
        intent.putExtra("EDIT_MODE", true);
        intent.putExtra("GOAL_ID", goal.id);
        startActivityForResult(intent, REQUEST_EDIT_GOAL);
    }

    private void loadGoals() {
        executor.execute(() -> {
            List<SavingsGoal> goals = db.savingsGoalDao().getAllGoals();
            requireActivity().runOnUiThread(() -> {
                adapter.setSavingsGoals(goals);
                updateEmptyState(goals.isEmpty());
            });
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
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
        if (resultCode == Activity.RESULT_OK) {
            loadGoals();
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