package com.example.prova1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;

public class AllerteFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_allerte, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.normativa_ghiaccio_neve).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_AllerteFragment_to_GhiaccioNeveFragment);
        });

        view.findViewById(R.id.normativa_temporali).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_AllerteFragment_to_TemporaliFragment);
        });

        view.findViewById(R.id.normativa_nebbia).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_AllerteFragment_to_NebbiaFragment);
        });

        view.findViewById(R.id.normativa_pioggia).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_AllerteFragment_to_PioggiaFragment);
        });

        view.findViewById(R.id.normativa_vento).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_AllerteFragment_to_VentoFragment);
        });

        view.findViewById(R.id.normativa_temperatura).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_AllerteFragment_to_TemperaturaFragment);
        });

        view.findViewById(R.id.normativa_qualita_aria).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_AllerteFragment_to_QualitaAriaFragment);
        });

        view.findViewById(R.id.normativa_raggi_uv).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_AllerteFragment_to_RaggiUvFragment);
        });
    }

}
