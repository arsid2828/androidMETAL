package com.example.prova1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prova1.models.AlertViewModel;
import com.example.prova1.models.WindAlert;
import com.example.prova1.ui.AlertAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment implements AlertAdapter.OnDeleteClickListener {

    private AlertViewModel alertViewModel;
    private RecyclerView recyclerView;
    private AlertAdapter adapter;
    private FloatingActionButton fabDeleteAll;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.alerts_recycler_view);
        fabDeleteAll = view.findViewById(R.id.fab_delete_all);

        setupRecyclerView();
        setupViewModel();
        setupFab();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AlertAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        alertViewModel = new ViewModelProvider(requireActivity()).get(AlertViewModel.class);
        alertViewModel.getWindAlerts().observe(getViewLifecycleOwner(), windAlerts -> {
            adapter.submitList(windAlerts);
            // Mostra o nascondi il FAB in base alla presenza di notifiche
            if (windAlerts == null || windAlerts.isEmpty()) {
                fabDeleteAll.hide();
            } else {
                fabDeleteAll.show();
            }
        });
    }

    private void setupFab() {
        fabDeleteAll.setOnClickListener(v -> {
            alertViewModel.deleteAllAlerts();
        });
    }

    @Override
    public void onDeleteClick(WindAlert alert) {
        alertViewModel.deleteAlert(alert);
    }
}
