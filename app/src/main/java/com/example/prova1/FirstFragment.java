package com.example.prova1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class FirstFragment extends Fragment {

    /**
     * esndrogo tongo
     * Questo è il "callback" che viene eseguito non appena la mappa è pronta per essere usata.
     * È qui che inseriremo tutta la logica per personalizzare la mappa.
     */
    private final OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            // La mappa è pronta, ora possiamo interagire con essa.

            // 1. Definiamo le coordinate del Veneto.
            // Ho scelto due punti (sud-ovest e nord-est) per creare un rettangolo
            // che contenga l'intera regione.
            LatLng sudOvest = new LatLng(44.8, 10.5);
            LatLng nordEst = new LatLng(46.5, 13.0);
            LatLngBounds venetoBounds = new LatLngBounds(sudOvest, nordEst);

            // 2. Centriamo la visuale sulla regione Veneto.
            // Il 'padding' di 50 (pixel) assicura che ci sia un po' di margine.
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(venetoBounds, 50));

            // 3. Impostiamo uno zoom minimo per non "uscire" troppo dalla regione.
            googleMap.setMinZoomPreference(7.5f);

            // 4. (Opzionale) Aggiungiamo un marcatore, ad esempio su Venezia.
            LatLng venezia = new LatLng(45.4408, 12.3155);
            // googleMap.addMarker(new MarkerOptions().position(venezia).title("Marcatore su Venezia"));
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Qui "gonfiamo" il layout che abbiamo modificato prima (fragment_first.xml).
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Troviamo il frammento della mappa che abbiamo inserito nel layout XML.
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        // Registriamo il nostro callback: quando la mappa è pronta,
        // il sistema eseguirà il codice dentro 'onMapReady'.
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}
    