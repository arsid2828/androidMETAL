package com.example.prova1;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.metal.idrogeo.api.ApiClient;
import com.metal.idrogeo.api.IdrogeoApiService;
import com.metal.idrogeo.api.NewsItem;
import com.metal.idrogeo.ui.NewsAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirstFragment extends Fragment {

    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private List<NewsItem> dummyList = new ArrayList<>();

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Inizializza la UI
        recyclerView = view.findViewById(R.id.news_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 2. Prepara e mostra SUBITO i dati di prova
        setupDummyData();
        newsAdapter = new NewsAdapter(new ArrayList<>(dummyList)); // Usa una copia per sicurezza
        recyclerView.setAdapter(newsAdapter);
        Toast.makeText(getContext(), "Dati di prova caricati. Tento di aggiornare...", Toast.LENGTH_SHORT).show();

        // 3. ORA, in background, prova a scaricare i dati veri
        fetchNews();
    }

    /**
     * Prepara la lista di dati finti.
     */
    private void setupDummyData() {
        dummyList.clear();
        NewsItem news1 = new NewsItem(1, "FRANA DI PROVA", "Questa è la descrizione di una frana di test.", "2024-01-01", "http://example.com");
        NewsItem news2 = new NewsItem(2, "ALLUVIONE DI TEST", "Descrizione dell'alluvione di prova.", "2024-01-02", "http://example.com");
        dummyList.add(news1);
        dummyList.add(news2);
    }

    private void fetchNews() {
        IdrogeoApiService apiService = ApiClient.getClient().create(IdrogeoApiService.class);
        Call<List<NewsItem>> call = apiService.getNews();

        call.enqueue(new Callback<List<NewsItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<NewsItem>> call, @NonNull Response<List<NewsItem>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // SUCCESSO: ABBIAMO DATI VERI!
                    List<NewsItem> realNews = response.body();
                    newsAdapter.updateData(realNews); // Aggiorna la UI con i dati veri
                    Toast.makeText(getContext(), "AGGIORNAMENTO RIUSCITO: Mostrando " + realNews.size() + " notizie vere.", Toast.LENGTH_LONG).show();
                } else {
                    // FALLIMENTO (ma senza crash): L'API non ha notizie o ha dato errore.
                    // NON FACCIAMO NIENTE ALLA UI. I dati di prova restano.
                    Toast.makeText(getContext(), "AGGIORNAMENTO FALLITO: L'API non ha restituito notizie. Mantengo i dati di prova.", Toast.LENGTH_LONG).show();
                    Log.w("FirstFragment", "API ha risposto con codice: " + response.code() + " o corpo vuoto. Mantengo dati di prova.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<NewsItem>> call, @NonNull Throwable t) {
                // FALLIMENTO DI RETE: Il server non è stato raggiunto.
                // NON FACCIAMO NIENTE ALLA UI. I dati di prova restano.
                Toast.makeText(getContext(), "AGGIORNAMENTO FALLITO: Errore di connessione. Mantengo i dati di prova.", Toast.LENGTH_LONG).show();
                Log.e("FirstFragment", "Fallimento della chiamata di rete. Mantengo dati di prova.", t);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
