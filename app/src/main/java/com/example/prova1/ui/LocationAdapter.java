package com.example.prova1.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prova1.R;
import com.example.prova1.models.LocationData;

import java.util.ArrayList;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    public interface OnItemInteractionListener {
        void onDeleteClick(int position);
        void onFavoriteClick(int position);
    }

    private List<LocationData> locations = new ArrayList<>();
    private OnItemInteractionListener listener;

    public void setLocations(List<LocationData> locations) {
        this.locations = locations;
        notifyDataSetChanged();
    }

    public void setOnItemInteractionListener(OnItemInteractionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_location, parent, false);
        return new LocationViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        LocationData location = locations.get(position);
        holder.bind(location);
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        private final TextView locationText;
        private final TextView weatherDescription;
        private final TextView alertDescription;
        private final Group alertGroup;
        private final ImageView deleteButton;
        private final ImageView favoriteButton;

        public LocationViewHolder(@NonNull View itemView, final OnItemInteractionListener listener) {
            super(itemView);
            locationText = itemView.findViewById(R.id.location_text);
            weatherDescription = itemView.findViewById(R.id.weather_description);
            alertDescription = itemView.findViewById(R.id.alert_description);
            alertGroup = itemView.findViewById(R.id.alert_group);
            deleteButton = itemView.findViewById(R.id.delete_button);
            favoriteButton = itemView.findViewById(R.id.favorite_button);

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(position);
                    }
                }
            });

            favoriteButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onFavoriteClick(position);
                    }
                }
            });
        }

        public void bind(LocationData location) {
            locationText.setText(location.getName());

            String weatherInfo = location.getWeatherInfo();
            String airQualityInfo = location.getAirQualityInfo();

            StringBuilder displayText = new StringBuilder(weatherInfo);
            if (airQualityInfo != null && !airQualityInfo.isEmpty()) {
                if (displayText.length() > 0 && !displayText.toString().equals("Caricamento...")) {
                    displayText.append(", ");
                }
                displayText.append(airQualityInfo);
            }
            weatherDescription.setText(displayText.toString());

            if (location.isFavorite()) {
                favoriteButton.setImageResource(R.drawable.ic_heart_filled);
            } else {
                favoriteButton.setImageResource(R.drawable.ic_heart_empty);
            }

            if (location.getAlertInfo() != null && !location.getAlertInfo().isEmpty()) {
                alertDescription.setText(location.getAlertInfo());
                alertGroup.setVisibility(View.VISIBLE);
            } else {
                alertGroup.setVisibility(View.GONE);
            }
        }
    }
}
