package com.example.prova1.helpers;

import android.content.Context;

import com.example.prova1.models.WindAlert;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class NotificationStorageHelper {

    private static final String FILE_NAME = "notifications.txt";

    // Salva una lista di notifiche, evitando duplicati
    public static void saveAlerts(Context context, List<WindAlert> newAlerts) {
        if (newAlerts == null || newAlerts.isEmpty()) {
            return;
        }

        // Usa un Set per garantire l'unicità e mantenere l'ordine di inserimento
        Set<WindAlert> allAlerts = new LinkedHashSet<>(loadAlerts(context));
        allAlerts.addAll(newAlerts);

        Gson gson = new Gson();
        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            for (WindAlert alert : allAlerts) {
                String json = gson.toJson(alert) + "\n";
                fos.write(json.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Carica tutte le notifiche dal file
    public static List<WindAlert> loadAlerts(Context context) {
        List<WindAlert> alerts = new ArrayList<>();
        try (FileInputStream fis = context.openFileInput(FILE_NAME);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            Gson gson = new Gson();
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    WindAlert alert = gson.fromJson(line, WindAlert.class);
                    alerts.add(alert);
                }
            }
        } catch (IOException e) {
            // Il file potrebbe non esistere al primo avvio, è normale
        }
        return alerts;
    }
}
