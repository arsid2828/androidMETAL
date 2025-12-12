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
        loadInitialAlerts();
    }

    private void loadInitialAlerts() {
        List<WindAlert> storedAlerts = NotificationStorageHelper.loadAlerts(getApplication());
        windAlerts.setValue(storedAlerts);
    }

    public LiveData<List<WindAlert>> getWindAlerts() {
        return windAlerts;
    }

    public void addNewAlerts(List<WindAlert> newAlerts) {
        if (newAlerts == null || newAlerts.isEmpty()) {
            return;
        }
        NotificationStorageHelper.saveAlerts(getApplication(), newAlerts);
        List<WindAlert> allAlerts = NotificationStorageHelper.loadAlerts(getApplication());
        windAlerts.setValue(allAlerts);
    }

    public void addWindAlert(WindAlert alert) {
        if (alert == null) {
            return;
        }
        List<WindAlert> newAlerts = new ArrayList<>();
        newAlerts.add(alert);
        addNewAlerts(newAlerts);
    }

    public void deleteAlert(WindAlert alert) {
        List<WindAlert> currentAlerts = windAlerts.getValue();
        if (currentAlerts != null) {
            currentAlerts.remove(alert);
            NotificationStorageHelper.overwriteAlerts(getApplication(), currentAlerts);
            windAlerts.setValue(new ArrayList<>(currentAlerts)); 
        }
    }

    public void deleteAllAlerts() {
        NotificationStorageHelper.overwriteAlerts(getApplication(), new ArrayList<>());
        windAlerts.setValue(new ArrayList<>());
    }
}
