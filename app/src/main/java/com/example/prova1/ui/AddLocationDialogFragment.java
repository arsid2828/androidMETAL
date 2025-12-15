package com.example.prova1.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.prova1.R;

public class AddLocationDialogFragment extends DialogFragment {

    public interface AddLocationDialogListener {
        void onDialogPositiveClick(String cityName);
    }

    private AddLocationDialogListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            listener = (AddLocationDialogListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement AddLocationDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_location, null);
        final EditText cityNameInput = view.findViewById(R.id.city_name_input);

        builder.setView(view)
                .setPositiveButton("Aggiungi", (dialog, id) -> {
                    String cityName = cityNameInput.getText().toString();
                    if (!cityName.isEmpty()) {
                        listener.onDialogPositiveClick(cityName);
                    }
                })
                .setNegativeButton("Annulla", (dialog, id) -> AddLocationDialogFragment.this.getDialog().cancel());
        return builder.create();
    }
}
