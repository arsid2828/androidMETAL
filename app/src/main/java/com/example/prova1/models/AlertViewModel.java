package com.example.prova1.models;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.prova1.helpers.NotificationStorageHelper;

import java.util.ArrayList;
import java.util.List;

public class AlertViewModel extends AndroidViewModel {
    private final MutableLiveData<List<WindAlert>> windAlerts = new MutableLiveData<>();

    public AlertViewModel(@NonNull Application application) {
        super(application);
        // All'avvio, carica le notifiche salvate in precedenza
        loadInitialAlerts();
    }

    private void loadInitialAlerts() {
        List<WindAlert> storedAlerts = NotificationStorageHelper.loadAlerts(getApplication());
        windAlerts.setValue(storedAlerts);
    }

    public LiveData<List<WindAlert>> getWindAlerts() {
        return windAlerts;
    }

    /**
     * Aggiunge una lista di nuove notifiche, le salva su file e aggiorna la UI.
     * Il meccanismo anti-duplicati è gestito dallo StorageHelper.
     */
    public void addNewAlerts(List<WindAlert> newAlerts) {
        if (newAlerts == null || newAlerts.isEmpty()) {
            return;
        }

        // Salva le nuove notifiche. L'helper si occupa di evitare i duplicati.
        NotificationStorageHelper.saveAlerts(getApplication(), newAlerts);

        // Ricarica la lista completa dal file per avere un'unica fonte di verità e aggiorna la UI.
        List<WindAlert> allAlerts = NotificationStorageHelper.loadAlerts(getApplication());
        windAlerts.setValue(allAlerts);
    }

    /**
     * Metodo di convenienza per aggiungere un singolo alert.
     */
    public void addWindAlert(WindAlert alert) {
        if (alert == null) {
            return;
        }
        List<WindAlert> newAlerts = new ArrayList<>();
        newAlerts.add(alert);
        addNewAlerts(newAlerts);
    }
}
