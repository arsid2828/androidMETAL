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
import com.example.prova1.ui.AlertAdapter;

public class NotificationsFragment extends Fragment {

    private AlertViewModel alertViewModel;
    private RecyclerView recyclerView;
    private AlertAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.alerts_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        alertViewModel = new ViewModelProvider(requireActivity()).get(AlertViewModel.class);

        alertViewModel.getWindAlerts().observe(getViewLifecycleOwner(), windAlerts -> {
            if (adapter == null) {
                adapter = new AlertAdapter(windAlerts);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }
        });
    }
}
