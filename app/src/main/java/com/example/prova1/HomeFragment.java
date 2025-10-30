package com.example.prova1;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private RecyclerView recyclerView;
    private HomeAdapter adapter;
    private List<String> data = new ArrayList<>();
    private IspraApiService apiService;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new HomeAdapter(data);
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getClient().create(IspraApiService.class);

        Button addButton = view.findViewById(R.id.button_add);
        addButton.setOnClickListener(v -> {
            int newItemNumber = data.size() + 1;
            data.add("Nuovo elemento " + newItemNumber);
            adapter.notifyItemInserted(data.size() - 1);
        });

        Button searchButton = view.findViewById(R.id.button_search);
        searchButton.setOnClickListener(v -> {
            searchDisasterEvents();
        });
    }

    private void searchDisasterEvents() {
        data.clear();
        adapter.notifyDataSetChanged();

        Call<DisasterEvent> call = apiService.getDisasterEvents();
        call.enqueue(new Callback<DisasterEvent>() {
            @Override
            public void onResponse(Call<DisasterEvent> call, Response<DisasterEvent> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DisasterEvent featureCollection = response.body();
                    for (DisasterFeature feature : featureCollection.getFeatures()) {
                        DisasterProperties properties = feature.getProperties();
                        String displayText = "Classe: " + properties.getDisasterClass() + ", Tipo: " + properties.getDisasterType();
                        data.add(displayText);
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Errore nel recupero dei dati", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DisasterEvent> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
                Toast.makeText(getContext(), "Chiamata API fallita", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
