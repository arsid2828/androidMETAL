package com.example.prova1.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prova1.R;
import com.example.prova1.models.LocationData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    public interface OnItemInteractionListener {
        void onDeleteClick(int position);
        void onFavoriteClick(int position);
    }

    private List<LocationData> locations = new ArrayList<>();
    private OnItemInteractionListener listener;

    public void setLocations(List<LocationData> locations) {
        Collections.sort(locations, (o1, o2) -> {
            if (o1.isCurrentLocation()) return -1;
            if (o2.isCurrentLocation()) return 1;
            return Boolean.compare(o2.isFavorite(), o1.isFavorite());
        });
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
        private final ImageView deleteButton;
        private final ImageView favoriteButton;
        private final TableLayout weatherDataLayout;
        private final TextView temperatureText;
        private final TextView humidityText;
        private final TextView windText;
        private final TextView precipitationText;
        private final LinearLayout pm25Layout;
        private final TextView pm25Text;
        private final LinearLayout cloudCoverLayout;
        private final TextView cloudCoverText;
        private final ConstraintLayout alertSectionLayout;
        private final ImageView alertIcon;
        private final TextView yourLocationLabel;


        public LocationViewHolder(@NonNull View itemView, final OnItemInteractionListener listener) {
            super(itemView);
            locationText = itemView.findViewById(R.id.location_text);
            weatherDescription = itemView.findViewById(R.id.weather_description);
            alertDescription = itemView.findViewById(R.id.alert_description);
            deleteButton = itemView.findViewById(R.id.delete_button);
            favoriteButton = itemView.findViewById(R.id.favorite_button);
            weatherDataLayout = itemView.findViewById(R.id.weather_data_layout);
            temperatureText = itemView.findViewById(R.id.temperature_text);
            humidityText = itemView.findViewById(R.id.humidity_text);
            windText = itemView.findViewById(R.id.wind_text);
            precipitationText = itemView.findViewById(R.id.precipitation_text);
            pm25Layout = itemView.findViewById(R.id.pm25_layout);
            pm25Text = itemView.findViewById(R.id.pm25_text);
            cloudCoverLayout = itemView.findViewById(R.id.cloud_cover_layout);
            cloudCoverText = itemView.findViewById(R.id.cloud_cover_text);
            alertSectionLayout = itemView.findViewById(R.id.alert_section_layout);
            alertIcon = itemView.findViewById(R.id.alert_icon);
            yourLocationLabel = itemView.findViewById(R.id.your_location_label);

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
            yourLocationLabel.setVisibility(location.isCurrentLocation() ? View.VISIBLE : View.GONE);

            boolean hasWeather = location.getWeatherInfo() != null && location.getWeatherInfo().isEmpty();
            boolean hasAirQuality = location.getAirQualityInfo() != null && location.getAirQualityInfo().isEmpty();

            if (hasWeather) {
                weatherDataLayout.setVisibility(View.VISIBLE);
                weatherDescription.setVisibility(View.GONE);
                temperatureText.setText(String.format("%.1f °C", location.getTemperature()));
                humidityText.setText(String.format("%d%%", location.getHumidity()));
                windText.setText(String.format("%.1f km/h", location.getWindSpeed()));
                precipitationText.setText(String.format("%.1f mm", location.getPrecipitation()));
                cloudCoverText.setText(String.format("%d%%", location.getCloudCover()));
                cloudCoverLayout.setVisibility(View.VISIBLE);
            } else {
                weatherDataLayout.setVisibility(View.GONE);
                weatherDescription.setVisibility(View.VISIBLE);
                weatherDescription.setText(location.getWeatherInfo());
                cloudCoverLayout.setVisibility(View.GONE);
            }

            if (hasAirQuality) {
                pm25Layout.setVisibility(View.VISIBLE);
                pm25Text.setText(String.format("%.1f μg/m³", location.getPm25()));
            } else {
                pm25Layout.setVisibility(View.GONE);
            }


            if (location.isFavorite()) {
                favoriteButton.setImageResource(R.drawable.ic_heart_filled);
            } else {
                favoriteButton.setImageResource(R.drawable.ic_heart_empty);
            }

            if (location.getAlertInfo() != null && !location.getAlertInfo().isEmpty()) {
                alertDescription.setText(location.getAlertInfo());
                alertDescription.setSelected(true); // Start marquee
                
                int severity = location.getAlertSeverity();
                boolean hasRealAlert = severity > 0;
                
                alertIcon.setImageResource(hasRealAlert ? R.drawable.ic_alert : R.drawable.ic_shield_check);
                
                // Set background based on severity
                if (severity == 3) {
                    alertSectionLayout.setBackgroundResource(R.drawable.alert_red_background);
                } else if (severity == 2) {
                    alertSectionLayout.setBackgroundResource(R.drawable.alert_orange_background);
                } else if (severity == 1) {
                    alertSectionLayout.setBackgroundResource(R.drawable.alert_yellow_background);
                } else {
                    alertSectionLayout.setBackgroundResource(R.drawable.alert_green_background);
                }
                
                alertSectionLayout.setVisibility(View.VISIBLE);
            } else {
                alertSectionLayout.setVisibility(View.GONE);
            }
        }
    }
}
