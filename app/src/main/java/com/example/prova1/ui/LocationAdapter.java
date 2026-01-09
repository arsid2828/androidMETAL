package com.example.prova1.ui;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
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
        private final TextView temperatureText;
        private final TextView humidityText;
        private final TextView windText;
        private final TextView precipitationText;
        private final LinearLayout pm25Layout;
        private final TextView pm25Text;
        private final LinearLayout cloudCoverLayout;
        private final TextView cloudCoverText;
        private final LinearLayout apparentTemperatureLayout;
        private final TextView apparentTemperatureText;
        private final LinearLayout uvIndexLayout;
        private final TextView uvIndexText;
        private final LinearLayout visibilityLayout;
        private final TextView visibilityText;
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
            temperatureText = itemView.findViewById(R.id.temperature_text);
            humidityText = itemView.findViewById(R.id.humidity_text);
            windText = itemView.findViewById(R.id.wind_text);
            precipitationText = itemView.findViewById(R.id.precipitation_text);
            pm25Layout = itemView.findViewById(R.id.pm25_layout);
            pm25Text = itemView.findViewById(R.id.pm25_text);
            cloudCoverLayout = itemView.findViewById(R.id.cloud_cover_layout);
            cloudCoverText = itemView.findViewById(R.id.cloud_cover_text);
            apparentTemperatureLayout = itemView.findViewById(R.id.apparent_temperature_layout);
            apparentTemperatureText = itemView.findViewById(R.id.apparent_temperature_text);
            uvIndexLayout = itemView.findViewById(R.id.uv_index_layout);
            uvIndexText = itemView.findViewById(R.id.uv_index_text);
            visibilityLayout = itemView.findViewById(R.id.visibility_layout);
            visibilityText = itemView.findViewById(R.id.visibility_text);
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
                weatherDescription.setVisibility(View.GONE);
                temperatureText.setText(String.format("%.1f °C", location.getTemperature()));
                humidityText.setText(String.format("%d%%", location.getHumidity()));
                windText.setText(String.format("%.1f km/h", location.getWindSpeed()));
                precipitationText.setText(String.format("%.1f mm", location.getPrecipitation()));
            } else {
                weatherDescription.setVisibility(View.VISIBLE);
                weatherDescription.setText(location.getWeatherInfo());
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(itemView.getContext());
            boolean showPm25 = prefs.getBoolean("pm25", false) && hasAirQuality;
            boolean showCloudCover = prefs.getBoolean("cloud_cover", false) && hasWeather;
            boolean showApparentTemp = prefs.getBoolean("apparent_temperature", false) && hasWeather;
            boolean showUvIndex = prefs.getBoolean("uv_index", false) && hasWeather;
            boolean showVisibility = prefs.getBoolean("visibility", false) && hasWeather;

            pm25Layout.setVisibility(showPm25 ? View.VISIBLE : View.GONE);
            if(showPm25) {
                pm25Text.setText(String.format("%.1f μg/m³", location.getPm25()));
            }
            cloudCoverLayout.setVisibility(showCloudCover ? View.VISIBLE : View.GONE);
            if (showCloudCover) {
                cloudCoverText.setText(String.format("%d%%", location.getCloudCover()));
            }
            apparentTemperatureLayout.setVisibility(showApparentTemp ? View.VISIBLE : View.GONE);
            if (showApparentTemp) {
                apparentTemperatureText.setText(String.format("%.1f °C", location.getApparentTemperature()));
            }
            uvIndexLayout.setVisibility(showUvIndex ? View.VISIBLE : View.GONE);
            if (showUvIndex) {
                uvIndexText.setText(String.format("%.1f", location.getUvIndex()));
            }
            visibilityLayout.setVisibility(showVisibility ? View.VISIBLE : View.GONE);
            if (showVisibility) {
                visibilityText.setText(String.format("%.1f km", location.getVisibility() / 1000));
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
