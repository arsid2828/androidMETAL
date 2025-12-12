package com.example.prova1.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class AlertViewModel extends ViewModel {
    private final MutableLiveData<List<WindAlert>> windAlerts = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<WindAlert>> getWindAlerts() {
        return windAlerts;
    }

    public void addWindAlert(WindAlert alert) {
        List<WindAlert> currentAlerts = windAlerts.getValue();
        if (currentAlerts != null) {
            currentAlerts.add(0, alert); // Aggiungi in cima alla lista
            windAlerts.setValue(currentAlerts);
        }
    }
}
