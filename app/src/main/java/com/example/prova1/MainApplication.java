package com.example.prova1;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.prova1.workers.AlertWorker;

import java.util.concurrent.TimeUnit;

public class MainApplication extends Application {

    public static final String WIND_CHANNEL_ID = "wind_notification_channel";
    public static final String FEED_CHANNEL_ID = "feed_notification_channel";
    public static final String TEMP_CHANNEL_ID = "temp_notification_channel";
    public static final String AIR_QUALITY_CHANNEL_ID = "air_quality_notification_channel";
    public static final String VISIBILITY_CHANNEL_ID = "visibility_notification_channel";
    public static final String UV_INDEX_CHANNEL_ID = "uv_index_notification_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
        setupPeriodicWork();
    }

    private void setupPeriodicWork() {
        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
                AlertWorker.class, 15, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(this).enqueue(periodicWorkRequest);
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel windChannel = new NotificationChannel(
                    WIND_CHANNEL_ID,
                    "Notifiche Vento",
                    NotificationManager.IMPORTANCE_HIGH);
            windChannel.setDescription("Canale per notifiche relative alla velocità del vento.");

            NotificationChannel feedChannel = new NotificationChannel(
                    FEED_CHANNEL_ID,
                    "Notifiche Allarmi Meteo",
                    NotificationManager.IMPORTANCE_HIGH);
            feedChannel.setDescription("Canale per notifiche relative a feed di allarmi.");

            NotificationChannel tempChannel = new NotificationChannel(
                    TEMP_CHANNEL_ID,
                    "Notifiche Temperatura",
                    NotificationManager.IMPORTANCE_DEFAULT);
            tempChannel.setDescription("Canale per notifiche relative alla temperatura.");

            NotificationChannel airQualityChannel = new NotificationChannel(
                    AIR_QUALITY_CHANNEL_ID,
                    "Notifiche Qualità dell'Aria",
                    NotificationManager.IMPORTANCE_DEFAULT);
            airQualityChannel.setDescription("Canale per notifiche relative alla qualità dell'aria.");

            NotificationChannel visibilityChannel = new NotificationChannel(
                    VISIBILITY_CHANNEL_ID,
                    "Notifiche Visibilità",
                    NotificationManager.IMPORTANCE_DEFAULT);
            visibilityChannel.setDescription("Canale per notifiche relative alla visibilità.");

            NotificationChannel uvIndexChannel = new NotificationChannel(
                    UV_INDEX_CHANNEL_ID,
                    "Notifiche Indice UV",
                    NotificationManager.IMPORTANCE_DEFAULT);
            uvIndexChannel.setDescription("Canale per notifiche relative all'indice UV.");


            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(windChannel);
            manager.createNotificationChannel(feedChannel);
            manager.createNotificationChannel(tempChannel);
            manager.createNotificationChannel(airQualityChannel);
            manager.createNotificationChannel(visibilityChannel);
            manager.createNotificationChannel(uvIndexChannel);
        }
    }
}
